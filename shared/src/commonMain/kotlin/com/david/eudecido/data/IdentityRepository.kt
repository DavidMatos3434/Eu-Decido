package com.david.eudecido.data

import kotlinx.coroutines.flow.Flow

interface IdentityRepository {
    /**
     * Gera um hash seguro a partir do NIF.
     * O NIF real nunca deve ser armazenado.
     */
    fun hashNif(nif: String): String

    /**
     * Solicita ao backend (Supabase) a criação de uma identidade 
     * e o retorno do primeiro voting token.
     */
    suspend fun verifyAndRegisterIdentity(nifHash: String): String?
}
