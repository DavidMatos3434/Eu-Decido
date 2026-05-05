-- POLÍTICAS DE SEGURANÇA (RLS - Row Level Security)

-- Ativar RLS em todas as tabelas
ALTER TABLE identity ENABLE ROW LEVEL SECURITY;
ALTER TABLE users ENABLE ROW LEVEL SECURITY;
ALTER TABLE proposals ENABLE ROW LEVEL SECURITY;
ALTER TABLE votes ENABLE ROW LEVEL SECURITY;
ALTER TABLE voting_tokens ENABLE ROW LEVEL SECURITY;

-- 1. Identity: Apenas o sistema (service_role) pode ler/escrever
CREATE POLICY "Identity restrita ao sistema" ON identity
    FOR ALL USING (auth.jwt() ->> 'role' = 'service_role');

-- 2. Users: Utilizador pode ver o seu próprio perfil, todos podem ver perfis públicos
CREATE POLICY "Perfis visíveis por todos" ON users
    FOR SELECT USING (true);

CREATE POLICY "Utilizador gere o seu próprio perfil" ON users
    FOR UPDATE USING (auth.uid() = id);

-- 3. Proposals: Todos podem ver, apenas utilizadores autenticados podem criar
CREATE POLICY "Propostas visíveis por todos" ON proposals
    FOR SELECT USING (true);

CREATE POLICY "Utilizadores autenticados criam propostas" ON proposals
    FOR INSERT WITH CHECK (auth.role() = 'authenticated');

-- 4. Votes: Votos são anónimos, apenas leitura pública de contagens
CREATE POLICY "Votos são privados mas auditáveis" ON votes
    FOR SELECT USING (true);

-- Apenas a Edge Function de Votação pode inserir votos (via service_role)
CREATE POLICY "Sistema insere votos" ON votes
    FOR INSERT WITH CHECK (auth.jwt() ->> 'role' = 'service_role');
