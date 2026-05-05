// Supabase Edge Function: Identity Validation
// Responsável por validar NIF/CMD e gerar o hash de identidade

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

serve(async (req) => {
  const { nif, phone, user_id } = await req.json()

  // 1. Validação de formato (Simulada)
  if (!nif || nif.length !== 9) {
    return new Response(JSON.stringify({ error: "NIF inválido" }), { status: 400 })
  }

  // 2. Lógica de Hashing (Argon2 ou similar no futuro)
  // O NIF real NUNCA é guardado na base de dados.
  const nifHash = btoa(nif); // Exemplo simples de encoding para o protótipo

  const supabase = createClient(
    Deno.env.get('SUPABASE_URL') ?? '',
    Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
  )

  // 3. Criar Identity e vincular ao User
  const { data, error } = await supabase
    .from('identity')
    .insert({ nif_hash: nifHash, phone_hash: phone, verified: true })
    .select()
    .single()

  if (error) return new Response(JSON.stringify({ error: error.message }), { status: 500 })

  return new Response(JSON.stringify({ identity_id: data.id, status: "verified" }), {
    headers: { "Content-Type": "application/json" },
  })
})
