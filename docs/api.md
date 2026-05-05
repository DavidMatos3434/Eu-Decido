# Documentação da API (Supabase)

Esta API é baseada no PostgREST (fornecido pelo Supabase) e Edge Functions.

## Endpoints Principais (Tabelas)

### Propostas (`/proposals`)
- **GET**: Lista todas as propostas visíveis para o utilizador.
- **POST**: Cria uma nova proposta (exige autenticação).

### Eleições (`/elections`)
- **GET**: Consulta processos eleitorais ativos.

## Edge Functions (Lógica Customizada)

### Votar (`/functions/v1/vote`)
- **Método**: POST
- **Lógica**: 
    1. Verifica se o utilizador já tem um `voting_token` para a proposta.
    2. Valida a elegibilidade (território).
    3. Insere o voto anónimo na tabela `votes`.
    4. Marca o token como usado.

### Gerar Resultados (`/functions/v1/results`)
- **Método**: GET
- **Lógica**: Calcula estatísticas em tempo real agregando dados da tabela `votes`.
