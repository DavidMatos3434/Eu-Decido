-- POLÍTICAS: Módulo de Identidade (Alta Segurança)

ALTER TABLE identity ENABLE ROW LEVEL SECURITY;
ALTER TABLE voting_tokens ENABLE ROW LEVEL SECURITY;

-- Impedir qualquer acesso direto de utilizadores comuns à tabela identity
CREATE POLICY "Identity: Acesso exclusivo via Service Role" ON identity
    FOR ALL 
    TO service_role
    USING (true);

-- Utilizadores podem ver os seus próprios tokens de votação mas não podem alterá-los
CREATE POLICY "VotingTokens: Ver meus tokens" ON voting_tokens
    FOR SELECT 
    TO authenticated
    USING (identity_id IN (
        SELECT identity_id FROM users WHERE id = auth.uid()
    ));

-- Apenas o sistema pode gerar ou marcar tokens como usados
CREATE POLICY "VotingTokens: Gestão pelo sistema" ON voting_tokens
    FOR ALL 
    TO service_role
    USING (true);
