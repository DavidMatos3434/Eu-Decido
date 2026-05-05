# EU DECIDO — Sistema de Governação Digital

## Visão Geral
Aplicação cívica para governação territorial em Portugal. Permite que cidadãos criem propostas, discutam, votem de forma anónima, candidatem-se a eleições e acompanhem representantes eleitos.

## Tecnologias

### Mobile (composeApp + shared)
- **Kotlin Multiplatform** — Android + iOS
- **Jetpack Compose Multiplatform** — UI declarativa
- **Voyager** — Navegação entre ecrãs (`Navigator`, `ScreenModel`, `TabNavigator`)
- **Koin** — Injeção de dependências (`commonModule` + `appModule`)
- **SQLDelight** — Base de dados local (offline-first)
- **kotlinx-datetime** — Gestão de timestamps

### Backend Local (Replit — substitui Supabase para testes)
- **Python 3.11 + FastAPI** — API REST
- **asyncpg** — Ligação assíncrona ao PostgreSQL
- **bcrypt** — Hash de palavras-passe
- **python-jose** — JWT (HS256, 7 dias de validade)
- **Replit PostgreSQL** — Base de dados (DATABASE_URL env var)
- **Uvicorn** — Servidor ASGI com hot-reload

### Backend Original (Supabase — produção)
- **PostgreSQL** com RLS (Row-Level Security)
- **Supabase Auth** — autenticação de utilizadores
- **Supabase Edge Functions (Deno/TypeScript)**

## Estrutura do Projeto
```
composeApp/        → UI e ScreenModels (Android/iOS)
shared/            → Repositórios, SQLDelight, DI, SessionManager
server/            → Backend FastAPI local (Replit)
  main.py          → Entrypoint FastAPI + lifespan (pool DB)
  migrate.py       → Script de migração do schema
  schema.sql       → Schema PostgreSQL completo
  requirements.txt → Dependências Python
  core/
    database.py    → Pool asyncpg
    security.py    → bcrypt + JWT + SHA-256
    dependencies.py→ get_current_user (JWT Bearer)
  routers/
    auth.py        → /auth/register, /auth/login, /auth/me
    proposals.py   → CRUD propostas
    comments.py    → /proposals/{id}/comments
    voting.py      → Votação anónima com token atómico
    elections.py   → CRUD eleições
    territories.py → Listagem de territórios
    representatives.py → Representantes eleitos
    notifications.py   → Notificações por utilizador
    identity.py    → Validação de identidade (NIF hash)
backend/
  supabase/
    migrations/    → Schema PostgreSQL (produção)
    functions/     → Edge Functions (Deno)
    policies/      → RLS policies
docs/              → Documentação técnica
```

## Motor de Agentes — Fase 1

### Arquitectura
```
Evento (nova proposta / comentário)
       │
       ▼
Orquestrador (server/agents/orchestrator.py)
  — roteamento determinístico por tipo de evento
  — nunca usa LLM para decidir qual agente chamar
       │
       ├─ 1. Agente de Moderação (SEMPRE primeiro, SÍNCRONO)
       │       → único que pode BLOQUEAR
       │       → filtro de padrões (sem LLM) + análise LLM para casos ambíguos
       │       → fail-open: se LLM indisponível, aprova automaticamente
       │
       └─ 2. Agentes de Enriquecimento (PARALELOS, nunca bloqueiam)
               ├─ Agente de Propostas → score, tema, sugestões, melhorias
               └─ Agente de Resumo    → TLDR, técnico, impacto local, acessível
```

### Configuração do LLM (variáveis de ambiente)
| Variável | Valores | Default |
|----------|---------|---------|
| `LLM_PROVIDER` | `ollama` \| `mistral` \| `openai` | `ollama` |
| `LLM_BASE_URL` | URL da API | `http://localhost:11434/v1` |
| `LLM_MODEL` | nome do modelo | `mistral` |
| `MISTRAL_API_KEY` | chave Mistral AI | — |
| `OPENAI_API_KEY` | chave OpenAI | — |
| `LLM_TIMEOUT` | segundos | `60` |

**Para usar Mistral API:**
```
LLM_PROVIDER=mistral
MISTRAL_API_KEY=<chave>
LLM_MODEL=mistral-small-latest
```

**Para usar Ollama local (servidor próprio):**
```
LLM_PROVIDER=ollama
LLM_BASE_URL=http://<ip-servidor>:11434/v1
LLM_MODEL=mistral
```

### Princípio fundamental dos agentes de supervisão
> **"Informar e contextualizar, nunca censurar — excepto conteúdo claramente ilegal."**
- Agente de Moderação: único que bloqueia (insultos, ódio, ameaças)
- Todos os outros: adicionam avisos/contexto, NUNCA bloqueiam propostas

