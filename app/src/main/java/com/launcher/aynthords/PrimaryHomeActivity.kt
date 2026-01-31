package com.launcher.aynthords


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

class PrimaryHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // On any entry to "home", make sure we own the other screen too.
        ensureSecondaryDisplayActivity()

        setContent {
            val primaryIsWhite by ScreenSwapState.primaryIsWhite.collectAsState()
            val color = if (primaryIsWhite) Color.White else Color.Black

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .clickable { ScreenSwapState.toggle() }
            )
        }
    }

    override fun onResume() {
        super.onResume()
        // Aggressive restore: whenever we're visible, re-assert the dual-screen layout.
        ensureSecondaryDisplayActivity()
    }
}
