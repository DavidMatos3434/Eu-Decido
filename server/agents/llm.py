"""
Cliente LLM universal — compatível com Ollama (Mistral local), Mistral API e OpenAI.
Configuração via variáveis de ambiente:
  LLM_PROVIDER  = "ollama" | "mistral" | "openai"  (default: ollama)
  LLM_BASE_URL  = URL base da API  (default: http://localhost:11434/v1)
  LLM_MODEL     = nome do modelo  (default: mistral)
  OPENAI_API_KEY / MISTRAL_API_KEY = chave de API (se necessário)
"""
import json
import os
from typing import Any

import httpx

PROVIDER = os.environ.get("LLM_PROVIDER", "ollama")
BASE_URL = os.environ.get(
    "LLM_BASE_URL",
    {
        "ollama": "http://localhost:11434/v1",
        "mistral": "https://api.mistral.ai/v1",
        "openai": "https://api.openai.com/v1",
    }.get(PROVIDER, "http://localhost:11434/v1"),
)
DEFAULT_MODEL = os.environ.get(
    "LLM_MODEL",
    {
        "ollama": "mistral",
        "mistral": "mistral-small-latest",
        "openai": "gpt-4o-mini",
    }.get(PROVIDER, "mistral"),
)
API_KEY = os.environ.get("MISTRAL_API_KEY") or os.environ.get("OPENAI_API_KEY") or "ollama"

TIMEOUT = float(os.environ.get("LLM_TIMEOUT", "60"))


async def chat(
    messages: list[dict],
    model: str | None = None,
    temperature: float = 0.3,
    max_tokens: int = 1024,
    json_mode: bool = False,
) -> str:
    """
    Envia uma conversa para o LLM e devolve o texto da resposta.
    Usa o formato OpenAI-compatible (funciona com Ollama, Mistral API e OpenAI).
    """
    payload: dict[str, Any] = {
        "model": model or DEFAULT_MODEL,
        "messages": messages,
        "temperature": temperature,
        "max_tokens": max_tokens,
    }
    if json_mode:
        payload["response_format"] = {"type": "json_object"}

    headers = {
        "Content-Type": "application/json",
        "Authorization": f"Bearer {API_KEY}",
    }

    async with httpx.AsyncClient(timeout=TIMEOUT) as client:
        resp = await client.post(
            f"{BASE_URL}/chat/completions",
            json=payload,
            headers=headers,
        )
        resp.raise_for_status()
        data = resp.json()
        return data["choices"][0]["message"]["content"]


async def chat_json(messages: list[dict], **kwargs) -> dict:
    """Envia conversa e faz parse do JSON devolvido."""
    kwargs["json_mode"] = True
    raw = await chat(messages, **kwargs)
    try:
        return json.loads(raw)
    except json.JSONDecodeError:
        import re
        match = re.search(r"\{.*\}", raw, re.DOTALL)
        if match:
            return json.loads(match.group())
        return {"error": "parse_failed", "raw": raw}


def is_available() -> bool:
    return bool(BASE_URL)


def provider_info() -> dict:
    return {
        "provider": PROVIDER,
        "base_url": BASE_URL,
        "model": DEFAULT_MODEL,
        "authenticated": API_KEY != "ollama",
    }
