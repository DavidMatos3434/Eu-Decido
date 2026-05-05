package com.david.eudecido.tabs

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.david.eudecido.screens.home.HomeScreen
import com.david.eudecido.screens.search.SearchScreen
import com.david.eudecido.screens.notifications.NotificationsScreen
import com.david.eudecido.screens.profile.ProfileScreen

internal object HomeTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 0u, title = "Início")

    @Composable
    override fun Content() {
        Navigator(HomeScreen())
    }
}

internal object SearchTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 1u, title = "Explorar")

    @Composable
    override fun Content() {
        Navigator(SearchScreen())
    }
}

internal object NotificationsTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 2u, title = "Alertas")

    @Composable
    override fun Content() {
        Navigator(NotificationsScreen())
    }
}

internal object ProfileTab : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(index = 3u, title = "Perfil")

    @Composable
    override fun Content() {
        Navigator(ProfileScreen())
    }
}
