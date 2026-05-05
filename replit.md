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

### Backend (Supabase)
- **PostgreSQL** com RLS (Row-Level Security)
- **Supabase Auth** — autenticação de utilizadores
- **Supabase Edge Functions (Deno/TypeScript)**:
  - `vote/` — Votação anónima com token (Tribunal Digital)
  - `results/` — Agregação de resultados em tempo real
  - `identity-validation/` — Validação de NIF/CMD e hashing seguro
  - `elections/` — Motor de eleições (placeholder)

## Estrutura do Projeto
```
composeApp/        → UI e ScreenModels (Android/iOS)
shared/            → Repositórios, SQLDelight, DI, SessionManager
backend/
  supabase/
    migrations/    → Schema PostgreSQL
    functions/     → Edge Functions (Deno)
    policies/      → RLS policies
docs/              → Documentação técnica
```

## Padrão de Arquitectura
- **Offline-First**: os dados são guardados localmente (SQLDelight) e sincronizados via `sync_queue`
- **SessionManager**: singleton (`shared/data/SessionManager.kt`) que mantém o utilizador autenticado em memória (userId, username, email)
- **Voyager ScreenModel**: cada ecrã tem o seu ScreenModel registado no `appModule` via Koin

## Bugs Corrigidos (Maio 2026)

### Críticos — Backend
1. **identity-validation**: adicionados CORS headers + handler OPTIONS; NIF e telefone agora são hasheados com SHA-256 (antes: btoa reversível + phone em texto claro)
2. **vote**: race condition eliminada — o token é agora queimado atomicamente (update condicional `used=false`) antes de inserir o voto; validação de parâmetros adicionada
3. **results**: `abstention` adicionado ao objecto `percentage`; percentagens garantidas a somar 100% (o valor de abstençao recebe o resto)

### Críticos — Mobile
4. **LoginScreenModel**: removidos username "David Silva" e userId hardcoded; username derivado do email; SessionManager actualizado no login
5. **RegisterScreenModel**: userId e username derivados de inputs reais; SessionManager actualizado no registo
6. **VotingScreenModel**: voto agora enfileirado no `sync_queue` para sincronização com a Edge Function; injectado `SyncRepository`
7. **AppModule**: ~15 ScreenModels em falta registados no Koin (ElectionDetail, ElectionList, ApplyCandidate, CandidateList, CandidateDetail, Profile, Delegation, TerritorySelection, TerritoryOverview, Report, Search, Community, AIProposalHelper, AIInsights, AIAssistant, ProposalList, Welcome)

### Significativos — Mobile
8. **SessionManager** criado: elimina todos os `"current_user"` hardcoded de HomeScreenModel, DiscussionScreenModel, CreateProposalScreenModel, ApplyCandidateScreenModel, ProfileScreenModel
9. **ResultsScreenModel**: nested `collectLatest` substituído por `flatMapLatest` + `collect` (padrão correcto para flows dependentes)
10. **ElectionDetailScreenModel**: mesmo fix de nested `collectLatest`

### Menores
11. **HomeScreenModel**: ellipsis `"..."` só adicionado quando a descrição ultrapassa 100 caracteres
12. **ProfileScreenModel**: `logout()` agora chama `SessionManager.logout()`
