package zdrowy.senior.io

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import zdrowy.senior.io.di.appModules

class SeniorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Timber.treeCount() == 0) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@SeniorApp)
            modules(appModules)
        }
    }
}
