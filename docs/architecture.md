# Arquitetura do Sistema EU DECIDO

## Princípio das 3 Camadas
1. **Identidade Real (Privada):** Armazenada no backend, nunca exposta à App. Ligada via hash de NIF.
2. **Conta de Utilizador (App):** Perfil público (username, avatar, território).
3. **Ações (Anónimas):** Votos e comentários desassociados da identidade real através de tokens.

## Fluxo de Dados
- **Offline-First:** Dados são guardados no SQLDelight e sincronizados via SyncManager.
- **Segurança:** Encriptação local e RLS (Row Level Security) no Supabase.
