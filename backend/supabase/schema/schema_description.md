# Descrição do Esquema de Base de Dados (PostgreSQL)

## Módulo de Identidade e Utilizadores
- **identity**: Tabela de alta segurança que armazena hashes de NIF e telemóvel. É o elo de ligação entre a pessoa real e a conta digital.
- **users**: Perfil da aplicação. Contém dados públicos e preferências. Está ligada à `identity` mas não contém dados sensíveis em claro.

## Módulo de Governação Territorial
- **territories**: Estrutura hierárquica (Nacional > Região > Município > Freguesia).
- **representatives**: Cidadãos eleitos para cargos específicos em territórios.

## Módulo de Propostas e Participação
- **proposals**: Ideias submetidas por cidadãos. Passam pelos estados: `DISCUSSION`, `VOTING`, `APPROVED`, `REJECTED`.
- **comments**: Discussão pública sobre propostas.

## Módulo de Votação Segura (Anonimização)
- **voting_tokens**: Tokens temporários gerados para cada utilizador/proposta. Garante a unicidade do voto sem revelar o autor.
- **votes**: Registo do voto (`SIM`, `NAO`, `ABSTENCAO`) ligado apenas ao token, nunca ao `user_id`.
