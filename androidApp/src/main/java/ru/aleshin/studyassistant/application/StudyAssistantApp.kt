package ru.aleshin.studyassistant.application

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.TrafficStats
import android.os.Build
import android.os.StrictMode
import androidx.annotation.RequiresApi
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationDefaults
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationImportance

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
class StudyAssistantApp : BaseFlavorApplication() {

    private val notificationManager: NotificationManager
        get() = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override fun initSettings() {
        setupStrictMode()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotifyChannel(
                channelId = Constants.Notification.CHANNEL_ID,
                channelName = Constants.Notification.CHANNEL_NAME,
                importance = NotificationImportance.MAX,
                defaults = NotificationDefaults()
            )
        }
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
        importance: NotificationImportance,
        defaults: NotificationDefaults,
    ) {
        val channel = NotificationChannel(channelId, channelName, importance.importance).apply {
            enableLights(defaults.isLights)
            enableVibration(defaults.isVibrate)
            vibrationPattern = longArrayOf(500, 500, 500)
        }
        notificationManager.createNotificationChannel(channel)
    }
}