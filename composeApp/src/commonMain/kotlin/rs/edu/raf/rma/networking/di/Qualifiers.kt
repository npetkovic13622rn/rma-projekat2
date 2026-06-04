package rs.raf.showtime.networking.di

import org.koin.core.qualifier.named

object Qualifiers {
    val Authenticated = named("Authenticated")
    val Unauthenticated = named("Unauthenticated")
}
