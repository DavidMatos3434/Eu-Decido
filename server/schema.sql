-- EU DECIDO: Schema para o servidor local no Replit
-- (Versão adaptada do schema Supabase — sem auth.users, sem RLS)

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ==========================================
-- TIPOS (ENUMS)
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
-- TABELAS
-- ==========================================

CREATE TABLE IF NOT EXISTS identity (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nif_hash    TEXT UNIQUE NOT NULL,
    phone_hash  TEXT,
    verified    BOOLEAN DEFAULT false,
    created_at  TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at  TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS users (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    identity_id   UUID REFERENCES identity(id) ON DELETE SET NULL,
    username      TEXT UNIQUE NOT NULL,
    email         TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    avatar_url    TEXT,
    is_candidate  BOOLEAN DEFAULT false,
    points        INTEGER DEFAULT 0,
    created_at    TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at    TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS territories (
    id        UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name      TEXT NOT NULL,
    type      territory_type NOT NULL,
    parent_id UUID REFERENCES territories(id)
);

CREATE TABLE IF NOT EXISTS proposals (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    territory_id UUID REFERENCES territories(id),
    title        TEXT NOT NULL,
    description  TEXT NOT NULL,
    status       proposal_status DEFAULT 'DISCUSSION',
    type         proposal_type DEFAULT 'IDEIA',
    created_at   TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at   TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS comments (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id UUID REFERENCES proposals(id) ON DELETE CASCADE,
    user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
    content     TEXT NOT NULL,
    created_at  TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS educational_resources (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title      TEXT NOT NULL,
    content    TEXT NOT NULL,
    category   TEXT NOT NULL,
    url_video  TEXT,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS elections (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id  UUID REFERENCES proposals(id),
    title        TEXT NOT NULL,
    territory_id UUID REFERENCES territories(id),
    role         TEXT NOT NULL,
    status       election_status DEFAULT 'OPEN',
    created_at   TIMESTAMPTZ DEFAULT now() NOT NULL,
    updated_at   TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS candidacies (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    election_id UUID REFERENCES elections(id) ON DELETE CASCADE,
    user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
    status      candidacy_status DEFAULT 'PENDING',
    manifesto   TEXT,
    created_at  TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS representatives (
    id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id      UUID REFERENCES users(id) ON DELETE CASCADE,
    territory_id UUID REFERENCES territories(id),
    election_id  UUID REFERENCES elections(id),
    role         TEXT NOT NULL,
    created_at   TIMESTAMPTZ DEFAULT now() NOT NULL
);

CREATE TABLE IF NOT EXISTS notifications (
    id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id    UUID REFERENCES users(id) ON DELETE CASCADE,
    title      TEXT NOT NULL,
    message    TEXT NOT NULL,
    is_read    BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- Votos anónimos — nunca contêm user_id
CREATE TABLE IF NOT EXISTS votes (
    id                UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    proposal_id       UUID REFERENCES proposals(id) ON DELETE CASCADE,
    election_id       UUID REFERENCES elections(id) ON DELETE CASCADE,
    vote_value        TEXT NOT NULL,
    voting_token_hash TEXT UNIQUE NOT NULL,
    created_at        TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- Tokens de votação — ponte segura entre identidade e voto
CREATE TABLE IF NOT EXISTS voting_tokens (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    identity_id UUID REFERENCES identity(id),
    proposal_id UUID REFERENCES proposals(id),
    election_id UUID REFERENCES elections(id),
    token_hash  TEXT UNIQUE NOT NULL,
    used        BOOLEAN DEFAULT false,
    created_at  TIMESTAMPTZ DEFAULT now() NOT NULL
);

-- ==========================================
-- ÍNDICES
-- ==========================================
CREATE INDEX IF NOT EXISTS idx_proposals_territory ON proposals(territory_id);
CREATE INDEX IF NOT EXISTS idx_proposals_status    ON proposals(status);
CREATE INDEX IF NOT EXISTS idx_votes_proposal      ON votes(proposal_id);
CREATE INDEX IF NOT EXISTS idx_votes_election      ON votes(election_id);
CREATE INDEX IF NOT EXISTS idx_comments_proposal   ON comments(proposal_id);
CREATE INDEX IF NOT EXISTS idx_notifications_user  ON notifications(user_id) WHERE is_read = false;
CREATE INDEX IF NOT EXISTS idx_tokens_hash         ON voting_tokens(token_hash);

-- ==========================================
-- FUNÇÕES E TRIGGERS
-- ==========================================

CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'tr_update_user_timestamp') THEN
        CREATE TRIGGER tr_update_user_timestamp
            BEFORE UPDATE ON users
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'tr_update_proposal_timestamp') THEN
        CREATE TRIGGER tr_update_proposal_timestamp
            BEFORE UPDATE ON proposals
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'tr_update_election_timestamp') THEN
        CREATE TRIGGER tr_update_election_timestamp
            BEFORE UPDATE ON elections
            FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$;

-- Notificar o autor de uma proposta quando alguém comenta
CREATE OR REPLACE FUNCTION notify_on_comment()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO notifications (user_id, title, message)
    SELECT p.user_id,
           'Novo comentário',
           'Alguém comentou na tua proposta: ' || p.title
    FROM proposals p
    WHERE p.id = NEW.proposal_id
      AND p.user_id IS NOT NULL
      AND p.user_id != NEW.user_id;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'tr_notify_on_comment') THEN
        CREATE TRIGGER tr_notify_on_comment
            AFTER INSERT ON comments
            FOR EACH ROW EXECUTE FUNCTION notify_on_comment();
    END IF;
END $$;

-- Criar eleição automaticamente quando proposta do tipo ELECTION_REQUEST é aprovada
CREATE OR REPLACE FUNCTION create_election_on_approval()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.type = 'ELECTION_REQUEST'
       AND NEW.status = 'APPROVED'
       AND OLD.status != 'APPROVED' THEN
        INSERT INTO elections (proposal_id, title, territory_id, role, status)
        VALUES (NEW.id, 'Eleição: ' || NEW.title, NEW.territory_id, 'Representante Comunitário', 'OPEN');

        INSERT INTO notifications (user_id, title, message)
        VALUES (NEW.user_id, 'Eleição Aberta!',
                'A tua proposta de eleição foi aprovada e as candidaturas estão abertas.');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'tr_create_election_on_approval') THEN
        CREATE TRIGGER tr_create_election_on_approval
            AFTER UPDATE ON proposals
            FOR EACH ROW EXECUTE FUNCTION create_election_on_approval();
    END IF;
END $$;

-- Promover vencedor após fecho da eleição
CREATE OR REPLACE FUNCTION promote_winner_on_election_close()
RETURNS TRIGGER AS $$
DECLARE
    winner_id UUID;
BEGIN
    IF NEW.status = 'CLOSED' AND OLD.status != 'CLOSED' THEN
        SELECT vote_value::UUID INTO winner_id
        FROM votes
        WHERE election_id = NEW.id
        GROUP BY vote_value
        ORDER BY count(*) DESC
        LIMIT 1;

        IF winner_id IS NOT NULL THEN
            INSERT INTO representatives (user_id, territory_id, role, election_id)
            VALUES (winner_id, NEW.territory_id, NEW.role, NEW.id);

            INSERT INTO notifications (user_id, title, message)
            VALUES (winner_id, 'Eleição Concluída',
                    'Parabéns! Foste eleito para o cargo de ' || NEW.role);
        END IF;
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'tr_promote_winner_on_election_close') THEN
        CREATE TRIGGER tr_promote_winner_on_election_close
            AFTER UPDATE ON elections
            FOR EACH ROW EXECUTE FUNCTION promote_winner_on_election_close();
    END IF;
END $$;

-- ==========================================
-- DADOS DE TESTE (Seed)
-- ==========================================

INSERT INTO territories (id, name, type) VALUES
    ('00000000-0000-0000-0000-000000000001', 'Portugal', 'NACIONAL'),
    ('00000000-0000-0000-0000-000000000002', 'Lisboa e Vale do Tejo', 'REGIAO'),
    ('00000000-0000-0000-0000-000000000003', 'Lisboa', 'MUNICIPIO'),
    ('00000000-0000-0000-0000-000000000004', 'Arroios', 'FREGUESIA'),
    ('00000000-0000-0000-0000-000000000005', 'Belém', 'FREGUESIA'),
    ('00000000-0000-0000-0000-000000000006', 'Porto', 'MUNICIPIO'),
    ('00000000-0000-0000-0000-000000000007', 'Cedofeita', 'FREGUESIA')
ON CONFLICT (id) DO NOTHING;
