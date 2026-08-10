/*
 * Copyright 2026 Stanislav Aleshin
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
import org.jetbrains.compose.resources.getString
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoNotificationType
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoNotifications
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.todo_reminder_title_suffix as core_todo_reminder_title_suffix

/**
 * @author Stanislav Aleshin on 31.08.2024.
 */
class TodoReminderManagerImpl(
    private val notificationScheduler: NotificationScheduler,
    private val dateManager: DateManager,
) : TodoReminderManager {

    override suspend fun scheduleReminders(
        targetId: UID,
        name: String,
        deadline: Instant?,
        notifications: TodoNotifications
    ) {
        clearAllReminders(targetId)
        if (deadline != null) {
            val currentTime = dateManager.fetchCurrentInstant()
            val reminderTitle = getString(CoreRes.string.core_todo_reminder_title_suffix)
            notifications.toTypes().forEach { type ->
                val time = type.fetchNotifyTrigger(deadline)
                if (time > currentTime) {
                    val id = targetId.hashCode() + type.idAmount
                    notificationScheduler.scheduleNotification(id.toInt(), reminderTitle, name, time)
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
