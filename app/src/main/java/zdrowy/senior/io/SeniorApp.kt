package zdrowy.senior.io

import android.app.Application
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import zdrowy.senior.io.di.appModules

class SeniorApp : Application() {
    override fun onCreate() {
        super.onCreate()

        if (Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }

        initFirebase()

        startKoin {
            androidContext(this@SeniorApp)
            modules(appModules)
        }
    }

    private fun initFirebase() {
        // Prefer standard initialization via google-services.json if present.
        // For local dev (DEBUG) we fall back to an explicit FirebaseOptions so the app can run
        // with the Auth Emulator Suite even without google-services.json committed.
        if (FirebaseApp.getApps(this).isEmpty()) {
            val app = FirebaseApp.initializeApp(this)
            if (app == null && BuildConfig.DEBUG) {
                val options = FirebaseOptions.Builder()
                    .setProjectId("demo-senior-app")
                    .setApplicationId("1:1234567890:android:1234567890abcdef")
                    .setApiKey("fake-api-key")
                    .build()
                FirebaseApp.initializeApp(this, options)
            }
        }

        if (BuildConfig.USE_FIREBASE_AUTH_EMULATOR) {
            val auth = FirebaseAuth.getInstance()
            auth.useEmulator(
                BuildConfig.FIREBASE_AUTH_EMULATOR_HOST,
                BuildConfig.FIREBASE_AUTH_EMULATOR_PORT
            )
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        }

        if (BuildConfig.USE_FIREBASE_FIRESTORE_EMULATOR) {
            FirebaseFirestore.getInstance().useEmulator(
                BuildConfig.FIREBASE_FIRESTORE_EMULATOR_HOST,
                BuildConfig.FIREBASE_FIRESTORE_EMULATOR_PORT
            )
        }
    }
}
