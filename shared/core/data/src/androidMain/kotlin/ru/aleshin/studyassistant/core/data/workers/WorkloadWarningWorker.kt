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

package ru.aleshin.studyassistant.core.data.workers

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import androidx.core.net.toUri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.R
import ru.aleshin.studyassistant.core.common.di.MainDirectDIAware
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.notifications.NotificationCreator
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationCategory
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyWorkload
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.high_workload_warning_body as core_high_workload_warning_body
import ru.aleshin.studyassistant.core.ui.resources.high_workload_warning_title as core_high_workload_warning_title

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
class WorkloadWarningWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), MainDirectDIAware {
    private val dateManager = instance<DateManager>()
    private val notificationCreator = instance<NotificationCreator>()
    private val calendarSettingsRepository = instance<CalendarSettingsRepository>()
    private val notificationSettingsRepository = instance<NotificationSettingsRepository>()
    private val homeworksRepository = instance<HomeworksRepository>()
    private val todoRepository = instance<TodoRepository>()
    private val baseScheduleRepository = instance<BaseScheduleRepository>()
    private val customScheduleRepository = instance<CustomScheduleRepository>()

    override suspend fun doWork(): Result {
        val warningTitle = getString(CoreRes.string.core_high_workload_warning_title)
        val warningBody = getString(CoreRes.string.core_high_workload_warning_body)
        val targetDate = dateManager.fetchBeginningCurrentInstant().shiftDay(1)

        val notificationSettings = notificationSettingsRepository.fetchSettings().first()
        val maxWorkloadValue = notificationSettings.highWorkload ?: return Result.success()

        val value = fetchDailyWorkload(targetDate)
        notificationCreator.cancelNotify(WORKLOAD_NOTIFICATION_ID)
        if (value.isHigh(maxWorkloadValue)) {
            val mainActivityUri = Constants.App.OPEN_APP_DEEPLINK.toUri()
            val contentIntent = Intent(ACTION_VIEW, mainActivityUri)
            val pContentIntent =
                PendingIntent.getActivity(context, WORKLOAD_NOTIFICATION_ID, contentIntent, FLAG_IMMUTABLE)
            val notify = notificationCreator.createNotify(
                channelId = Constants.Notification.CHANNEL_ID,
                title = warningTitle,
                text = warningBody,
                smallIcon = R.drawable.ic_launcher_notification,
                category = NotificationCategory.CATEGORY_REMINDER,
                priority = NotificationPriority.MAX,
                contentIntent = pContentIntent,
            )

            notificationCreator.showNotify(notify, WORKLOAD_NOTIFICATION_ID)
        }
        return Result.success()
    }

    private suspend fun fetchDailyWorkload(date: Instant): DailyWorkload {
        val calendarSettings = calendarSettingsRepository.fetchSettings().first()
        val week = date.dateTime().date.numberOfRepeatWeek(calendarSettings.numberOfWeek)

        val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, week).first()
        val customSchedule = customScheduleRepository.fetchScheduleByDate(date).first()
        val todos = todoRepository.fetchTodosByDate(date).first()
        val homeworks = homeworksRepository.fetchHomeworksByDate(date).first()

        val classes = (customSchedule?.classes ?: baseSchedule?.classes).orEmpty().filter { classModel ->
            calendarSettings.holidays.none { holiday ->
                val dateFilter = TimeRange(holiday.start, holiday.end).containsDate(date)
                val organizationFilter = holiday.organizations.contains(classModel.organization.uid)
                dateFilter && organizationFilter
            }
        }

        return DailyWorkload.calculate(classes, homeworks, todos)
    }

    companion object {
        const val WORK_KEY = "WORKLOAD_WARNING_SERVICE"
        const val WORKLOAD_NOTIFICATION_ID = 2482
    }
}
