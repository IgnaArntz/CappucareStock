
package com.cappucare.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val CappucareDarkColorScheme = darkColorScheme(
    primary = VerdePrincipal,
    onPrimary = FondoPrincipal,

    secondary = VerdeSuave,
    onSecondary = FondoPrincipal,

    tertiary = ColorAdvertencia,
    onTertiary = FondoPrincipal,

    background = FondoPrincipal,
    onBackground = TextoPrincipal,

    surface = Superficie,
    onSurface = TextoPrincipal,

    surfaceVariant = FondoSecundario,
    onSurfaceVariant = TextoSecundario,

    outline = BordeSutil,
    error = ColorError,
    onError = FondoPrincipal
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CappucareDarkColorScheme,
        typography = Typography,
        content = content
    )
}
