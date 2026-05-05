package com.david.eudecido.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

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
        // 1. Seed Territories
        userRepository.addTerritory("t1", "Arroios", "FREGUESIA", "m1")
        userRepository.addTerritory("t2", "Lisboa", "MUNICIPIO", "r1")

        // 2. Seed User
        userRepository.insertUser(
            id = "current_user",
            identityId = "id_hash_david",
            username = "David Silva",
            email = "david@eudecido.pt",
            isCandidate = false
        )

        // 3. Seed Proposals
        proposalRepository.createProposal(
            id = "p1",
            userId = "current_user",
            territoryId = "t1",
            title = "Nova Ciclovia na Almirante Reis",
            description = "Proposta para criar uma ciclovia bidirecional segura, separada do tráfego automóvel.",
            status = "DISCUSSION"
        )
        
        proposalRepository.createProposal(
            id = "p2",
            userId = "admin",
            territoryId = "t1",
            title = "Horta Comunitária no Bairro",
            description = "Espaço para cultivo partilhado de vegetais e ervas aromáticas pelos residentes.",
            status = "VOTING"
        )

        // 4. Seed Eleições (NOVO)
        electionRepository.createElection(
            id = "e1",
            title = "Eleição para Representante de Freguesia",
            territoryId = "t1",
            role = "Delegado Comunitário",
            status = "OPEN"
        )

        // 5. Seed Representatives
        representativeRepository.addRepresentative(
            id = "r1",
            userId = "rep_1",
            territoryId = "t1",
            role = "Presidente da Junta"
        )

        // 6. Seed Notificações (Para testar o novo ecrã)
        notificationRepository.insertNotification(
            id = "n1",
            title = "Bem-vindo ao EU DECIDO",
            message = "Já podes começar a propor e votar nas decisões da tua freguesia."
        )
    }
}
