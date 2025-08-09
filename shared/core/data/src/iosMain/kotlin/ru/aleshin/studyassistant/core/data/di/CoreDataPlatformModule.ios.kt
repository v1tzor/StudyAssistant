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

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import platform.UserNotifications.UNUserNotificationCenter
import ru.aleshin.studyassistant.core.data.managers.reminders.EndClassesReminderManagerImpl
import ru.aleshin.studyassistant.core.data.managers.reminders.HomeworksReminderManagerImpl
import ru.aleshin.studyassistant.core.data.managers.reminders.NotificationScheduler
import ru.aleshin.studyassistant.core.data.managers.reminders.StartClassesReminderManagerImpl
import ru.aleshin.studyassistant.core.data.managers.reminders.WorkloadWarningManagerImpl
import ru.aleshin.studyassistant.core.data.managers.sync.SyncWorkManagerImpl
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.managers.sync.SyncWorkManager

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
actual val coreDataPlatformModule = DI.Module("CoreDataPlatform") {
    bindProvider<SyncWorkManager> { SyncWorkManagerImpl() }
    bindProvider<WorkloadWarningManager> { WorkloadWarningManagerImpl() }
    bindProvider<HomeworksReminderManager> { HomeworksReminderManagerImpl() }
    bindProvider<StartClassesReminderManager> { StartClassesReminderManagerImpl() }
    bindProvider<EndClassesReminderManager> { EndClassesReminderManagerImpl() }
    bindSingleton<NotificationScheduler> {
        NotificationScheduler(UNUserNotificationCenter.currentNotificationCenter(), instance())
    }
}