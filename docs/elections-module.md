# Módulo de Eleições e Representantes

Este módulo transforma propostas aprovadas em processos democráticos estruturados.

## Fluxo de Vida de uma Eleição
1. **Trigger**: Uma proposta do tipo "Abertura de Eleição" é aprovada pela comunidade.
2. **Abertura**: O sistema cria um registo na tabela `elections` com status `OPEN`.
3. **Candidatura**: Cidadãos autenticam-se com Chave Móvel Digital e submetem perfil.
4. **Campanha**: Período de discussão e apresentação de propostas dos candidatos.
5. **Votação**: Utilizadores do território votam usando o sistema de tokens anónimos.
6. **Proclamação**: O vencedor é inserido na tabela `representatives`.

## Regras de Integridade
- **Unicidade**: 1 Identidade Real (NIF Hash) = 1 Candidatura por eleição.
- **Territorialidade**: Apenas cidadãos da mesma Freguesia/Município podem votar ou candidatar-se.
