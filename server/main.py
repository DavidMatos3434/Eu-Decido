import os
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from server.core.database import close_pool, get_pool
from server.routers import (
    auth,
    comments,
    elections,
    identity,
    notifications,
    proposals,
    representatives,
    territories,
    voting,
)


@asynccontextmanager
async def lifespan(app: FastAPI):
    await get_pool()
    yield
    await close_pool()


app = FastAPI(
    title="EU DECIDO — API",
    description="Backend local para desenvolvimento e testes da plataforma EU DECIDO.",
    version="1.0.0",
    lifespan=lifespan,
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(proposals.router)
app.include_router(comments.router)
app.include_router(voting.router)
app.include_router(elections.router)
app.include_router(territories.router)
app.include_router(representatives.router)
app.include_router(notifications.router)
app.include_router(identity.router)


@app.get("/", tags=["Estado"])
async def root():
    return {
        "app": "EU DECIDO API",
        "version": "1.0.0",
        "status": "online",
        "docs": "/docs",
    }


@app.get("/health", tags=["Estado"])
async def health():
    try:
        pool = await get_pool()
        async with pool.acquire() as conn:
            await conn.fetchval("SELECT 1")
        return {"status": "ok", "database": "connected"}
    except Exception as e:
        return JSONResponse(status_code=503, content={"status": "error", "detail": str(e)})
