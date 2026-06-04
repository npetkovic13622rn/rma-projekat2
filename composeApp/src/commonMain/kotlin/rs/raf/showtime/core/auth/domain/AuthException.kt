package rs.raf.showtime.core.auth.domain

class AuthException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
