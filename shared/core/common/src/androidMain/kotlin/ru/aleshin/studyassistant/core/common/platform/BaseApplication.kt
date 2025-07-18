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

package ru.aleshin.studyassistant.core.common.platform

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import ru.aleshin.studyassistant.core.common.di.MainDirectDIAware

/**
 * @author Stanislav Aleshin on 14.04.2025.
 */
abstract class BaseApplication : Application(), MainDirectDIAware {

    protected val job = Job()

    protected val applicationScope = CoroutineScope(job + Dispatchers.Main)

    abstract fun initPlatformServices()

    abstract fun initSettings()

    override fun onCreate() {
        super.onCreate()
        initPlatformServices()
        initSettings()
    }

    override fun onTerminate() {
        super.onTerminate()
        applicationScope.cancel(null)
    }
}