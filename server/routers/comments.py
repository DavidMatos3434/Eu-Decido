from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from server.core.database import get_conn
from server.core.dependencies import get_current_user

router = APIRouter(tags=["Comentários"])


class CreateCommentRequest(BaseModel):
    content: str


def _fmt(row) -> dict:
    return {
        "id": str(row["id"]),
        "proposal_id": str(row["proposal_id"]),
        "user_id": str(row["user_id"]) if row["user_id"] else None,
        "author": row.get("username") or "Utilizador",
        "content": row["content"],
        "created_at": row["created_at"].isoformat(),
    }


@router.get("/proposals/{proposal_id}/comments")
async def list_comments(proposal_id: str, conn=Depends(get_conn)):
    rows = await conn.fetch(
        """
        SELECT c.*, u.username
        FROM comments c
        LEFT JOIN users u ON u.id = c.user_id
        WHERE c.proposal_id = $1::uuid
        ORDER BY c.created_at ASC
        """,
        proposal_id,
    )
    return [_fmt(r) for r in rows]


@router.post("/proposals/{proposal_id}/comments", status_code=201)
async def add_comment(
    proposal_id: str,
    body: CreateCommentRequest,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    if not body.content.strip():
        raise HTTPException(status_code=400, detail="O comentário não pode estar vazio.")

    proposal = await conn.fetchrow(
        "SELECT id FROM proposals WHERE id = $1::uuid", proposal_id
    )
    if not proposal:
        raise HTTPException(status_code=404, detail="Proposta não encontrada.")

    row = await conn.fetchrow(
        """
        INSERT INTO comments (proposal_id, user_id, content)
        VALUES ($1::uuid, $2::uuid, $3)
        RETURNING *
        """,
        proposal_id,
        current_user["id"],
        body.content.strip(),
    )
    result = _fmt(row)
    result["author"] = current_user["username"]
    return result
