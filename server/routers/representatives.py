from fastapi import APIRouter, Depends, Query

from server.core.database import get_conn

router = APIRouter(prefix="/representatives", tags=["Representantes"])


def _fmt(row) -> dict:
    return {
        "id": str(row["id"]),
        "user_id": str(row["user_id"]),
        "username": row.get("username"),
        "territory_id": str(row["territory_id"]) if row["territory_id"] else None,
        "territory_name": row.get("territory_name"),
        "election_id": str(row["election_id"]) if row["election_id"] else None,
        "role": row["role"],
        "created_at": row["created_at"].isoformat(),
    }


@router.get("")
async def list_representatives(
    territory_id: str | None = Query(None),
    conn=Depends(get_conn),
):
    params = []
    where = ""
    if territory_id:
        params.append(territory_id)
        where = "WHERE r.territory_id = $1::uuid"

    rows = await conn.fetch(
        f"""
        SELECT r.*, u.username, t.name AS territory_name
        FROM representatives r
        LEFT JOIN users u ON u.id = r.user_id
        LEFT JOIN territories t ON t.id = r.territory_id
        {where}
        ORDER BY r.created_at DESC
        """,
        *params,
    )
    return [_fmt(r) for r in rows]
