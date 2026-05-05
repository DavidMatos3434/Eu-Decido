package com.david.eudecido.data

import kotlinx.coroutines.delay

class IdentityRepositoryImpl : IdentityRepository {

    override fun hashNif(nif: String): String {
        // Implementação simplificada de hash para o protótipo.
        // Numa app real, usaríamos uma biblioteca como BCrypt ou Argon2 via KMP.
        return "hash_" + nif.hashCode().toString()
    }

    override suspend fun verifyAndRegisterIdentity(nifHash: String): String? {
        // Simulação de chamada ao Backend (Supabase)
        delay(1500)
        
        // Retorna um voting_token inicial se a identidade for válida/nova
        return "initial_token_${nifHash.takeLast(4)}"
    }
}
