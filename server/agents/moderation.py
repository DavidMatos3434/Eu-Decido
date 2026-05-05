"""
Agente de Moderação Avançada
============================
ÚNICO agente que pode BLOQUEAR conteúdo.
Deteta: discurso de ódio, racismo, homofobia, insultos graves, ameaças.
Para qualquer outra questão ética/legal → usa avisos, nunca bloqueio.

Abordagem híbrida:
  1. Filtro de palavras/padrões (rápido, determinístico, sem LLM)
  2. Análise LLM para casos ambíguos
"""
import re
import time

from server.agents.base import AgentResult, BaseAgent
from server.agents import llm

BLOCKED_PATTERNS = [
    r"\b(filho\s*da\s*puta|fdp|caralho|merda|porra|paneleiro|bicha|viado|preto\s*de\s*merda|cigano\s*de\s*merda)\b",
    r"\b(mata[r]?\s+os|extermina[r]?\s+os|morte\s+aos|fora\s+os\s+\w+)\b",
    r"\b(nazismo|hitler|heil)\b",
]
COMPILED = [re.compile(p, re.IGNORECASE | re.UNICODE) for p in BLOCKED_PATTERNS]

SYSTEM_PROMPT = """És o Agente de Moderação do sistema EU DECIDO, uma plataforma de governação democrática.
A tua missão é identificar conteúdo claramente inaceitável: discurso de ódio, insultos graves,
ameaças directas, racismo explícito, homofobia explícita.

REGRA CRÍTICA: Nunca bloqueies opiniões políticas, mesmo que controversas.
Só bloqueias o que é claramente ilegal ou viola direitos fundamentais.

Responde SEMPRE em JSON com este formato exacto:
{
  "should_block": true/false,
  "confidence": 0.0-1.0,
  "reason": "explicação breve",
  "categories": ["hate_speech" | "threats" | "discrimination" | "spam" | "none"],
  "severity": "none" | "low" | "medium" | "high"
}"""


class ModerationAgent(BaseAgent):
    name = "agente_moderacao"
    description = "Deteta e bloqueia conteúdo com discurso de ódio, insultos graves ou ameaças."

    async def run(self, context: dict) -> AgentResult:
        start = time.monotonic()
        text = context.get("text", "") + " " + context.get("title", "")
        text = text.strip()

        if not text:
            return self._result("pass", {"message": "Sem conteúdo para moderar."})

        # 1. Filtro determinístico rápido
        for pattern in COMPILED:
            if pattern.search(text):
                duration = int((time.monotonic() - start) * 1000)
                return self._result(
                    "block",
                    {
                        "message": "O conteúdo contém linguagem imprópria ou discurso de ódio.",
                        "categories": ["hate_speech"],
                        "severity": "high",
                        "method": "pattern_match",
                    },
                    audit={"pattern_triggered": True},
                    duration_ms=duration,
                )

        # 2. Análise LLM para casos ambíguos
        try:
            result = await llm.chat_json(
                messages=[
                    {"role": "system", "content": SYSTEM_PROMPT},
                    {"role": "user", "content": f"Analisa este conteúdo:\n\n{text[:2000]}"},
                ],
                temperature=0.1,
                max_tokens=256,
            )

            duration = int((time.monotonic() - start) * 1000)

            should_block = result.get("should_block", False)
            confidence = float(result.get("confidence", 0.0))
            severity = result.get("severity", "none")

            # Só bloqueamos se LLM tem alta confiança e severidade alta
            if should_block and confidence >= 0.85 and severity == "high":
                return self._result(
                    "block",
                    {
                        "message": f"Conteúdo bloqueado: {result.get('reason', 'violação detectada')}",
                        "categories": result.get("categories", []),
                        "severity": severity,
                        "confidence": confidence,
                        "method": "llm_analysis",
                    },
                    audit={"llm_raw": result},
                    duration_ms=duration,
                )
            elif should_block and confidence >= 0.6:
                return self._result(
                    "warn",
                    {
                        "message": f"Conteúdo sinalizado para revisão: {result.get('reason', '')}",
                        "categories": result.get("categories", []),
                        "severity": severity,
                        "confidence": confidence,
                        "method": "llm_analysis",
                    },
                    warnings=["Este conteúdo foi sinalizado para revisão manual."],
                    audit={"llm_raw": result},
                    duration_ms=duration,
                )
            else:
                return self._result(
                    "pass",
                    {"message": "Conteúdo aprovado.", "method": "llm_analysis"},
                    audit={"llm_raw": result},
                    duration_ms=duration,
                )

        except Exception as e:
            # Se LLM falha, não bloqueia (fail-open para não censurar)
            duration = int((time.monotonic() - start) * 1000)
            return self._result(
                "pass",
                {"message": "Moderação LLM indisponível — aprovação automática."},
                warnings=["Moderação avançada temporariamente indisponível."],
                audit={"error": str(e)},
                duration_ms=duration,
            )
