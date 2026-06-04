package rs.raf.showtime.core.db.di

import rs.raf.showtime.core.db.AppDatabase
import rs.raf.showtime.core.db.buildAppDatabase
import rs.raf.showtime.core.db.getDatabaseBuilder
import org.koin.dsl.module

actual fun databaseModule() = module {
    single<AppDatabase> {
        buildAppDatabase(
            builder = getDatabaseBuilder(
                context = get(),
            )
        )
    }
}
