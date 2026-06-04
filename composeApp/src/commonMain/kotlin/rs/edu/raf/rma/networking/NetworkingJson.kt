package rs.raf.showtime.networking

import kotlinx.serialization.json.Json

val NetworkingJson = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}
