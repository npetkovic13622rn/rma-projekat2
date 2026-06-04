package rs.raf.showtime

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import rs.raf.showtime.app.ShowtimeApp
import rs.raf.showtime.di.initKoin

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Showtime",
        ) {
            ShowtimeApp()
        }
    }
}
