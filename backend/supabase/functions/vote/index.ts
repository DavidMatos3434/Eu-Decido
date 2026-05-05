// Supabase Edge Function: Anonymous Voting Logic
// Este é o "Tribunal Digital" que garante 1 pessoa = 1 voto anónimo

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    const { proposal_id, vote_value, token_hash } = await req.json()

    if (!proposal_id || !vote_value || !token_hash) {
      throw new Error("Parâmetros em falta: proposal_id, vote_value e token_hash são obrigatórios.")
    }

    // PASSO 1: QUEIMAR O TOKEN ATOMICAMENTE
    // Fazemos o update com a condição used=false numa única operação.
    // Se dois pedidos chegarem ao mesmo tempo, apenas um conseguirá
    // mudar used de false para true — o outro receberá 0 linhas.
    const { data: burnedToken, error: burnError } = await supabase
      .from('voting_tokens')
      .update({ used: true })
      .eq('token_hash', token_hash)
      .eq('proposal_id', proposal_id)
      .eq('used', false)
      .select('id')
      .single()

    if (burnError || !burnedToken) {
      throw new Error("Token de votação inválido ou já utilizado.")
    }

    // PASSO 2: REGISTAR O VOTO (ANÓNIMO)
    // O token já está queimado. Inserimos o voto apenas com o hash,
    // NUNCA com o user_id, garantindo o anonimato.
    const { error: voteError } = await supabase
      .from('votes')
      .insert({
        proposal_id: proposal_id,
        vote_value: vote_value,
        voting_token_hash: token_hash
      })

    if (voteError) throw voteError

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
