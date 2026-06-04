package rs.raf.showtime

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform