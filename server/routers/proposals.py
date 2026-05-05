from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel
from typing import Literal
import uuid

from server.core.database import get_conn
from server.core.dependencies import get_current_user, get_optional_user

router = APIRouter(prefix="/proposals", tags=["Propostas"])


class CreateProposalRequest(BaseModel):
    title: str
    description: str
    territory_id: str | None = None
    type: Literal["IDEIA", "ELECTION_REQUEST"] = "IDEIA"


class UpdateStatusRequest(BaseModel):
    status: Literal["DISCUSSION", "VOTING", "APPROVED", "REJECTED"]


def _fmt(row) -> dict:
    return {
        "id": str(row["id"]),
        "user_id": str(row["user_id"]) if row["user_id"] else None,
        "territory_id": str(row["territory_id"]) if row["territory_id"] else None,
        "title": row["title"],
        "description": row["description"],
        "status": row["status"],
        "type": row["type"],
        "created_at": row["created_at"].isoformat(),
        "updated_at": row["updated_at"].isoformat(),
    }


@router.get("")
async def list_proposals(
    status: str | None = Query(None),
    territory_id: str | None = Query(None),
    limit: int = Query(50, le=200),
    offset: int = Query(0),
    conn=Depends(get_conn),
):
    filters, params = [], []
    if status:
        params.append(status)
        filters.append(f"p.status = ${len(params)}::proposal_status")
    if territory_id:
        params.append(territory_id)
        filters.append(f"p.territory_id = ${len(params)}::uuid")

    where = ("WHERE " + " AND ".join(filters)) if filters else ""
    params += [limit, offset]

    rows = await conn.fetch(
        f"""
        SELECT p.*, u.username
        FROM proposals p
        LEFT JOIN users u ON u.id = p.user_id
        {where}
        ORDER BY p.created_at DESC
        LIMIT ${len(params)-1} OFFSET ${len(params)}
        """,
        *params,
    )
    result = []
    for r in rows:
        d = _fmt(r)
        d["author"] = r["username"]
        result.append(d)
    return result


@router.get("/{proposal_id}")
async def get_proposal(proposal_id: str, conn=Depends(get_conn)):
    row = await conn.fetchrow(
        """
        SELECT p.*, u.username
        FROM proposals p
        LEFT JOIN users u ON u.id = p.user_id
        WHERE p.id = $1::uuid
        """,
        proposal_id,
    )
    if not row:
        raise HTTPException(status_code=404, detail="Proposta não encontrada.")
    d = _fmt(row)
    d["author"] = row["username"]
    return d


@router.post("", status_code=201)
async def create_proposal(
    body: CreateProposalRequest,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    row = await conn.fetchrow(
        """
        INSERT INTO proposals (user_id, territory_id, title, description, type)
        VALUES ($1::uuid, $2::uuid, $3, $4, $5::proposal_type)
        RETURNING *
        """,
        current_user["id"],
        body.territory_id,
        body.title,
        body.description,
        body.type,
    )
    return _fmt(row)


@router.patch("/{proposal_id}/status")
async def update_status(
    proposal_id: str,
    body: UpdateStatusRequest,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    row = await conn.fetchrow(
        """
        UPDATE proposals SET status = $1::proposal_status
        WHERE id = $2::uuid
        RETURNING *
        """,
        body.status,
        proposal_id,
    )
    if not row:
        raise HTTPException(status_code=404, detail="Proposta não encontrada.")
    return _fmt(row)


@router.delete("/{proposal_id}", status_code=204)
async def delete_proposal(
    proposal_id: str,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    row = await conn.fetchrow(
        "SELECT user_id FROM proposals WHERE id = $1::uuid", proposal_id
    )
    if not row:
        raise HTTPException(status_code=404, detail="Proposta não encontrada.")
    if str(row["user_id"]) != current_user["id"]:
        raise HTTPException(status_code=403, detail="Sem permissão para apagar esta proposta.")
    await conn.execute("DELETE FROM proposals WHERE id = $1::uuid", proposal_id)
