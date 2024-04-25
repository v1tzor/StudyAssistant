package ru.aleshin.studyassistant.application

import android.app.Application
import android.net.TrafficStats
import android.os.Build
import android.os.StrictMode
import ru.aleshin.studyassistant.PlatformSDK
import ru.aleshin.studyassistant.di.PlatformConfiguration

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
class StudyAssistantApp : Application() {

    private val configuration by lazy {
        PlatformConfiguration(applicationContext)
    }

    private fun initPlatformSDK() = PlatformSDK.doInit(configuration)

    override fun onCreate() {
        super.onCreate()
        initPlatformSDK()
        setupStrictMode()
    }

    private fun setupStrictMode() {
        val vmPolicyBuilder = StrictMode.VmPolicy.Builder()
            .detectActivityLeaks()
            .detectCleartextNetwork()
            .detectFileUriExposure()
            .detectLeakedClosableObjects()
            .detectLeakedRegistrationObjects()
            .detectLeakedSqlLiteObjects()
            .penaltyLog()

        val vmPolicy = when (Build.VERSION.SDK_INT) {
            in Build.VERSION_CODES.O..Build.VERSION_CODES.Q -> vmPolicyBuilder
                .detectContentUriWithoutPermission()

            in Build.VERSION_CODES.Q..Build.VERSION_CODES.S -> vmPolicyBuilder
                .detectContentUriWithoutPermission()
                .detectCredentialProtectedWhileLocked()
                .detectImplicitDirectBoot()

            in Build.VERSION_CODES.S..Int.MAX_VALUE -> vmPolicyBuilder
                .detectContentUriWithoutPermission()
                .detectCredentialProtectedWhileLocked()
                .detectImplicitDirectBoot()
                .detectIncorrectContextUse()
                .detectUnsafeIntentLaunch()

            else -> vmPolicyBuilder
        }.build()

        val threadPolicy = StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .penaltyLog()
            .build()

        StrictMode.setThreadPolicy(threadPolicy)
        StrictMode.setVmPolicy(vmPolicy)
        TrafficStats.setThreadStatsTag(Thread.currentThread().id.toInt())
    }
}