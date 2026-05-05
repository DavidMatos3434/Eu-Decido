package com.david.eudecido

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Ativa o modo Edge-to-Edge para que a app ocupe todo o ecrã
        enableEdgeToEdge()
        
        setContent {
            App()
        }
    }
}
