package zdrowy.senior.io

import android.app.Application
import com.google.firebase.FirebaseApp
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import zdrowy.senior.io.di.appModule

class SeniorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)

        startKoin {
            androidContext(this@SeniorApp)
            modules(appModule)
        }
    }
}
