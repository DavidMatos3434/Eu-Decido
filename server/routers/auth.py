from fastapi import APIRouter, Depends, HTTPException, status
from pydantic import BaseModel, EmailStr
import asyncpg

from server.core.database import get_conn
from server.core.security import create_access_token, hash_password, verify_password

router = APIRouter(prefix="/auth", tags=["Autenticação"])


class RegisterRequest(BaseModel):
    username: str
    email: EmailStr
    password: str
    nif_hash: str | None = None


class LoginRequest(BaseModel):
    email: EmailStr
    password: str


class AuthResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: str
    username: str
    email: str


@router.post("/register", response_model=AuthResponse, status_code=status.HTTP_201_CREATED)
async def register(body: RegisterRequest, conn=Depends(get_conn)):
    existing = await conn.fetchrow("SELECT id FROM users WHERE email = $1", body.email)
    if existing:
        raise HTTPException(status_code=400, detail="Email já registado.")

    existing_user = await conn.fetchrow("SELECT id FROM users WHERE username = $1", body.username)
    if existing_user:
        raise HTTPException(status_code=400, detail="Nome de utilizador já existe.")

    password_hash = hash_password(body.password)

    try:
        row = await conn.fetchrow(
            """
            INSERT INTO users (username, email, password_hash, identity_id)
            VALUES ($1, $2, $3, $4)
            RETURNING id, username, email
            """,
            body.username,
            body.email,
            password_hash,
            None,
        )
    except asyncpg.UniqueViolationError:
        raise HTTPException(status_code=400, detail="Utilizador já existe.")

    token = create_access_token({"sub": str(row["id"])})
    return AuthResponse(
        access_token=token,
        user_id=str(row["id"]),
        username=row["username"],
        email=row["email"],
    )


@router.post("/login", response_model=AuthResponse)
async def login(body: LoginRequest, conn=Depends(get_conn)):
    row = await conn.fetchrow(
        "SELECT id, username, email, password_hash FROM users WHERE email = $1",
        body.email,
    )
    if not row or not verify_password(body.password, row["password_hash"]):
        raise HTTPException(status_code=401, detail="Email ou palavra-passe incorretos.")

    token = create_access_token({"sub": str(row["id"])})
    return AuthResponse(
        access_token=token,
        user_id=str(row["id"]),
        username=row["username"],
        email=row["email"],
    )


@router.get("/me")
async def me(conn=Depends(get_conn), current_user: dict = Depends(__import__("server.core.dependencies", fromlist=["get_current_user"]).get_current_user)):
    return current_user
