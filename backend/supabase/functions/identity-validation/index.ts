import { serve } from "https://deno.land/std@0.168.0/http/server.ts"
import { createClient } from "https://esm.sh/@supabase/supabase-js@2"

const corsHeaders = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Headers': 'authorization, x-client-info, apikey, content-type',
}

async function sha256Hex(value: string): Promise<string> {
  const buffer = await crypto.subtle.digest('SHA-256', new TextEncoder().encode(value))
  return Array.from(new Uint8Array(buffer))
    .map(b => b.toString(16).padStart(2, '0'))
    .join('')
}

serve(async (req) => {
  if (req.method === 'OPTIONS') {
    return new Response('ok', { headers: corsHeaders })
  }

  try {
    const { nif, phone, user_id } = await req.json()

    if (!nif || nif.length !== 9) {
      return new Response(JSON.stringify({ error: "NIF inválido" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      })
    }

    if (!phone) {
      return new Response(JSON.stringify({ error: "Telemóvel inválido" }), {
        status: 400,
        headers: { ...corsHeaders, "Content-Type": "application/json" }
      })
    }

    const nifHash = await sha256Hex(nif)
    const phoneHash = await sha256Hex(phone)

    const supabase = createClient(
      Deno.env.get('SUPABASE_URL') ?? '',
      Deno.env.get('SUPABASE_SERVICE_ROLE_KEY') ?? ''
    )

    const { data, error } = await supabase
      .from('identity')
      .insert({ nif_hash: nifHash, phone_hash: phoneHash, verified: true })
      .select()
      .single()

    if (error) return new Response(JSON.stringify({ error: error.message }), {
      status: 500,
      headers: { ...corsHeaders, "Content-Type": "application/json" }
    })

    return new Response(JSON.stringify({ identity_id: data.id, status: "verified" }), {
      headers: { ...corsHeaders, "Content-Type": "application/json" },
    })
  } catch (error) {
    return new Response(JSON.stringify({ error: error.message }), {
      status: 400,
      headers: { ...corsHeaders, "Content-Type": "application/json" }
    })
  }
})
