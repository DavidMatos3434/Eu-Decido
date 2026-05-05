-- EU DECIDO: Dados de Teste e Configuração Inicial

-- 1. TERRITÓRIOS (Hierarquia)
-- Portugal (Nacional)
INSERT INTO territories (id, name, type) 
VALUES ('00000000-0000-0000-0000-000000000001', 'Portugal', 'NACIONAL');

-- Lisboa (Município)
INSERT INTO territories (id, name, type, parent_id) 
VALUES ('00000000-0000-0000-0000-000000000002', 'Lisboa', 'MUNICIPIO', '00000000-0000-0000-0000-000000000001');

-- Arroios (Freguesia)
INSERT INTO territories (id, name, type, parent_id) 
VALUES ('00000000-0000-0000-0000-000000000003', 'Arroios', 'FREGUESIA', '00000000-0000-0000-0000-000000000002');


-- 2. PROPOSTA DE EXEMPLO (Tipo IDEIA)
INSERT INTO proposals (id, title, description, territory_id, status, type)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'Nova Ciclovia Almirante Reis',
    'Proposta para criar uma via segura e separada para bicicletas e trotinetes.',
    '00000000-0000-0000-0000-000000000003',
    'DISCUSSION',
    'IDEIA'
);

-- 3. PROPOSTA DE ELEIÇÃO (Para testar o gatilho automático)
INSERT INTO proposals (id, title, description, territory_id, status, type)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    'Eleição para Conselho de Moradores de Arroios',
    'Abertura de processo para escolher 3 representantes locais.',
    '00000000-0000-0000-0000-000000000003',
    'VOTING',
    'ELECTION_REQUEST'
);

-- Nota: Quando esta proposta passar para 'APPROVED', o Trigger criará automaticamente a Eleição.
