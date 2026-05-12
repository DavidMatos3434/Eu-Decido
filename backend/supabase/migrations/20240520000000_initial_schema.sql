-- EU DECIDO: Esquema de Base de Dados Definitivo (Segurança + Automação + Governação)
-- Versão para Supabase (com auth.users e RLS)

/*
### Descrição do Esquema de Base de Dados (PostgreSQL)

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

## Módulo de Inteligência Artificial
- **agent_results**: Log de auditoria e resultados dos agentes (Moderação, Resumo, Análise de Impacto).
*/

-- Habilitar extensões
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================================
-- 1. TIPOS PERSONALIZADOS (ENUMS)
-- ==========================================
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'proposal_status') THEN
        CREATE TYPE proposal_status AS ENUM ('DISCUSSION', 'VOTING', 'APPROVED', 'REJECTED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'proposal_type') THEN
        CREATE TYPE proposal_type AS ENUM ('IDEIA', 'ELECTION_REQUEST');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'territory_type') THEN
        CREATE TYPE territory_type AS ENUM ('FREGUESIA', 'MUNICIPIO', 'REGIAO', 'NACIONAL');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'election_status') THEN
        CREATE TYPE election_status AS ENUM ('DRAFT', 'OPEN', 'VOTING', 'CLOSED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'candidacy_status') THEN
        CREATE TYPE candidacy_status AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
    END IF;
END $$;

-- ==========================================
-- 2. TABELAS
-- ==========================================

-- A. IDENTIDADE REAL (CAMADA 1)
CREATE TABLE IF NOT EXISTS identity (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nif_hash TEXT UNIQUE NOT NULL,
    phone_hash TEXT,
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- B. CONTA DE UTILIZADOR (CAMADA 2)
CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY REFERENCES auth.users ON DELETE CASCADE,
    identity_id UUID REFERENCES identity(id) ON DELETE SET NULL,
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE,
    avatar_url TEXT,
    is_candidate BOOLEAN DEFAULT false,
    points INTEGER DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- C. TERRITÓRIOS
CREATE TABLE IF NOT EXISTS territories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    type territory_type NOT NULL,
    parent_id UUID REFERENCES territories(id)
);

-- D. PROPOSTAS
CREATE TABLE IF NOT EXISTS proposals (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id),
    territory_id UUID REFERENCES territories(id),
    title TEXT NOT NULL,
    description TEXT NOT NULL,
    status proposal_status DEFAULT 'DISCUSSION',
    type proposal_type DEFAULT 'IDEIA',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- E. COMENTÁRIOS
CREATE TABLE IF NOT EXISTS comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id UUID REFERENCES proposals(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- G. ELEIÇÕES
CREATE TABLE IF NOT EXISTS elections (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id UUID REFERENCES proposals(id),
    title TEXT NOT NULL,
    territory_id UUID REFERENCES territories(id),
    role TEXT NOT NULL,
    status election_status DEFAULT 'OPEN',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- H. CANDIDATURAS
CREATE TABLE IF NOT EXISTS candidacies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    election_id UUID REFERENCES elections(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    status candidacy_status DEFAULT 'PENDING',
    manifesto TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- I. REPRESENTANTES (ELEITOS)
CREATE TABLE IF NOT EXISTS representatives (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    territory_id UUID REFERENCES territories(id),
    election_id  UUID REFERENCES elections(id),
    role TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- J. NOTIFICAÇÕES
CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- K. VOTOS ANÓNIMOS (CAMADA 3)
CREATE TABLE IF NOT EXISTS votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id UUID REFERENCES proposals(id) ON DELETE CASCADE,
    election_id UUID REFERENCES elections(id) ON DELETE CASCADE,
    vote_value TEXT NOT NULL,
    voting_token_hash TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- L. TOKENS DE VOTAÇÃO (PONTE SEGURA)
CREATE TABLE IF NOT EXISTS voting_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    identity_id UUID REFERENCES identity(id),
    proposal_id UUID REFERENCES proposals(id),
    election_id UUID REFERENCES elections(id),
    token_hash TEXT UNIQUE NOT NULL,
    used BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- M. RESULTADOS DE AGENTES IA
CREATE TABLE IF NOT EXISTS agent_results (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id UUID REFERENCES proposals(id) ON DELETE CASCADE,
    comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
    agent_name TEXT NOT NULL,
    result_type TEXT NOT NULL, -- 'MODERATION', 'SUMMARY', 'IMPACT'
    payload JSONB NOT NULL,
    score FLOAT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- ==========================================
-- 3. SEGURANÇA (RLS)
-- ==========================================
ALTER TABLE identity ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE territories ENABLE ROW LEVEL SECURITY;
ALTER TABLE proposals ENABLE ROW LEVEL SECURITY;
ALTER TABLE comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE elections ENABLE ROW LEVEL SECURITY;
ALTER TABLE candidacies ENABLE ROW LEVEL SECURITY;
ALTER TABLE representatives ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE voting_tokens ENABLE ROW LEVEL SECURITY;
ALTER TABLE agent_results ENABLE ROW LEVEL SECURITY;

-- Políticas de Leitura Pública
CREATE POLICY "Leitura pública" ON territories FOR SELECT USING (true);
CREATE POLICY "Leitura pública" ON proposals FOR SELECT USING (true);
CREATE POLICY "Leitura pública" ON comments FOR SELECT USING (true);
CREATE POLICY "Leitura pública" ON votes FOR SELECT USING (true);
CREATE POLICY "Leitura pública" ON elections FOR SELECT USING (true);
CREATE POLICY "Leitura pública" ON candidacies FOR SELECT USING (true);
CREATE POLICY "Leitura pública" ON representatives FOR SELECT USING (true);
CREATE POLICY "Leitura pública" ON agent_results FOR SELECT USING (true);

-- Políticas de Inserção Autenticada
CREATE POLICY "Inserção autenticada" ON proposals FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Inserção autenticada" ON comments FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users: Gestão perfil próprio" ON users FOR ALL USING (auth.uid() = id);
CREATE POLICY "Notificações: Ver próprias" ON notifications FOR SELECT TO authenticated USING (auth.uid() = user_id);

-- ==========================================
-- 4. ÍNDICES
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_proposals_territory ON proposals(territory_id);
CREATE INDEX IF NOT EXISTS idx_votes_proposal ON votes(proposal_id);
CREATE INDEX IF NOT EXISTS idx_votes_election ON votes(election_id);
CREATE INDEX IF NOT EXISTS idx_notifications_unread ON notifications(user_id) WHERE is_read = false;

-- ==========================================
-- 5. FUNÇÕES E GATILHOS (AUTOMAÇÃO)
-- ==========================================

-- Timestamp Automático
CREATE OR REPLACE FUNCTION update_updated_at_column() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER tr_update_user_timestamp BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER tr_update_proposal_timestamp BEFORE UPDATE ON proposals FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

-- Sincronização Auth -> Users (Supabase Auth)
CREATE OR REPLACE FUNCTION handle_new_user() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, username, email)
    VALUES (NEW.id, split_part(NEW.email, '@', 1), NEW.email);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created AFTER INSERT ON auth.users FOR EACH ROW EXECUTE FUNCTION handle_new_user();

-- Notificar Comentários
CREATE OR REPLACE FUNCTION notify_on_comment() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO notifications (user_id, title, message)
    SELECT user_id, 'Novo comentário', 'Alguém comentou na tua proposta: ' || title
    FROM proposals WHERE id = NEW.proposal_id AND user_id != NEW.user_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_notify_on_comment AFTER INSERT ON comments FOR EACH ROW EXECUTE FUNCTION notify_on_comment();