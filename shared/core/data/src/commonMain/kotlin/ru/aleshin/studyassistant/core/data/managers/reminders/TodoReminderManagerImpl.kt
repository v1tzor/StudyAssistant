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

package ru.aleshin.studyassistant.core.data.managers.reminders

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoNotificationType
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoNotifications
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchCoreStrings

/**
 * @author Stanislav Aleshin on 31.08.2024.
 */
class TodoReminderManagerImpl(
    private val notificationScheduler: NotificationScheduler,
    private val deviceInfoProvider: DeviceInfoProvider,
    private val dateManager: DateManager,
) : TodoReminderManager {

    override fun scheduleReminders(
        targetId: UID,
        name: String,
        deadline: Instant?,
        notifications: TodoNotifications
    ) {
        clearAllReminders(targetId)
        if (deadline != null) {
            val currentTime = dateManager.fetchCurrentInstant()
            val deviceLanguage = deviceInfoProvider.fetchDeviceLanguage()
            val coreStrings = fetchCoreStrings(fetchAppLanguage(deviceLanguage))
            notifications.toTypes().forEach { type ->
                val time = type.fetchNotifyTrigger(deadline)
                if (time > currentTime) {
                    val id = targetId.hashCode() + type.idAmount
                    val title = coreStrings.todoReminderTitleSuffix
                    notificationScheduler.scheduleNotification(id.toInt(), title, name, time)
                }
            }
        }
    }

    override fun clearAllReminders(targetId: UID) {
        TodoNotificationType.entries.forEach { type ->
            val id = targetId.hashCode() + type.idAmount
            notificationScheduler.cancelNotification(id.toInt())
        }
    }
}