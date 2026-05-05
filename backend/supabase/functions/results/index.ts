// Supabase Edge Function: Results Aggregator
// Calcula estatísticas de votação em tempo real garantindo o anonimato

import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

serve(async (req) => {
  if (req.method === 'OPTIONS') return new Response('ok', { headers: corsHeaders })

  try {
    const { proposal_id, election_id } = await req.json()
    
    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    let results = {}

    if (proposal_id) {
      const { data, error } = await supabase
        .from('votes')
        .select('vote_value')
        .eq('proposal_id', proposal_id)

      if (error) throw error

      const total = data.length
      const yes = data.filter(v => v.vote_value === 'SIM').length
      const no = data.filter(v => v.vote_value === 'NAO').length
      const abs = data.filter(v => v.vote_value === 'ABSTENCAO').length

      // Calculamos yes e no normalmente; abstencao recebe o resto para que
      // a soma seja sempre exactamente 100 quando total > 0.
      const yesPercent = total > 0 ? Math.round((yes / total) * 100) : 0
      const noPercent = total > 0 ? Math.round((no / total) * 100) : 0
      const absPercent = total > 0 ? 100 - yesPercent - noPercent : 0

      results = {
        type: 'PROPOSAL',
        total,
        stats: { yes, no, abstention: abs },
        percentage: {
          yes: yesPercent,
          no: noPercent,
          abstention: absPercent
        }
      }
    } else if (election_id) {
      const { data, error } = await supabase
        .from('votes')
        .select('vote_value')
        .eq('election_id', election_id)

      if (error) throw error

      const counts = data.reduce((acc: any, vote) => {
        acc[vote.vote_value] = (acc[vote.vote_value] || 0) + 1
        return acc
      }, {})

      results = {
        type: 'ELECTION',
        total: data.length,
        candidates: counts
      }
    } else {
      throw new Error("É necessário fornecer proposal_id ou election_id.")
    }

    return new Response(JSON.stringify(results), {
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
