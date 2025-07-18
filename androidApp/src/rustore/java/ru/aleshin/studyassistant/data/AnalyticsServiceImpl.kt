/*
 * Copyright 2023 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.aleshin.studyassistant.data

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.provider.Settings.Secure
import com.my.tracker.MyTracker
import ru.aleshin.studyassistant.android.BuildConfig
import ru.aleshin.studyassistant.core.common.platform.services.AnalyticsService

/**
 * @author Stanislav Aleshin on 13.04.2025.
 */
class AnalyticsServiceImpl(
    private val context: Context,
) : AnalyticsService {

    override fun trackEvent(name: String, eventParams: Map<String, String>) {
        MyTracker.trackEvent(name, eventParams)
    }

    override fun initializeService() {
        MyTracker.getTrackerParams().apply {
            setCustomParam("android_id", getAndroidId(context))
            setCustomParam("store", "rustore")
        }
        MyTracker.initTracker(BuildConfig.MY_TRACKER_KEY, (context.applicationContext as Application))
    }

    @SuppressLint("HardwareIds")
    private fun getAndroidId(context: Context): String? {
        return Secure.getString(context.contentResolver, Secure.ANDROID_ID)
    }
}