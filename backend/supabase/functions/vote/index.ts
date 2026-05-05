// Supabase Edge Function: Anonymous Voting Logic
// Este é o "Tribunal Digital" que garante 1 pessoa = 1 voto anónimo

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  // Tratar requisições OPTIONS (CORS)
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? '' // Usa service_role para bypassar RLS interno
    )

    // 1. Obter dados do body
    const { proposal_id, vote_value, token_hash } = await req.json()

    // 2. Verificar se o token é válido e não foi usado
    const { data: tokenData, error: tokenError } = await supabase
      .from('voting_tokens')
      .select('*')
      .eq('token_hash', token_hash)
      .eq('proposal_id', proposal_id)
      .eq('used', false)
      .single()

    if (tokenError || !tokenData) {
      throw new Error("Token de votação inválido ou já utilizado.")
    }

    // 3. REGISTO DO VOTO (ANÓNIMO)
    // Inserimos na tabela 'votes' apenas com o hash do token, NUNCA com o user_id
    const { error: voteError } = await supabase
      .from('votes')
      .insert({
        proposal_id: proposal_id,
        vote_value: vote_value,
        voting_token_hash: token_hash
      })

    if (voteError) throw voteError

    // 4. QUEIMAR O TOKEN
    // Marcamos como usado para que o mesmo hash não possa votar outra vez
    await supabase
      .from('voting_tokens')
      .update({ used: true })
      .eq('id', tokenData.id)

    return new Response(JSON.stringify({ message: "Voto registado com sucesso e anonimizado." }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
      status: 200,
    })

  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
      status: 400,
    })
  }
})
