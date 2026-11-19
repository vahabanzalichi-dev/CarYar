package com.vahab.caryar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.vahab.caryar.navigation.CarYarNavHost
import com.vahab.caryar.ui.theme.CarYarTheme
import androidx.compose.runtime.CompositionLocalProvider

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CarYarTheme {
                // Force RTL for Persian
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    CarYarAppRoot()
                }
            }
        }
    }
}

@Composable
fun CarYarAppRoot() {
    val navController = rememberNavController()
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        CarYarNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
