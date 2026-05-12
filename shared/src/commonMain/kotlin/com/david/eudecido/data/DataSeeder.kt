package com.david.eudecido.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DataSeeder(
    private val userRepository: UserRepository,
    private val proposalRepository: ProposalRepository,
    private val candidateRepository: CandidateRepository,
    private val representativeRepository: RepresentativeRepository,
    private val electionRepository: ElectionRepository,
    private val notificationRepository: NotificationRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    fun seedIfNeeded() {
        scope.launch {
            val territories = userRepository.getTerritories().first()
            if (territories.isEmpty()) {
                seedData()
            }
        }
    }

    private suspend fun seedData() {
        // Inserir apenas os territórios base necessários para a app funcionar.
        // Toda a informação de propostas e utilizadores virá agora do Supabase.
        userRepository.addTerritory("00000000-0000-0000-0000-000000000001", "Portugal", "NACIONAL", null)
        userRepository.addTerritory("00000000-0000-0000-0000-000000000002", "Lisboa e Vale do Tejo", "REGIAO", "00000000-0000-0000-0000-000000000001")
        userRepository.addTerritory("00000000-0000-0000-0000-000000000003", "Lisboa", "MUNICIPIO", "00000000-0000-0000-0000-000000000002")
        userRepository.addTerritory("00000000-0000-0000-0000-000000000004", "Arroios", "FREGUESIA", "00000000-0000-0000-0000-000000000003")
    }
}
