"""
Classe base para todos os agentes EU DECIDO.
Cada agente tem um nome, uma descrição e um método principal `run()`.
Todos os resultados são estruturados e auditáveis.
"""
from abc import ABC, abstractmethod
from dataclasses import dataclass, field
from datetime import datetime, timezone
from typing import Any


@dataclass
class AgentResult:
    agent: str
    action: str          # "pass" | "block" | "warn" | "enrich" | "error"
    data: dict           # payload específico do agente
    warnings: list[str] = field(default_factory=list)
    audit: dict = field(default_factory=dict)
    duration_ms: int = 0
    timestamp: str = field(
        default_factory=lambda: datetime.now(timezone.utc).isoformat()
    )

    def to_dict(self) -> dict[str, Any]:
        return {
            "agent": self.agent,
            "action": self.action,
            "data": self.data,
            "warnings": self.warnings,
            "audit": self.audit,
            "duration_ms": self.duration_ms,
            "timestamp": self.timestamp,
        }

    @property
    def blocked(self) -> bool:
        return self.action == "block"

    @property
    def ok(self) -> bool:
        return self.action in ("pass", "warn", "enrich")


class BaseAgent(ABC):
    name: str = "base"
    description: str = ""

    @abstractmethod
    async def run(self, context: dict) -> AgentResult:
        """
        Executa o agente com o contexto fornecido.
        context pode conter: text, title, proposal_id, user_id, language, etc.
        """
        ...

    def _result(
        self,
        action: str,
        data: dict,
        warnings: list[str] | None = None,
        audit: dict | None = None,
        duration_ms: int = 0,
    ) -> AgentResult:
        return AgentResult(
            agent=self.name,
            action=action,
            data=data,
            warnings=warnings or [],
            audit=audit or {},
            duration_ms=duration_ms,
        )
