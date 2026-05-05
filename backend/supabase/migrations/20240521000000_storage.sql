-- CONFIGURAÇÃO DE STORAGE (EU DECIDO)

-- 1. Criar buckets
INSERT INTO storage.buckets (id, name, public) VALUES 
('avatars', 'avatars', true),
('proposals', 'proposals', true),
('candidates', 'candidates', true);

-- 2. POLÍTICAS PARA AVATARS (Público para ver, restrito para subir)
CREATE POLICY "Avatars públicos" ON storage.objects FOR SELECT USING (bucket_id = 'avatars');
CREATE POLICY "Utilizador sobe seu avatar" ON storage.objects FOR INSERT WITH CHECK (
    bucket_id = 'avatars' AND auth.uid()::text = (storage.foldername(name))[1]
);

-- 3. POLÍTICAS PARA PROPOSTAS (Imagens/Documentos de apoio)
CREATE POLICY "Ficheiros de propostas públicos" ON storage.objects FOR SELECT USING (bucket_id = 'proposals');
CREATE POLICY "Utilizador autenticado sobe anexos" ON storage.objects FOR INSERT TO authenticated WITH CHECK (bucket_id = 'proposals');

-- 4. POLÍTICAS PARA CANDIDATOS (Fotos de campanha)
CREATE POLICY "Fotos de candidatos públicas" ON storage.objects FOR SELECT USING (bucket_id = 'candidates');
CREATE POLICY "Apenas candidatos sobem fotos" ON storage.objects FOR INSERT TO authenticated WITH CHECK (
    bucket_id = 'candidates' AND (SELECT is_candidate FROM public.users WHERE id = auth.uid()) = true
);
