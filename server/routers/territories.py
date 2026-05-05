from fastapi import APIRouter, Depends, HTTPException, Query
from pydantic import BaseModel
from typing import Literal

from server.core.database import get_conn
from server.core.dependencies import get_current_user

router = APIRouter(prefix="/territories", tags=["Territórios"])


class CreateTerritoryRequest(BaseModel):
    name: str
    type: Literal["FREGUESIA", "MUNICIPIO", "REGIAO", "NACIONAL"]
    parent_id: str | None = None


def _fmt(row) -> dict:
    return {
        "id": str(row["id"]),
        "name": row["name"],
        "type": row["type"],
        "parent_id": str(row["parent_id"]) if row["parent_id"] else None,
    }


@router.get("")
async def list_territories(
    type: str | None = Query(None),
    parent_id: str | None = Query(None),
    conn=Depends(get_conn),
):
    filters, params = [], []
    if type:
        params.append(type)
        filters.append(f"type = ${len(params)}::territory_type")
    if parent_id:
        params.append(parent_id)
        filters.append(f"parent_id = ${len(params)}::uuid")

    where = ("WHERE " + " AND ".join(filters)) if filters else ""
    rows = await conn.fetch(
        f"SELECT * FROM territories {where} ORDER BY name ASC", *params
    )
    return [_fmt(r) for r in rows]


@router.get("/{territory_id}")
async def get_territory(territory_id: str, conn=Depends(get_conn)):
    row = await conn.fetchrow(
        "SELECT * FROM territories WHERE id = $1::uuid", territory_id
    )
    if not row:
        raise HTTPException(status_code=404, detail="Território não encontrado.")
    return _fmt(row)


@router.get("/{territory_id}/proposals")
async def get_territory_proposals(territory_id: str, conn=Depends(get_conn)):
    rows = await conn.fetch(
        """
        SELECT p.*, u.username
        FROM proposals p
        LEFT JOIN users u ON u.id = p.user_id
        WHERE p.territory_id = $1::uuid
        ORDER BY p.created_at DESC
        """,
        territory_id,
    )
    result = []
    for r in rows:
        d = {
            "id": str(r["id"]),
            "title": r["title"],
            "description": r["description"],
            "status": r["status"],
            "type": r["type"],
            "author": r["username"],
            "created_at": r["created_at"].isoformat(),
        }
        result.append(d)
    return result
