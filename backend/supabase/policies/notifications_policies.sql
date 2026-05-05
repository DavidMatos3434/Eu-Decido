-- POLÍTICAS: Módulo de Notificações

ALTER TABLE notifications ENABLE ROW LEVEL SECURITY;

-- Utilizador só pode ver as suas próprias notificações
CREATE POLICY "Utilizador vê suas notificações" ON notifications
    FOR SELECT
    TO authenticated
    USING (auth.uid() = user_id);

-- Utilizador pode marcar as suas notificações como lidas (update no campo is_read)
CREATE POLICY "Utilizador marca como lida" ON notifications
    FOR UPDATE
    TO authenticated
    USING (auth.uid() = user_id);

-- Apenas o sistema pode inserir notificações (via triggers ou funções)
CREATE POLICY "Sistema insere notificações" ON notifications
    FOR INSERT
    TO service_role
    WITH CHECK (true);
