-- POLÍTICAS: Módulo de Propostas e Discussão (Colaboração Comunitária)

ALTER TABLE proposals ENABLE ROW LEVEL SECURITY;
ALTER TABLE comments ENABLE ROW LEVEL SECURITY;

-- 1. PROPOSTAS
-- Todos (mesmo não autenticados) podem ver propostas
CREATE POLICY "Propostas: Leitura pública" ON proposals
    FOR SELECT USING (true);

-- Apenas utilizadores autenticados podem submeter novas ideias
CREATE POLICY "Propostas: Submissão autenticada" ON proposals
    FOR INSERT 
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

-- Apenas o autor pode editar a descrição enquanto estiver em 'DISCUSSION'
CREATE POLICY "Propostas: Edição pelo autor" ON proposals
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id AND status = 'DISCUSSION');

-- 2. COMENTÁRIOS
-- Leitura aberta para promover a transparência
CREATE POLICY "Comentários: Leitura pública" ON comments
    FOR SELECT USING (true);

-- Utilizadores autenticados podem comentar
CREATE POLICY "Comentários: Criar comentário" ON comments
    FOR INSERT
    TO authenticated
    WITH CHECK (auth.uid() = user_id);

-- Eliminação apenas pelo autor ou moderação (futuro)
CREATE POLICY "Comentários: Eliminar pelo autor" ON comments
    FOR DELETE
    TO authenticated
    USING (auth.uid() = user_id);
