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
import org.kodein.di.DI
import org.kodein.di.DirectDIAware
import org.kodein.di.bindProvider
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.coreCommonModule
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.fetchCurrentLanguage
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.notifications.NotificationCreator
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationCategory
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationStyles
import ru.aleshin.studyassistant.core.data.R
import ru.aleshin.studyassistant.core.data.di.coreDataModule
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchCoreStrings

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
class HomeworksReminderWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), DirectDIAware {

    override val directDI = DI.direct {
        bindProvider<Context> { applicationContext }
        importAll(coreCommonModule, coreDataModule)
    }
    private val coreStrings: StudyAssistantStrings
        get() = fetchCoreStrings(fetchAppLanguage(applicationContext.fetchCurrentLanguage()))

    private val dateManager = instance<DateManager>()
    private val notificationCreator = instance<NotificationCreator>()
    private val usersRepository = instance<UsersRepository>()
    private val homeworksRepository = instance<HomeworksRepository>()

    override suspend fun doWork(): Result {
        val currentUser = usersRepository.fetchCurrentAppUser() ?: return Result.failure()
        val today = dateManager.fetchBeginningCurrentInstant()
        val tomorrow = today.shiftDay(1)
        val afterTomorrow = today.shiftDay(2)
        val targetTimeRange = TimeRange(from = today, to = afterTomorrow.endThisDay())

        val homeworks = homeworksRepository.fetchHomeworksByTimeRange(targetTimeRange, currentUser.uid).first()
        val groupedHomeworks = homeworks.groupBy { it.deadline.startThisDay() }.mapValues { entry ->
            entry.value.map { homework -> Pair(homework, homework.completeDate == null) }
        }

        val nearestHomeworks = (groupedHomeworks[today] ?: emptyList()) + (groupedHomeworks[tomorrow] ?: emptyList())
        val afterTomorrowHomeworks = groupedHomeworks[afterTomorrow] ?: emptyList()

        showReminderNotification(nearestHomeworks, afterTomorrowHomeworks)

        return Result.success()
    }

    private fun showReminderNotification(
        nearestHomeworks: List<Pair<Homework, Boolean>>,
        afterTomorrowHomeworks: List<Pair<Homework, Boolean>>,
    ) {
        if (nearestHomeworks.count { it.second } != 0) {
            val subjects = nearestHomeworks.mapNotNull { it.first.subject?.name }
            val notify = notificationCreator.createNotify(
                channelId = Constants.Notification.CHANNEL_ID,
                title = coreStrings.homeworksReminderTitle,
                text = "",
                style = NotificationStyles.BigTextStyle(
                    text = buildString {
                        appendLine(coreStrings.homeworksReminderBodyPrefix)
                        subjects.forEachIndexed { index, name ->
                            appendLine(index.inc().toString() + ") " + name)
                        }
                    }
                ),
                smallIcon = R.drawable.ic_launcher_notification,
                category = NotificationCategory.CATEGORY_REMINDER,
                priority = NotificationPriority.MAX,
            )

            notificationCreator.showNotify(notify)
        } else if (afterTomorrowHomeworks.count { it.second } != 0) {
            val subjects = afterTomorrowHomeworks.mapNotNull { it.first.subject?.name }
            val notify = notificationCreator.createNotify(
                channelId = Constants.Notification.CHANNEL_ID,
                title = coreStrings.homeworksRecommendationTitle,
                text = "",
                style = NotificationStyles.BigTextStyle(
                    text = buildString {
                        append(coreStrings.homeworksRecommendationBodyPrefix)
                        append(subjects.size)
                        append(coreStrings.homeworksRecommendationBodySuffix)
                    },
                ),
                smallIcon = R.drawable.ic_launcher_notification,
                category = NotificationCategory.CATEGORY_REMINDER,
                priority = NotificationPriority.MAX,
            )

            notificationCreator.showNotify(notify)
        }
    }

    companion object {
        const val WORK_KEY = "HOMEWORKS_REMINDER_SERVICE"
    }
}