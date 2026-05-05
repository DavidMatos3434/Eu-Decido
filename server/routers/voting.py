import hashlib
import secrets
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel
from typing import Literal

from server.core.database import get_conn
from server.core.dependencies import get_current_user

router = APIRouter(tags=["Votação"])


class IssueTokenRequest(BaseModel):
    proposal_id: str | None = None
    election_id: str | None = None


class CastVoteRequest(BaseModel):
    proposal_id: str | None = None
    election_id: str | None = None
    vote_value: str
    token_hash: str


class VoteResponse(BaseModel):
    message: str


def _sha256(value: str) -> str:
    return hashlib.sha256(value.encode()).hexdigest()


@router.post("/voting/token", status_code=201)
async def issue_token(
    body: IssueTokenRequest,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    """
    Emite um token de votação para o utilizador autenticado.
    O token é guardado hasheado — não é possível recuperar o valor original.
    """
    if not body.proposal_id and not body.election_id:
        raise HTTPException(status_code=400, detail="É necessário proposal_id ou election_id.")

    # Verificar se o utilizador já tem um token não usado para esta votação
    existing = await conn.fetchrow(
        """
        SELECT id FROM voting_tokens
        WHERE identity_id = (SELECT identity_id FROM users WHERE id = $1::uuid)
          AND ($2::uuid IS NULL OR proposal_id = $2::uuid)
          AND ($3::uuid IS NULL OR election_id = $3::uuid)
          AND used = false
        """,
        current_user["id"],
        body.proposal_id,
        body.election_id,
    )
    if existing:
        raise HTTPException(status_code=400, detail="Já tens um token de votação activo.")

    # Verificar se já votou (token já usado)
    already_voted = await conn.fetchrow(
        """
        SELECT vt.id FROM voting_tokens vt
        WHERE vt.identity_id = (SELECT identity_id FROM users WHERE id = $1::uuid)
          AND ($2::uuid IS NULL OR vt.proposal_id = $2::uuid)
          AND ($3::uuid IS NULL OR vt.election_id = $3::uuid)
          AND vt.used = true
        """,
        current_user["id"],
        body.proposal_id,
        body.election_id,
    )
    if already_voted:
        raise HTTPException(status_code=400, detail="Já votaste nesta proposta/eleição.")

    raw_token = secrets.token_hex(32)
    token_hash = _sha256(raw_token)

    identity_row = await conn.fetchrow(
        "SELECT identity_id FROM users WHERE id = $1::uuid", current_user["id"]
    )
    identity_id = identity_row["identity_id"] if identity_row else None

    await conn.execute(
        """
        INSERT INTO voting_tokens (identity_id, proposal_id, election_id, token_hash)
        VALUES ($1::uuid, $2::uuid, $3::uuid, $4)
        """,
        identity_id,
        body.proposal_id,
        body.election_id,
        token_hash,
    )

    # Devolvemos o token em claro UMA ÚNICA VEZ — o cliente deve guardá-lo temporariamente
    return {"token": raw_token, "token_hash": token_hash}


@router.post("/vote", response_model=VoteResponse)
async def cast_vote(body: CastVoteRequest, conn=Depends(get_conn)):
    """
    Regista um voto anónimo.
    Não requer autenticação aqui — o token é a prova de elegibilidade.
    O token é queimado atomicamente antes de inserir o voto (sem race condition).
    """
    if not body.proposal_id and not body.election_id:
        raise HTTPException(status_code=400, detail="É necessário proposal_id ou election_id.")

    token_hash = _sha256(body.token_hash) if len(body.token_hash) != 64 else body.token_hash

    async with conn.transaction():
        # Queimar o token atomicamente (UPDATE condicional)
        burned = await conn.fetchrow(
            """
            UPDATE voting_tokens
            SET used = true
            WHERE token_hash = $1
              AND ($2::uuid IS NULL OR proposal_id = $2::uuid)
              AND ($3::uuid IS NULL OR election_id = $3::uuid)
              AND used = false
            RETURNING id
            """,
            body.token_hash,
            body.proposal_id,
            body.election_id,
        )

        if not burned:
            raise HTTPException(status_code=400, detail="Token de votação inválido ou já utilizado.")

        # Inserir o voto anónimo — NUNCA contém user_id
        await conn.execute(
            """
            INSERT INTO votes (proposal_id, election_id, vote_value, voting_token_hash)
            VALUES ($1::uuid, $2::uuid, $3, $4)
            """,
            body.proposal_id,
            body.election_id,
            body.vote_value,
            body.token_hash,
        )

    return VoteResponse(message="Voto registado com sucesso e anonimizado.")


@router.get("/results")
async def get_results(
    proposal_id: str | None = None,
    election_id: str | None = None,
    conn=Depends(get_conn),
):
    if not proposal_id and not election_id:
        raise HTTPException(status_code=400, detail="É necessário proposal_id ou election_id.")

    if proposal_id:
        rows = await conn.fetch(
            "SELECT vote_value FROM votes WHERE proposal_id = $1::uuid",
            proposal_id,
        )
        total = len(rows)
        yes = sum(1 for r in rows if r["vote_value"] == "SIM")
        no = sum(1 for r in rows if r["vote_value"] == "NAO")
        abs_ = sum(1 for r in rows if r["vote_value"] == "ABSTENCAO")

        yes_p = round((yes / total) * 100) if total > 0 else 0
        no_p = round((no / total) * 100) if total > 0 else 0
        abs_p = (100 - yes_p - no_p) if total > 0 else 0

        return {
            "type": "PROPOSAL",
            "total": total,
            "stats": {"yes": yes, "no": no, "abstention": abs_},
            "percentage": {"yes": yes_p, "no": no_p, "abstention": abs_p},
        }

    rows = await conn.fetch(
        "SELECT vote_value FROM votes WHERE election_id = $1::uuid",
        election_id,
    )
    counts: dict = {}
    for r in rows:
        counts[r["vote_value"]] = counts.get(r["vote_value"], 0) + 1

    return {"type": "ELECTION", "total": len(rows), "candidates": counts}
