"""
Corre as migrações do schema na base de dados do Replit.
Uso: python server/migrate.py
"""
import asyncio
import os
from pathlib import Path
import asyncpg


async def main():
    sql = Path("server/schema.sql").read_text()
    conn = await asyncpg.connect(os.environ["DATABASE_URL"])
    try:
        await conn.execute(sql)
        print("✅ Schema aplicado com sucesso.")
    except Exception as e:
        print(f"❌ Erro ao aplicar schema: {e}")
        raise
    finally:
        await conn.close()


if __name__ == "__main__":
    asyncio.run(main())
