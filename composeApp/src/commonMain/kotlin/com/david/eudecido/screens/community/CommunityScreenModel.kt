package com.david.eudecido.screens.community

import cafe.adriel.voyager.core.model.ScreenModel
import com.david.eudecido.models.CommunityItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CommunityScreenModel(val locationName: String) : ScreenModel {
    private val _items = MutableStateFlow<List<CommunityItem>>(emptyList())
    val items = _items.asStateFlow()

    init {
        // Mock data
        _items.value = listOf(
            CommunityItem("Assembleia de Freguesia", "Consulta as atas e próximas reuniões."),
            CommunityItem("Eventos Locais", "Fica a par do que acontece na tua comunidade."),
            CommunityItem("Orçamento Participativo", "Acompanha a execução dos projetos vencedores."),
            CommunityItem("Relatórios de Transparência", "Acesso aberto às contas e decisões locais.")
        )
    }
}
