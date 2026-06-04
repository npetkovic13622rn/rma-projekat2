package rs.raf.showtime.core.db

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import rs.raf.showtime.core.db.converters.DateConverters
import rs.raf.showtime.favorites.data.local.dao.FavoritesDao
import rs.raf.showtime.favorites.data.local.entity.FavoriteEntity
import rs.raf.showtime.movies.data.local.dao.MoviesDao
import rs.raf.showtime.movies.data.local.entity.CastMemberEntity
import rs.raf.showtime.movies.data.local.entity.GenreEntity
import rs.raf.showtime.movies.data.local.entity.MovieDetailsEntity
import rs.raf.showtime.movies.data.local.entity.MovieEntity
import rs.raf.showtime.movies.data.local.entity.MovieGenreCrossRefEntity
import rs.raf.showtime.movies.data.local.entity.MovieImageEntity
import rs.raf.showtime.movies.data.local.entity.MovieVideoEntity
import rs.raf.showtime.profile.data.local.dao.ProfileDao
import rs.raf.showtime.profile.data.local.entity.LocalUserEntity
import rs.raf.showtime.quiz.data.local.dao.QuizDao
import rs.raf.showtime.quiz.data.local.entity.QuizSessionEntity
import rs.raf.showtime.quiz.data.local.entity.QuizStatsEntity
import rs.raf.showtime.watchlist.data.local.dao.WatchlistDao
import rs.raf.showtime.watchlist.data.local.entity.WatchlistEntity

@Database(
    entities = [
        MovieEntity::class,
        MovieDetailsEntity::class,
        GenreEntity::class,
        MovieGenreCrossRefEntity::class,
        CastMemberEntity::class,
        MovieImageEntity::class,
        MovieVideoEntity::class,
        FavoriteEntity::class,
        WatchlistEntity::class,
        QuizStatsEntity::class,
        QuizSessionEntity::class,
        LocalUserEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
@TypeConverters(DateConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun moviesDao(): MoviesDao
    abstract fun favoritesDao(): FavoritesDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun quizDao(): QuizDao
    abstract fun profileDao(): ProfileDao
}

// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

fun buildAppDatabase(
    builder: RoomDatabase.Builder<AppDatabase>,
): AppDatabase {
    return builder
        .fallbackToDestructiveMigrationOnDowngrade(dropAllTables = true)
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}
