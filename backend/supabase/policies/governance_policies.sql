-- POLÍTICAS: Módulo de Governação (Eleições e Candidaturas)

ALTER TABLE elections ENABLE ROW LEVEL SECURITY;
ALTER TABLE candidacies ENABLE ROW LEVEL SECURITY;
ALTER TABLE representatives ENABLE ROW LEVEL SECURITY;

-- 1. ELEIÇÕES
-- Qualquer cidadão pode consultar eleições ativas ou passadas
CREATE POLICY "Elections: Leitura pública" ON elections
    FOR SELECT USING (true);

-- Apenas o sistema (via trigger ou service_role) pode abrir novas eleições
CREATE POLICY "Elections: Gestão exclusiva pelo sistema" ON elections
    FOR ALL TO service_role USING (true);

-- 2. CANDIDATURAS
-- Qualquer pessoa pode ver quem são os candidatos
CREATE POLICY "Candidaturas: Leitura pública" ON candidacies
    FOR SELECT USING (true);

-- Utilizadores autenticados podem submeter candidatura (sujeito a validação do Identity Agent)
CREATE POLICY "Candidaturas: Submissão autenticada" ON candidacies
    FOR INSERT 
    TO authenticated
    WITH CHECK (auth.uid() = user_id AND status = 'PENDING');

-- 3. REPRESENTANTES (ELEITOS)
-- Leitura pública para transparência total
CREATE POLICY "Representantes: Leitura pública" ON representatives
    FOR SELECT USING (true);

-- Apenas o sistema pode oficializar um representante após o fecho da eleição
CREATE POLICY "Representantes: Nomeação pelo sistema" ON representatives
    FOR ALL TO service_role USING (true);
