package ru.aleshin.studyassistant.application

import android.app.Application
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
    }
}