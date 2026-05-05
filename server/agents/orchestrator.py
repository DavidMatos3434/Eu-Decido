"""
Orquestrador de Agentes EU DECIDO
===================================
Ponto único de entrada para todos os agentes.
Princípios:
  - Roteamento determinístico por tipo de evento (não depende de LLM para decidir)
  - O único agente que pode BLOQUEAR é o Agente de Moderação
  - Todos os outros agentes ENRIQUECEM ou AVISAM — nunca bloqueiam
  - Registo de auditoria completo de todas as decisões

Eventos suportados (Fase 1):
  - "new_proposal"  → Moderação → [Propostas + Resumo] em paralelo
  - "new_comment"   → Moderação apenas
  - "text_analyze"  → Moderação → [Propostas + Resumo] em paralelo (sem persistência)
"""
import asyncio
import time
from datetime import datetime, timezone
from typing import Any

from server.agents.base import AgentResult
from server.agents.moderation import ModerationAgent
from server.agents.proposals import ProposalsAgent
from server.agents.summary import SummaryAgent

_moderation = ModerationAgent()
_proposals = ProposalsAgent()
_summary = SummaryAgent()

EVENT_PIPELINES: dict[str, list[str]] = {
    "new_proposal": ["moderation", "proposals", "summary"],
    "new_comment": ["moderation"],
    "text_analyze": ["moderation", "proposals", "summary"],
}


class OrchestratorResult:
    def __init__(
        self,
        event: str,
        entity_id: str | None,
        blocked: bool,
        block_reason: str | None,
        agent_results: list[AgentResult],
        duration_ms: int,
    ):
        self.event = event
        self.entity_id = entity_id
        self.blocked = blocked
        self.block_reason = block_reason
        self.agent_results = agent_results
        self.duration_ms = duration_ms
        self.timestamp = datetime.now(timezone.utc).isoformat()

    def to_dict(self) -> dict[str, Any]:
        return {
            "event": self.event,
            "entity_id": self.entity_id,
            "blocked": self.blocked,
            "block_reason": self.block_reason,
            "duration_ms": self.duration_ms,
            "timestamp": self.timestamp,
            "agents": [r.to_dict() for r in self.agent_results],
            "warnings": [
                w for r in self.agent_results for w in r.warnings
            ],
            "enrichments": {
                r.agent: r.data
                for r in self.agent_results
                if r.action in ("enrich", "warn") and r.agent != "agente_moderacao"
            },
        }


async def process(
    event: str,
    context: dict,
    entity_id: str | None = None,
) -> OrchestratorResult:
    """
    Processa um evento com os agentes configurados para esse tipo.

    context deve conter:
      - text / description: texto principal
      - title: título (opcional)
      - territory_name: nome do território (opcional)
      - user_id: utilizador que originou o evento (opcional)
    """
    start = time.monotonic()
    pipeline = EVENT_PIPELINES.get(event, ["moderation"])
    results: list[AgentResult] = []

    # 1. Moderação SEMPRE corre primeiro e de forma síncrona
    if "moderation" in pipeline:
        mod_result = await _moderation.run(context)
        results.append(mod_result)

        if mod_result.blocked:
            duration = int((time.monotonic() - start) * 1000)
            return OrchestratorResult(
                event=event,
                entity_id=entity_id,
                blocked=True,
                block_reason=mod_result.data.get("message", "Conteúdo bloqueado."),
                agent_results=results,
                duration_ms=duration,
            )

    # 2. Agentes de enriquecimento correm em paralelo (nunca bloqueiam)
    parallel_agents = [a for a in pipeline if a != "moderation"]
    if parallel_agents:
        tasks = []
        if "proposals" in parallel_agents:
            tasks.append(_proposals.run(context))
        if "summary" in parallel_agents:
            tasks.append(_summary.run(context))

        parallel_results = await asyncio.gather(*tasks, return_exceptions=True)

        for res in parallel_results:
            if isinstance(res, Exception):
                from server.agents.base import AgentResult
                results.append(
                    AgentResult(
                        agent="unknown",
                        action="error",
                        data={"error": str(res)},
                        duration_ms=0,
                    )
                )
            else:
                results.append(res)

    duration = int((time.monotonic() - start) * 1000)
    return OrchestratorResult(
        event=event,
        entity_id=entity_id,
        blocked=False,
        block_reason=None,
        agent_results=results,
        duration_ms=duration,
    )
