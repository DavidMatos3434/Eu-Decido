"""
Agente de Resumo
================
Gera resumos em múltiplos formatos a partir de uma proposta.
Formatos:
  - tldr: 1-2 frases para o público geral
  - technical: versão técnica para especialistas e representantes
  - local_impact: impacto esperado na comunidade local
  - accessibility: versão em linguagem simples (nível B1)

NUNCA bloqueia — apenas gera conteúdo de valor.
"""
import time

from server.agents.base import AgentResult, BaseAgent
from server.agents import llm

SYSTEM_PROMPT = """És o Agente de Resumo do EU DECIDO, uma plataforma de governação democrática portuguesa.
A tua missão é criar resumos acessíveis de propostas cívicas para diferentes audiências.

Gera um JSON com:
{
  "tldr": "1-2 frases para qualquer cidadão entender",
  "technical": "análise técnica em 3-5 frases para especialistas e representantes",
  "local_impact": "impacto esperado na vida quotidiana da comunidade local",
  "accessibility": "explicação em linguagem simples (nível secundário), sem jargão",
  "keywords": ["palavra-chave 1", "palavra-chave 2", "palavra-chave 3"]
}

Sê conciso, neutro e factual. Não adds opiniões sobre se a proposta é boa ou má.
Responde sempre em português de Portugal."""


class SummaryAgent(BaseAgent):
    name = "agente_resumo"
    description = "Gera resumos em múltiplos formatos: TLDR, técnico, impacto local, linguagem simples."

    async def run(self, context: dict) -> AgentResult:
        start = time.monotonic()
        title = context.get("title", "")
        text = context.get("text", "") or context.get("description", "")
        territory = context.get("territory_name", "")

        if not title and not text:
            return self._result(
                "pass",
                {"message": "Sem conteúdo suficiente para resumir."},
                duration_ms=0,
            )

        content = f"Título: {title}\n\nDescrição: {text}"
        if territory:
            content += f"\n\nTerritório: {territory}"

        try:
            result = await llm.chat_json(
                messages=[
                    {"role": "system", "content": SYSTEM_PROMPT},
                    {"role": "user", "content": content[:3000]},
                ],
                temperature=0.5,
                max_tokens=700,
            )

            duration = int((time.monotonic() - start) * 1000)

            return self._result(
                "enrich",
                {
                    "tldr": result.get("tldr", ""),
                    "technical": result.get("technical", ""),
                    "local_impact": result.get("local_impact", ""),
                    "accessibility": result.get("accessibility", ""),
                    "keywords": result.get("keywords", []),
                },
                audit={"input_length": len(content)},
                duration_ms=duration,
            )

        except Exception as e:
            duration = int((time.monotonic() - start) * 1000)
            return self._result(
                "pass",
                {"message": "Resumo temporariamente indisponível."},
                warnings=["O agente de resumo está temporariamente indisponível."],
                audit={"error": str(e)},
                duration_ms=duration,
            )
