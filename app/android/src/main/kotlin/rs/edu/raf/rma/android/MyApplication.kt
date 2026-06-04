package rs.raf.showtime.android

import android.app.Application
import android.util.Log
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import rs.raf.showtime.AppContextHolder
import rs.raf.showtime.di.initKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContextHolder.init(this.applicationContext)
        Napier.base(DebugAntilog())
        Log.d("Test", "App:onCreate()")
        initKoin {
            androidContext(this@MyApplication)
        }
    }
}
