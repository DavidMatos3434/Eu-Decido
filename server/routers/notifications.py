from fastapi import APIRouter, Depends

from server.core.database import get_conn
from server.core.dependencies import get_current_user

router = APIRouter(prefix="/notifications", tags=["Notificações"])


def _fmt(row) -> dict:
    return {
        "id": str(row["id"]),
        "title": row["title"],
        "message": row["message"],
        "is_read": row["is_read"],
        "created_at": row["created_at"].isoformat(),
    }


@router.get("")
async def list_notifications(
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    rows = await conn.fetch(
        """
        SELECT * FROM notifications
        WHERE user_id = $1::uuid
        ORDER BY created_at DESC
        LIMIT 100
        """,
        current_user["id"],
    )
    return [_fmt(r) for r in rows]


@router.patch("/{notification_id}/read")
async def mark_as_read(
    notification_id: str,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    await conn.execute(
        """
        UPDATE notifications SET is_read = true
        WHERE id = $1::uuid AND user_id = $2::uuid
        """,
        notification_id,
        current_user["id"],
    )
    return {"ok": True}


@router.patch("/read-all")
async def mark_all_read(
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    await conn.execute(
        "UPDATE notifications SET is_read = true WHERE user_id = $1::uuid",
        current_user["id"],
    )
    return {"ok": True}
