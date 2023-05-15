package io.github.mattpvaughn.chronicle.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable() () -> Unit
) {
    val systemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(
        color = Color.Transparent, darkIcons = !useDarkTheme
    )

    val context = LocalContext.current

    val colors = if (!useDarkTheme) {
        dynamicLightColorScheme(context)
    } else {
        dynamicDarkColorScheme(context)
    }

    MaterialTheme(
        colorScheme = colors, content = content
    )
}