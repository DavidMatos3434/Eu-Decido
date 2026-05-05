-- EU DECIDO: Esquema de Base de Dados Definitivo (Segurança + Automação + Governação)
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
CREATE TABLE identity (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nif_hash TEXT UNIQUE NOT NULL,
    verified BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- B. CONTA DE UTILIZADOR (CAMADA 2)
CREATE TABLE users (
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
CREATE TABLE territories (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT NOT NULL,
    type territory_type NOT NULL,
    parent_id UUID REFERENCES territories(id)
);

-- D. PROPOSTAS
CREATE TABLE proposals (
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
CREATE TABLE comments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id UUID REFERENCES proposals(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE SET NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- F. RECURSOS EDUCATIVOS
CREATE TABLE educational_resources (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    category TEXT NOT NULL,
    url_video TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- G. ELEIÇÕES
CREATE TABLE elections (
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
CREATE TABLE candidacies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    election_id UUID REFERENCES elections(id) ON DELETE CASCADE,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    status candidacy_status DEFAULT 'PENDING',
    manifesto TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- I. REPRESENTANTES (ELEITOS)
CREATE TABLE representatives (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    territory_id UUID REFERENCES territories(id),
    election_id UUID REFERENCES elections(id),
    role TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- J. NOTIFICAÇÕES
CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    title TEXT NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT false,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- K. VOTOS ANÓNIMOS (CAMADA 3)
CREATE TABLE votes (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id UUID REFERENCES proposals(id) ON DELETE CASCADE,
    election_id UUID REFERENCES elections(id) ON DELETE CASCADE,
    vote_value TEXT NOT NULL, 
    voting_token_hash TEXT UNIQUE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL
);

-- L. TOKENS DE VOTAÇÃO (PONTE SEGURA)
CREATE TABLE voting_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    identity_id UUID REFERENCES identity(id),
    proposal_id UUID REFERENCES proposals(id),
    election_id UUID REFERENCES elections(id),
    token_hash TEXT UNIQUE NOT NULL,
    used BOOLEAN DEFAULT false,
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
ALTER TABLE educational_resources ENABLE ROW LEVEL SECURITY;
ALTER TABLE elections ENABLE ROW LEVEL SECURITY;
ALTER TABLE candidacies ENABLE ROW LEVEL SECURITY;
ALTER TABLE representatives ENABLE ROW LEVEL SECURITY;
ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;
ALTER TABLE votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE voting_tokens ENABLE ROW LEVEL SECURITY;

-- Políticas
CREATE POLICY "Territórios: Leitura pública" ON territories FOR SELECT USING (true);
CREATE POLICY "Recursos: Leitura pública" ON educational_resources FOR SELECT USING (true);
CREATE POLICY "Propostas: Leitura pública" ON proposals FOR SELECT USING (true);
CREATE POLICY "Propostas: Inserção autenticada" ON proposals FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Comentários: Leitura pública" ON comments FOR SELECT USING (true);
CREATE POLICY "Comentários: Inserção autenticada" ON comments FOR INSERT TO authenticated WITH CHECK (auth.uid() = user_id);
CREATE POLICY "Users: Gestão perfil próprio" ON users FOR ALL USING (auth.uid() = id);
CREATE POLICY "Identity: Acesso sistema" ON identity FOR ALL TO service_role USING (true);
CREATE POLICY "Notificações: Ver próprias" ON notifications FOR SELECT TO authenticated USING (auth.uid() = user_id);
CREATE POLICY "Notificações: Update próprio" ON notifications FOR UPDATE TO authenticated USING (auth.uid() = user_id);
CREATE POLICY "Votos: Leitura pública" ON votes FOR SELECT USING (true);
CREATE POLICY "Eleições: Leitura pública" ON elections FOR SELECT USING (true);
CREATE POLICY "Candidaturas: Leitura pública" ON candidacies FOR SELECT USING (true);
CREATE POLICY "Representantes: Leitura pública" ON representatives FOR SELECT USING (true);

-- ==========================================
-- 4. ÍNDICES
-- ==========================================
CREATE INDEX idx_proposals_territory ON proposals(territory_id);
CREATE INDEX idx_votes_proposal ON votes(proposal_id);
CREATE INDEX idx_votes_election ON votes(election_id);
CREATE INDEX idx_notifications_unread ON notifications(user_id) WHERE is_read = false;

-- ==========================================
-- 5. FUNÇÕES E GATILHOS (AUTOMAÇÃO)
-- ==========================================

-- A. Timestamp Automático
CREATE OR REPLACE FUNCTION update_updated_at_column() RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER tr_update_user_timestamp BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();
CREATE TRIGGER tr_update_proposal_timestamp BEFORE UPDATE ON proposals FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

-- B. Sincronização Auth -> Users
CREATE OR REPLACE FUNCTION handle_new_user() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.users (id, username, email)
    VALUES (NEW.id, split_part(NEW.email, '@', 1), NEW.email);
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

CREATE TRIGGER on_auth_user_created AFTER INSERT ON auth.users FOR EACH ROW EXECUTE FUNCTION handle_new_user();

-- C. Notificar Comentários
CREATE OR REPLACE FUNCTION notify_on_comment() RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO notifications (user_id, title, message)
    SELECT user_id, 'Novo comentário', 'Alguém comentou na tua proposta: ' || title
    FROM proposals WHERE id = NEW.proposal_id AND user_id != NEW.user_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_notify_on_comment AFTER INSERT ON comments FOR EACH ROW EXECUTE FUNCTION notify_on_comment();

-- D. Criar Eleição após Aprovação
CREATE OR REPLACE FUNCTION create_election_on_approval() RETURNS TRIGGER AS $$
BEGIN
    IF NEW.type = 'ELECTION_REQUEST' AND NEW.status = 'APPROVED' AND (OLD IS NULL OR OLD.status != 'APPROVED') THEN
        INSERT INTO elections (proposal_id, title, territory_id, role, status)
        VALUES (NEW.id, 'Eleição: ' || NEW.title, NEW.territory_id, 'Representante Comunitário', 'OPEN');
        
        INSERT INTO notifications (user_id, title, message)
        VALUES (NEW.user_id, 'Eleição Aberta!', 'A tua proposta de eleição foi aprovada e as candidaturas estão abertas.');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_create_election_on_approval AFTER UPDATE ON proposals FOR EACH ROW EXECUTE FUNCTION create_election_on_approval();

-- E. Promover Vencedor a Representante após fecho da eleição
CREATE OR REPLACE FUNCTION promote_winner_on_election_close() RETURNS TRIGGER AS $$
DECLARE
    winner_id UUID;
BEGIN
    IF NEW.status = 'CLOSED' AND OLD.status != 'CLOSED' THEN
        SELECT vote_value::UUID INTO winner_id
        FROM votes WHERE election_id = NEW.id
        GROUP BY vote_value ORDER BY count(*) DESC LIMIT 1;

        IF winner_id IS NOT NULL THEN
            INSERT INTO representatives (user_id, territory_id, role, election_id)
            VALUES (winner_id, NEW.territory_id, NEW.role, NEW.id);
            
            INSERT INTO notifications (user_id, title, message)
            VALUES (winner_id, 'Eleição Concluída', 'Parabéns! Foste eleito para o cargo de ' || NEW.role);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tr_promote_winner_on_election_close AFTER UPDATE ON elections FOR EACH ROW EXECUTE FUNCTION promote_winner_on_election_close();
