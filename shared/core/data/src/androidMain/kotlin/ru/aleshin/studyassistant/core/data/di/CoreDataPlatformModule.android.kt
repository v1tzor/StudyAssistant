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

package ru.aleshin.studyassistant.core.data.di

import android.app.AlarmManager
import android.content.Context
import androidx.work.WorkManager
import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.data.managers.EndClassesReminderManagerImpl
import ru.aleshin.studyassistant.core.data.managers.HomeworksReminderManagerImpl
import ru.aleshin.studyassistant.core.data.managers.NotificationScheduler
import ru.aleshin.studyassistant.core.data.managers.StartClassesReminderManagerImpl
import ru.aleshin.studyassistant.core.data.managers.WorkloadWarningManagerImpl
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.WorkloadWarningManager

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
actual val coreDataPlatformModule = DI.Module("CoreDataPlatform") {
    bindSingleton<WorkManager> { WorkManager.getInstance(instance<Context>()) }
    bindSingleton<AlarmManager> { instance<Context>().getSystemService(AlarmManager::class.java) }
    bindProvider<WorkloadWarningManager> { WorkloadWarningManagerImpl(instance(), instance()) }
    bindProvider<HomeworksReminderManager> { HomeworksReminderManagerImpl(instance(), instance()) }
    bindProvider<StartClassesReminderManager> { StartClassesReminderManagerImpl(instance(), instance(), instance(), instance()) }
    bindProvider<EndClassesReminderManager> { EndClassesReminderManagerImpl(instance(), instance(), instance(), instance()) }
    bindSingleton<NotificationScheduler> { NotificationScheduler(instance(), instance(), instance()) }
}