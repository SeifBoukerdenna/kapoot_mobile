package com.log3990.kapoot.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.log3990.kapoot.ui.theme.KapootTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // If using Hilt
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            KapootTheme {
                MainApp()
            }
        }
    }
}
