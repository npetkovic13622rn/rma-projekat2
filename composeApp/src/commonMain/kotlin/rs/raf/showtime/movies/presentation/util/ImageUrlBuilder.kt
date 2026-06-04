package rs.raf.showtime.movies.presentation.util

object ImageUrlBuilder {
    fun posterUrl(path: String?, size: String = "w500"): String? =
        buildUrl(path = path, size = size)

    fun backdropUrl(path: String?, size: String = "w780"): String? =
        buildUrl(path = path, size = size)

    fun profileUrl(path: String?, size: String = "w185"): String? =
        buildUrl(path = path, size = size)

    private fun buildUrl(path: String?, size: String): String? {
        val trimmedPath = path?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (trimmedPath.startsWith("http")) return trimmedPath

        val normalizedPath = if (trimmedPath.startsWith("/")) trimmedPath else "/$trimmedPath"
        return "https://image.tmdb.org/t/p/$size$normalizedPath"
    }
}
