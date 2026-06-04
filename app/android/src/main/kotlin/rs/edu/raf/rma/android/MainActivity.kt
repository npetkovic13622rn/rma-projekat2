package rs.raf.showtime.android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import rs.raf.showtime.app.ShowtimeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Log.d("Test", "Main:onCreate()")
        setContent {
            ShowtimeApp()
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d("Test", "Main:onStart()")
    }

    override fun onResume() {
        super.onResume()
        Log.d("Test", "Main:onResume()")
    }

    override fun onPause() {
        super.onPause()
        Log.d("Test", "Main:onPause()")
    }

    override fun onStop() {
        super.onStop()
        Log.d("Test", "Main:onStop()")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("Test", "Main:onDestroy()")
    }
}
