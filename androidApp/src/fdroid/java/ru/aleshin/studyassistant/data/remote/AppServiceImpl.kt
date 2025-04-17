/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.data.remote

import android.content.Context
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import ru.aleshin.studyassistant.android.BuildConfig
import ru.aleshin.studyassistant.core.common.inject.AppService
import ru.aleshin.studyassistant.core.common.inject.Flavor

/**
 * @author Stanislav Aleshin on 11.09.2024.
 */
class AppServiceImpl(
    private val applicationContext: Context,
    private val googleApiAvailability: GoogleApiAvailability,
) : AppService {

    override val flavor: Flavor = Flavor.GITHUB

    override val isAvailableServices: Boolean = false

    override fun initializeApp() {
        val options = FirebaseOptions.Builder().apply {
            setApplicationId(BuildConfig.APPLICATION_ID)
            setProjectId(BuildConfig.PROJECT_ID)
            setStorageBucket(BuildConfig.STORAGE_BUCKET)
            setApiKey(BuildConfig.FIREBASE_API_KEY)
        }.build()
        FirebaseApp.initializeApp(applicationContext, options)
    }
}