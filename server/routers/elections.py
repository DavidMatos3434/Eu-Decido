from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel
from typing import Literal

from server.core.database import get_conn
from server.core.dependencies import get_current_user

router = APIRouter(prefix="/elections", tags=["Eleições"])


class CreateElectionRequest(BaseModel):
    proposal_id: str | None = None
    title: str
    territory_id: str | None = None
    role: str


class CreateCandidacyRequest(BaseModel):
    manifesto: str | None = None


def _fmt_election(row) -> dict:
    return {
        "id": str(row["id"]),
        "proposal_id": str(row["proposal_id"]) if row["proposal_id"] else None,
        "title": row["title"],
        "territory_id": str(row["territory_id"]) if row["territory_id"] else None,
        "role": row["role"],
        "status": row["status"],
        "created_at": row["created_at"].isoformat(),
        "updated_at": row["updated_at"].isoformat(),
    }


def _fmt_candidacy(row) -> dict:
    return {
        "id": str(row["id"]),
        "election_id": str(row["election_id"]),
        "user_id": str(row["user_id"]),
        "username": row.get("username"),
        "status": row["status"],
        "manifesto": row["manifesto"],
        "created_at": row["created_at"].isoformat(),
    }


@router.get("")
async def list_elections(
    status: str | None = Query(None),
    territory_id: str | None = Query(None),
    conn=Depends(get_conn),
):
    filters, params = [], []
    if status:
        params.append(status)
        filters.append(f"status = ${len(params)}::election_status")
    if territory_id:
        params.append(territory_id)
        filters.append(f"territory_id = ${len(params)}::uuid")

    where = ("WHERE " + " AND ".join(filters)) if filters else ""
    rows = await conn.fetch(
        f"SELECT * FROM elections {where} ORDER BY created_at DESC", *params
    )
    return [_fmt_election(r) for r in rows]


@router.get("/{election_id}")
async def get_election(election_id: str, conn=Depends(get_conn)):
    row = await conn.fetchrow(
        "SELECT * FROM elections WHERE id = $1::uuid", election_id
    )
    if not row:
        raise HTTPException(status_code=404, detail="Eleição não encontrada.")
    return _fmt_election(row)


@router.post("", status_code=201)
async def create_election(
    body: CreateElectionRequest,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    row = await conn.fetchrow(
        """
        INSERT INTO elections (proposal_id, title, territory_id, role)
        VALUES ($1::uuid, $2, $3::uuid, $4)
        RETURNING *
        """,
        body.proposal_id,
        body.title,
        body.territory_id,
        body.role,
    )
    return _fmt_election(row)


@router.patch("/{election_id}/status")
async def update_election_status(
    election_id: str,
    status: Literal["DRAFT", "OPEN", "VOTING", "CLOSED"],
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    row = await conn.fetchrow(
        """
        UPDATE elections SET status = $1::election_status
        WHERE id = $2::uuid RETURNING *
        """,
        status,
        election_id,
    )
    if not row:
        raise HTTPException(status_code=404, detail="Eleição não encontrada.")
    return _fmt_election(row)


@router.get("/{election_id}/candidacies")
async def list_candidacies(election_id: str, conn=Depends(get_conn)):
    rows = await conn.fetch(
        """
        SELECT c.*, u.username
        FROM candidacies c
        LEFT JOIN users u ON u.id = c.user_id
        WHERE c.election_id = $1::uuid
        ORDER BY c.created_at ASC
        """,
        election_id,
    )
    return [_fmt_candidacy(r) for r in rows]


@router.post("/{election_id}/candidacies", status_code=201)
async def apply_candidacy(
    election_id: str,
    body: CreateCandidacyRequest,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    election = await conn.fetchrow(
        "SELECT id, status FROM elections WHERE id = $1::uuid", election_id
    )
    if not election:
        raise HTTPException(status_code=404, detail="Eleição não encontrada.")
    if election["status"] not in ("OPEN",):
        raise HTTPException(status_code=400, detail="Candidaturas encerradas.")

    existing = await conn.fetchrow(
        "SELECT id FROM candidacies WHERE election_id = $1::uuid AND user_id = $2::uuid",
        election_id,
        current_user["id"],
    )
    if existing:
        raise HTTPException(status_code=400, detail="Já te candidataste a esta eleição.")

    row = await conn.fetchrow(
        """
        INSERT INTO candidacies (election_id, user_id, manifesto)
        VALUES ($1::uuid, $2::uuid, $3)
        RETURNING *
        """,
        election_id,
        current_user["id"],
        body.manifesto,
    )
    result = _fmt_candidacy(row)
    result["username"] = current_user["username"]
    return result
