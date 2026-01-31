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

class SecondaryHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val primaryIsWhite by ScreenSwapState.primaryIsWhite.collectAsState()
            // Secondary is always the opposite of primary in this v0 demo.
            val color = if (primaryIsWhite) Color.Black else Color.White

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
                    .clickable { ScreenSwapState.toggle() }
            )
        }
    }
}
