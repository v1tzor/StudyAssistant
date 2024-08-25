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

package ru.aleshin.studyassistant.core.data.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.MainDirectDIAware
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.fetchCurrentLanguage
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.notifications.NotificationCreator
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationCategory
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority
import ru.aleshin.studyassistant.core.data.R
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.CLASS_MINUTE_DURATION_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.MAX_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.MOVEMENT_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.PRACTICE_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.PRESENTATION_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.TEST_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.THEORY_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.TODO_PRIORITY_RATE
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyAnalysis.Companion.TODO_RATE
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.fetchAllTasks
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkComponents
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchCoreStrings

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
class WorkloadWarningWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), MainDirectDIAware {

    private val dateManager = instance<DateManager>()
    private val notificationCreator = instance<NotificationCreator>()
    private val usersRepository = instance<UsersRepository>()
    private val calendarSettingsRepository = instance<CalendarSettingsRepository>()
    private val notificationSettingsRepository = instance<NotificationSettingsRepository>()
    private val homeworksRepository = instance<HomeworksRepository>()
    private val todoRepository = instance<TodoRepository>()
    private val baseScheduleRepository = instance<BaseScheduleRepository>()
    private val customScheduleRepository = instance<CustomScheduleRepository>()

    override suspend fun doWork(): Result {
        val coreStrings = fetchCoreStrings(fetchAppLanguage(applicationContext.fetchCurrentLanguage()))
        val currentUser = usersRepository.fetchCurrentAppUser()?.uid ?: return Result.failure()
        val currentDate = dateManager.fetchBeginningCurrentInstant()

        val notificationSettings = notificationSettingsRepository.fetchSettings(currentUser).first()
        val maxWorkloadValue = notificationSettings.highWorkload ?: return Result.failure()

        val value = fetchDailyWorkload(currentUser, currentDate)
        if (value.toInt() >= maxWorkloadValue) {
            val notify = notificationCreator.createNotify(
                channelId = Constants.Notification.CHANNEL_ID,
                title = coreStrings.highWorkloadWarningTitle,
                text = coreStrings.highWorkloadWarningBody,
                smallIcon = R.drawable.ic_launcher_notification,
                category = NotificationCategory.CATEGORY_REMINDER,
                priority = NotificationPriority.MAX,
            )

            notificationCreator.showNotify(notify)
        }
        return Result.success()
    }

    private suspend fun fetchDailyWorkload(currentUser: UID, date: Instant): Float {
        val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(currentUser).first().numberOfWeek
        val week = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)

        val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, week, currentUser).first()
        val customSchedule = customScheduleRepository.fetchScheduleByDate(date, currentUser).first()
        val todos = todoRepository.fetchTodosByDate(date, currentUser).first()
        val homeworks = homeworksRepository.fetchHomeworksByDate(date, currentUser).first()

        val classes = customSchedule?.classes ?: baseSchedule?.classes
        val movementMap = classes?.groupBy { it.organization }?.mapValues { classEntry ->
            classEntry.value.map { it.location }.toSet()
        }

        val testsRate = homeworks.count { it.test != null } * TEST_RATE

        val classesDuration = classes?.map { it.timeRange.periodDuration() }?.sumOf { it.minutes } ?: 0
        val classesRate = classesDuration * CLASS_MINUTE_DURATION_RATE

        val movementsRate = (movementMap?.toList()?.sumOf { it.second.size } ?: 0) * MOVEMENT_RATE

        val theoriesTasksRate = homeworks.sumOf { homework ->
            homework.theoreticalTasks.toHomeworkComponents().fetchAllTasks().size
        }.let { numberOfTheories ->
            numberOfTheories * THEORY_RATE
        }
        val practicesTasksRate = homeworks.sumOf { homework ->
            homework.practicalTasks.toHomeworkComponents().fetchAllTasks().size
        }.let { numberOfPractices ->
            numberOfPractices * PRACTICE_RATE
        }
        val presentationsTasksRate = homeworks.sumOf { homework ->
            homework.presentationTasks.toHomeworkComponents().fetchAllTasks().size
        }.let { numberOfPresentations ->
            numberOfPresentations * PRESENTATION_RATE
        }
        val homeworksRate = theoriesTasksRate + practicesTasksRate + presentationsTasksRate

        val todosRate = todos.sumOf { todo ->
            if (todo.priority == TaskPriority.STANDARD) TODO_RATE else TODO_PRIORITY_RATE
        }.toFloat()

        val rateList = listOf(
            classesRate,
            testsRate,
            movementsRate,
            homeworksRate,
            todosRate,
        )
        val generalAssessment = rateList.sum() / MAX_RATE

        return generalAssessment
    }

    companion object {
        const val WORK_KEY = "WORKLOAD_WARNING_SERVICE"
    }
}