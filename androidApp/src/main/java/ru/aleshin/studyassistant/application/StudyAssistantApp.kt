package ru.aleshin.studyassistant.application

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.TrafficStats
import android.os.Build
import android.os.StrictMode
import androidx.annotation.RequiresApi
import com.google.firebase.FirebaseApp
import ru.aleshin.studyassistant.PlatformSDK
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationDefaults
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority
import ru.aleshin.studyassistant.di.PlatformConfiguration
import ru.aleshin.studyassistant.presentation.services.RemoteMessageHandlerImpl

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
class StudyAssistantApp : Application() {

    private val googleAuthTokenProvider by lazy {
        GoogleAuthTokenProvider(applicationContext)
    }

    private val pushClientFactory by lazy {
        UniversalPushClientFactory.Base(applicationContext)
    }

    private val remoteMessageHandler by lazy {
        RemoteMessageHandlerImpl(applicationContext)
    }

    private val notificationManager: NotificationManager
        get() = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun onCreate() {
        super.onCreate()
        initPlatformSDK()
        setupStrictMode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotifyChannel(
                channelId = Constants.Notification.CHANNEL_ID,
                channelName = Constants.Notification.CHANNEL_NAME,
                priority = NotificationPriority.MAX,
                defaults = NotificationDefaults()
            )
        }
    }

    private fun initPlatformSDK() {
        // val options = FirebaseOptions.Builder().build() TODO: Set FirebaseOptions
        FirebaseApp.initializeApp(this)

        pushClientFactory.createPushClient()

        PlatformSDK.doInit(
            configuration = PlatformConfiguration(
                serviceTokenProvider = googleAuthTokenProvider,
                remoteMessageHandler = remoteMessageHandler,
                applicationContext = applicationContext,
            )
        )
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
            in Build.VERSION_CODES.O..Build.VERSION_CODES.Q ->
                vmPolicyBuilder
                    .detectContentUriWithoutPermission()

            in Build.VERSION_CODES.Q..Build.VERSION_CODES.S ->
                vmPolicyBuilder
                    .detectContentUriWithoutPermission()
                    .detectCredentialProtectedWhileLocked()
                    .detectImplicitDirectBoot()

            in Build.VERSION_CODES.S..Int.MAX_VALUE ->
                vmPolicyBuilder
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotifyChannel(
        channelId: String,
        channelName: String,
        priority: NotificationPriority,
        defaults: NotificationDefaults,
    ) {
        val channel = NotificationChannel(channelId, channelName, priority.importance).apply {
            enableLights(defaults.isLights)
            enableVibration(defaults.isVibrate)
            vibrationPattern = longArrayOf(500, 500, 500)
        }
        notificationManager.createNotificationChannel(channel)
    }
}