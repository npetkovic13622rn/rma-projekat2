package rs.raf.showtime

import androidx.compose.ui.window.ComposeUIViewController
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import rs.raf.showtime.app.ShowtimeApp
import rs.raf.showtime.di.initKoin

@Suppress("unused")
fun MainViewController(): platform.UIKit.UIViewController {
    Napier.base(DebugAntilog())
    initKoin()
    return ComposeUIViewController {
        ShowtimeApp()
    }
}
