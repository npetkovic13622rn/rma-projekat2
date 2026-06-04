package rs.raf.showtime.core.auth

import rs.raf.showtime.AppContextHolder

private const val AUTH_DATA_FILE_NAME = "auth_data.json"

actual fun createAuthDataStorePath(): String {
    val context = AppContextHolder.appContext
    return context.filesDir.resolve("datastore/$AUTH_DATA_FILE_NAME").absolutePath
}