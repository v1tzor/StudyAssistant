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
package ru.aleshin.studyassistant.data

import ru.aleshin.studyassistant.android.BuildConfig
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.ok.tracer.CoreTracerConfiguration
import ru.ok.tracer.Tracer
import ru.ok.tracer.TracerConfiguration
import ru.ok.tracer.crash.report.CrashFreeConfiguration
import ru.ok.tracer.crash.report.CrashReportConfiguration
import ru.ok.tracer.crash.report.TracerCrashReport
import ru.ok.tracer.disk.usage.DiskUsageConfiguration
import ru.ok.tracer.heap.dumps.HeapDumpConfiguration

/**
 * @author Stanislav Aleshin on 13.04.2025.
 */
class CrashlyticsServiceImpl : CrashlyticsService {

    override fun sendLog(message: String) {
        TracerCrashReport.log(message)
    }

    override fun recordException(tag: String, message: String, exception: Throwable) {
        TracerCrashReport.report(exception, tag)
    }

    override fun setupUser(id: UID?) {
        Tracer.setUserId(id)
    }

    override fun initializeService() = Unit

    companion object {
        val tracerConfiguration: List<TracerConfiguration>
            get() = listOf(
                CoreTracerConfiguration.build {
                    setDebugUpload(BuildConfig.DEBUG)
                },
                CrashReportConfiguration.build {
                    setNativeEnabled(true)
                    setSendAnr(true)
                },
                CrashFreeConfiguration.build {
                    setEnabled(true)
                },
                HeapDumpConfiguration.build {
                    setEnabled(true)
                },
                DiskUsageConfiguration.build {
                    setEnabled(true)
                    setInterestingSize(3L * 1024 * 1024 * 1024)
                    setProbability(100)
                },
            )
    }
}