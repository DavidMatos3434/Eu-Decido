package com.david.eudecido

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.david.eudecido.components.AIBottomSheetContent
import com.david.eudecido.screens.ai.GlobalAIScreenModel
import com.david.eudecido.screens.auth.WelcomeScreen
import kotlinx.coroutines.launch
import org.koin.compose.KoinContext
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App() {
    KoinContext {
        val aiScreenModel = koinInject<GlobalAIScreenModel>()
        val messages by aiScreenModel.messages.collectAsState()
        val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val scope = rememberCoroutineScope()

        MaterialTheme {
            // ✅ Box com systemBarsPadding envolve toda a app para evitar sobreposições
            Box(modifier = Modifier.systemBarsPadding()) {
                ModalBottomSheetLayout(
                    sheetState = sheetState,
                    sheetShape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
                    sheetContent = {
                        AIBottomSheetContent(
                            messages = messages,
                            onSend = { aiScreenModel.sendMessage(it) }
                        )
                    }
                ) {
                    Surface {
                        CompositionLocalProvider(LocalAIHandler provides {
                            scope.launch { sheetState.show() }
                        }) {
                            Navigator(WelcomeScreen()) { navigator ->
                                SlideTransition(navigator)
                            }
                        }
                    }
                }
            }
        }
    }
}

val LocalAIHandler = staticCompositionLocalOf<() -> Unit> { {} }
