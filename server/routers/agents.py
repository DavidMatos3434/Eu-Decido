"""
Router de Agentes EU DECIDO
==============================
Endpoints para interagir com o sistema de agentes.
"""
from typing import Any, Literal

from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from server.agents import llm as llm_client
from server.agents.orchestrator import process
from server.core.database import get_conn
from server.core.dependencies import get_current_user

router = APIRouter(prefix="/agents", tags=["Agentes"])


# ── Modelos de pedido ──────────────────────────────────────────────────────────

class AnalyzeRequest(BaseModel):
    title: str = ""
    text: str
    territory_name: str = ""
    event: Literal["new_proposal", "new_comment", "text_analyze"] = "text_analyze"


class ProposalAnalyzeRequest(BaseModel):
    proposal_id: str


# ── Endpoints ─────────────────────────────────────────────────────────────────

@router.get("/status")
async def agent_status():
    """Estado do sistema de agentes e do LLM configurado."""
    info = llm_client.provider_info()

    llm_online = False
    llm_error = None
    try:
        test = await llm_client.chat(
            messages=[{"role": "user", "content": "Responde apenas: ok"}],
            max_tokens=10,
            temperature=0.0,
        )
        llm_online = "ok" in test.lower() or len(test) > 0
    except Exception as e:
        llm_error = str(e)

    return {
        "system": "EU DECIDO — Motor de Agentes",
        "phase": "Fase 1",
        "agents": [
            {"name": "agente_moderacao", "status": "active", "can_block": True},
            {"name": "agente_propostas", "status": "active", "can_block": False},
            {"name": "agente_resumo", "status": "active", "can_block": False},
        ],
        "llm": {
            **info,
            "online": llm_online,
            "error": llm_error,
        },
        "pipelines": {
            "new_proposal": ["agente_moderacao", "agente_propostas", "agente_resumo"],
            "new_comment": ["agente_moderacao"],
            "text_analyze": ["agente_moderacao", "agente_propostas", "agente_resumo"],
        },
    }


@router.post("/analyze")
async def analyze_text(
    body: AnalyzeRequest,
    current_user: dict = Depends(get_current_user),
):
    """
    Analisa texto com o pipeline de agentes sem persistir na base de dados.
    Útil para pré-visualização antes de submeter uma proposta.
    """
    context = {
        "title": body.title,
        "text": body.text,
        "territory_name": body.territory_name,
        "user_id": current_user["id"],
    }

    result = await process(event=body.event, context=context)
    return result.to_dict()


@router.post("/proposal/{proposal_id}/analyze")
async def analyze_proposal(
    proposal_id: str,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    """
    Analisa uma proposta existente e guarda os resultados na base de dados.
    Guarda o resultado na tabela agent_results para auditoria completa.
    """
    row = await conn.fetchrow(
        """
        SELECT p.*, t.name as territory_name
        FROM proposals p
        LEFT JOIN territories t ON t.id = p.territory_id
        WHERE p.id = $1::uuid
        """,
        proposal_id,
    )
    if not row:
        raise HTTPException(status_code=404, detail="Proposta não encontrada.")

    context = {
        "title": row["title"],
        "text": row["description"],
        "territory_name": row["territory_name"] or "",
        "user_id": str(row["user_id"]) if row["user_id"] else "",
    }

    result = await process(
        event="new_proposal",
        context=context,
        entity_id=proposal_id,
    )
    result_dict = result.to_dict()

    # Persistir cada resultado de agente para auditoria
    for agent_result in result.agent_results:
        await conn.execute(
            """
            INSERT INTO agent_results
                (event_type, entity_id, agent_name, action, result)
            VALUES ($1, $2::uuid, $3, $4, $5::jsonb)
            """,
            "new_proposal",
            proposal_id,
            agent_result.agent,
            agent_result.action,
            __import__("json").dumps(agent_result.to_dict()),
        )

    return result_dict


@router.post("/comment/moderate")
async def moderate_comment(
    body: AnalyzeRequest,
    current_user: dict = Depends(get_current_user),
):
    """
    Modera um comentário antes de ser publicado.
    Retorna: blocked=true → comentário não deve ser publicado.
    """
    context = {
        "text": body.text,
        "title": "",
        "user_id": current_user["id"],
    }
    result = await process(event="new_comment", context=context)
    return {
        "blocked": result.blocked,
        "block_reason": result.block_reason,
        "warnings": [w for r in result.agent_results for w in r.warnings],
        "duration_ms": result.duration_ms,
    }


@router.get("/proposal/{proposal_id}/history")
async def agent_history(
    proposal_id: str,
    conn=Depends(get_conn),
):
    """
    Devolve o histórico completo de decisões dos agentes para uma proposta.
    Transparência total e auditável.
    """
    rows = await conn.fetch(
        """
        SELECT id, event_type, agent_name, action, result, created_at
        FROM agent_results
        WHERE entity_id = $1::uuid
        ORDER BY created_at DESC
        """,
        proposal_id,
    )
    return [
        {
            "id": str(r["id"]),
            "event_type": r["event_type"],
            "agent_name": r["agent_name"],
            "action": r["action"],
            "result": __import__("json").loads(r["result"]),
            "created_at": r["created_at"].isoformat(),
        }
        for r in rows
    ]
