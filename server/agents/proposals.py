"""
Agente de Propostas
===================
Melhora, estrutura e valida propostas dos cidadãos.
Sugere melhorias de clareza, identifica lacunas, propõe estrutura.
NUNCA bloqueia — apenas enriquece com sugestões.

Acções:
  - Avalia completude da proposta (título, problema, solução, impacto)
  - Sugere reformulações mais claras
  - Identifica perguntas que o autor devia responder
  - Classifica o tema principal
"""
import time

from server.agents.base import AgentResult, BaseAgent
from server.agents import llm

SYSTEM_PROMPT = """És o Agente de Propostas do EU DECIDO, uma plataforma de governação democrática portuguesa.
A tua missão é ajudar os cidadãos a estruturarem melhor as suas propostas, nunca as censurar.

Analisa a proposta e devolve um JSON com:
{
  "score": 0-100,
  "theme": "mobilidade | ambiente | habitação | saúde | educação | segurança | cultura | economia | outro",
  "completeness": {
    "has_problem": true/false,
    "has_solution": true/false,
    "has_impact": true/false,
    "has_territory": true/false
  },
  "suggestions": ["sugestão 1", "sugestão 2"],
  "questions_for_author": ["pergunta para clarificar aspecto X"],
  "improved_title": "título melhorado (ou null se o original está bom)",
  "improved_summary": "resumo melhorado em 2-3 frases (ou null)",
  "tags": ["tag1", "tag2"]
}

Score 0-100: 0-40 = incompleta, 41-70 = razoável, 71-100 = bem estruturada.
Responde sempre em português de Portugal."""


class ProposalsAgent(BaseAgent):
    name = "agente_propostas"
    description = "Melhora e estrutura propostas dos cidadãos com sugestões construtivas."

    async def run(self, context: dict) -> AgentResult:
        start = time.monotonic()
        title = context.get("title", "")
        text = context.get("text", "") or context.get("description", "")
        territory = context.get("territory_name", "")

        if not title and not text:
            return self._result(
                "pass",
                {"message": "Sem conteúdo suficiente para analisar."},
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
                temperature=0.4,
                max_tokens=800,
            )

            duration = int((time.monotonic() - start) * 1000)

            score = int(result.get("score", 50))
            warnings = []
            if score < 40:
                warnings.append(
                    "Esta proposta está incompleta. Considera responder às sugestões antes de submeter para votação."
                )

            return self._result(
                "enrich",
                {
                    "score": score,
                    "theme": result.get("theme", "outro"),
                    "completeness": result.get("completeness", {}),
                    "suggestions": result.get("suggestions", []),
                    "questions_for_author": result.get("questions_for_author", []),
                    "improved_title": result.get("improved_title"),
                    "improved_summary": result.get("improved_summary"),
                    "tags": result.get("tags", []),
                },
                warnings=warnings,
                audit={"input_length": len(content)},
                duration_ms=duration,
            )

        except Exception as e:
            duration = int((time.monotonic() - start) * 1000)
            return self._result(
                "pass",
                {"message": "Análise de proposta temporariamente indisponível."},
                warnings=["O agente de propostas está temporariamente indisponível."],
                audit={"error": str(e)},
                duration_ms=duration,
            )