### Endpoints dos Agentes
| Método | Path | Descrição |
|--------|------|-----------|
| GET | /agents/status | Estado do sistema + LLM online/offline |
| POST | /agents/analyze | Analisa texto (sem persistência) |
| POST | /agents/comment/moderate | Modera comentário antes de publicar |
| POST | /agents/proposal/{id}/analyze | Analisa proposta + guarda auditoria |
| GET | /agents/proposal/{id}/history | Histórico completo de decisões dos agentes |

### Tabela de Auditoria
`agent_results` — registo de todas as decisões de todos os agentes, com JSON completo de input/output, para transparência total e auditabilidade.

## Workflow Replit
- **Nome**: `EU DECIDO API`
- **Comando**: `python -m uvicorn server.main:app --host 0.0.0.0 --port 8000 --reload`
- **Porta**: 8000
- **Docs interativas**: `/docs` (Swagger UI)
- **Health check**: `/health`

## Endpoints Disponíveis
| Método | Path | Auth | Descrição |
|--------|------|------|-----------|
| GET | / | — | Estado da API |
| GET | /health | — | Health check + DB |
| POST | /auth/register | — | Criar conta |
| POST | /auth/login | — | Login (retorna JWT) |
| GET | /auth/me | JWT | Perfil do utilizador |
| GET | /proposals | — | Listar propostas |
| POST | /proposals | JWT | Criar proposta |
| GET | /proposals/{id} | — | Detalhe de proposta |
| PATCH | /proposals/{id} | JWT | Atualizar proposta |
| GET | /proposals/{id}/comments | — | Listar comentários |
| POST | /proposals/{id}/comments | JWT | Adicionar comentário |
| POST | /voting/tokens/generate | JWT | Gerar token de voto |
| POST | /voting/cast | — | Votar (token anónimo) |
| GET | /elections | — | Listar eleições |
| GET | /territories | — | Listar territórios |
| GET | /representatives | — | Representantes eleitos |
| GET | /notifications | JWT | Notificações do utilizador |
| PATCH | /notifications/{id}/read | JWT | Marcar notificação como lida |
| POST | /identity/verify | JWT | Verificar identidade (NIF) |

## Padrão de Arquitectura Mobile
- **Offline-First**: dados guardados localmente (SQLDelight) e sincronizados via `sync_queue`
- **SessionManager**: singleton (`shared/data/SessionManager.kt`) — mantém userId, username, email em memória
- **Voyager ScreenModel**: cada ecrã tem o seu ScreenModel registado no `appModule` via Koin

## Base de Dados — Tabelas Principais
| Tabela | Descrição |
|--------|-----------|
| users | Utilizadores registados |
| identity | Hashes NIF/telefone para verificação |
| territories | Hierarquia: NACIONAL > REGIAO > MUNICIPIO > FREGUESIA |
| proposals | Propostas cívicas (DISCUSSION → VOTING → APPROVED/REJECTED) |
| comments | Comentários em propostas |
| elections | Eleições abertas |
| candidacies | Candidaturas a eleições |
| representatives | Representantes eleitos |
| votes | Votos anónimos (token hash único) |
| voting_tokens | Tokens de voto (burn-on-use) |
| notifications | Notificações de utilizadores |

## Triggers Automáticos
- `tr_notify_on_comment` — notifica o autor da proposta quando alguém comenta
- `tr_create_election_on_approval` — cria eleição quando proposta ELECTION_REQUEST é aprovada
- `tr_promote_winner_on_election_close` — promove vencedor a representante quando eleição fecha
- `tr_update_*_timestamp` — atualiza `updated_at` em users, proposals, elections

## Bugs Corrigidos (Maio 2026)

### Críticos — Backend Supabase
1. **identity-validation**: CORS headers + SHA-256 para NIF e telefone
2. **vote**: race condition eliminada — token queimado atomicamente
3. **results**: `abstention` adicionado; percentagens garantidas a somar 100%

### Críticos — Mobile
4. **LoginScreenModel**: removidos hardcodes; SessionManager actualizado
5. **RegisterScreenModel**: userId/username dos inputs reais
6. **VotingScreenModel**: voto enfileirado no `sync_queue`; SyncRepository injectado
7. **AppModule**: ~15 ScreenModels em falta registados no Koin

### Significativos — Mobile
8. **SessionManager** criado: elimina `"current_user"` hardcoded em 5 ScreenModels
9. **ResultsScreenModel**: `flatMapLatest` + `collect` (fix nested collectLatest)
10. **ElectionDetailScreenModel**: mesmo fix

### Menores
11. **HomeScreenModel**: ellipsis só quando descrição > 100 chars
12. **ProfileScreenModel**: `logout()` chama `SessionManager.logout()`
