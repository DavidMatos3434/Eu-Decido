import hashlib
from fastapi import APIRouter, Depends, HTTPException
from pydantic import BaseModel

from server.core.database import get_conn
from server.core.dependencies import get_current_user

router = APIRouter(prefix="/identity", tags=["Identidade"])


class ValidateIdentityRequest(BaseModel):
    nif: str
    phone: str | None = None


def _sha256(value: str) -> str:
    return hashlib.sha256(value.encode()).hexdigest()


@router.post("/validate", status_code=201)
async def validate_identity(
    body: ValidateIdentityRequest,
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    if not body.nif or len(body.nif) != 9 or not body.nif.isdigit():
        raise HTTPException(status_code=400, detail="NIF inválido. Deve ter 9 dígitos.")

    nif_hash = _sha256(body.nif)
    phone_hash = _sha256(body.phone) if body.phone else None

    # Verificar se este NIF já está registado por outro utilizador
    existing = await conn.fetchrow(
        "SELECT id FROM identity WHERE nif_hash = $1", nif_hash
    )
    if existing:
        raise HTTPException(status_code=400, detail="Este NIF já está associado a outra conta.")

    identity_row = await conn.fetchrow(
        """
        INSERT INTO identity (nif_hash, phone_hash, verified)
        VALUES ($1, $2, true)
        RETURNING id
        """,
        nif_hash,
        phone_hash,
    )
    identity_id = identity_row["id"]

    # Associar ao utilizador
    await conn.execute(
        "UPDATE users SET identity_id = $1::uuid WHERE id = $2::uuid",
        identity_id,
        current_user["id"],
    )

    return {"identity_id": str(identity_id), "status": "verified"}


@router.get("/status")
async def get_identity_status(
    conn=Depends(get_conn),
    current_user: dict = Depends(get_current_user),
):
    row = await conn.fetchrow(
        """
        SELECT i.id, i.verified, i.created_at
        FROM users u
        LEFT JOIN identity i ON i.id = u.identity_id
        WHERE u.id = $1::uuid
        """,
        current_user["id"],
    )
    if not row or not row["id"]:
        return {"verified": False, "identity_id": None}
    return {
        "verified": row["verified"],
        "identity_id": str(row["id"]),
        "verified_at": row["created_at"].isoformat(),
    }
