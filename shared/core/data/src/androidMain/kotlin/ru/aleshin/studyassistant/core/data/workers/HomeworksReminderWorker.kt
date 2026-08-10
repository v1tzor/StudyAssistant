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
import org.jetbrains.compose.resources.getString
import org.kodein.di.DI
import org.kodein.di.DirectDIAware
import org.kodein.di.bindProvider
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.di.coreCommonModule
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.notifications.NotificationCreator
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationCategory
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationPriority
import ru.aleshin.studyassistant.core.common.notifications.parameters.NotificationStyles
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.data.R
import ru.aleshin.studyassistant.core.data.di.coreDataModule
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.homeworks_recommendation_body_prefix as core_homeworks_recommendation_body_prefix
import ru.aleshin.studyassistant.core.ui.resources.homeworks_recommendation_body_suffix as core_homeworks_recommendation_body_suffix
import ru.aleshin.studyassistant.core.ui.resources.homeworks_recommendation_title as core_homeworks_recommendation_title
import ru.aleshin.studyassistant.core.ui.resources.homeworks_reminder_body_prefix as core_homeworks_reminder_body_prefix
import ru.aleshin.studyassistant.core.ui.resources.homeworks_reminder_title as core_homeworks_reminder_title

/**
 * @author Stanislav Aleshin on 22.08.2024.
 */
class HomeworksReminderWorker(
    private val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), DirectDIAware {

    override val directDI = DI.direct {
        bindProvider<Context> { applicationContext }
        bindProvider<CrashlyticsService> { CrashlyticsService.Empty() }
        importAll(coreCommonModule, coreDataModule)
    }
    private val dateManager = instance<DateManager>()
    private val notificationCreator = instance<NotificationCreator>()
    private val homeworksRepository = instance<HomeworksRepository>()

    override suspend fun doWork(): Result {
        val today = dateManager.fetchBeginningCurrentInstant()
        val tomorrow = today.shiftDay(1)
        val afterTomorrow = today.shiftDay(2)
        val targetTimeRange = TimeRange(from = today, to = afterTomorrow.endThisDay())

        val homeworks = homeworksRepository.fetchHomeworksByTimeRange(targetTimeRange).first()
            .filterNot { it.isDone }
        val groupedHomeworks = homeworks.groupBy { it.deadline.startThisDay() }

        val nearestHomeworks =
            (groupedHomeworks[today] ?: emptyList()) + (groupedHomeworks[tomorrow] ?: emptyList())
        val afterTomorrowHomeworks = groupedHomeworks[afterTomorrow] ?: emptyList()

        notificationCreator.cancelNotify(HOMEWORKS_NOTIFICATION_ID)
        showReminderNotification(nearestHomeworks, afterTomorrowHomeworks)

        return Result.success()
    }

    private suspend fun showReminderNotification(
        nearestHomeworks: List<Homework>,
        afterTomorrowHomeworks: List<Homework>,
    ) {
        val reminderTitle = getString(CoreRes.string.core_homeworks_reminder_title)
        val reminderBodyPrefix = getString(CoreRes.string.core_homeworks_reminder_body_prefix)
        val recommendationTitle = getString(CoreRes.string.core_homeworks_recommendation_title)
        val recommendationBodyPrefix = getString(CoreRes.string.core_homeworks_recommendation_body_prefix)
        val recommendationBodySuffix = getString(CoreRes.string.core_homeworks_recommendation_body_suffix)
        val mainActivityUri = Constants.App.OPEN_APP_DEEPLINK.toUri()
        val contentIntent = Intent(ACTION_VIEW, mainActivityUri)
        val pContentIntent =
            PendingIntent.getActivity(context, HOMEWORKS_NOTIFICATION_ID, contentIntent, FLAG_IMMUTABLE)
        if (nearestHomeworks.isNotEmpty()) {
            val subjects = nearestHomeworks.mapNotNull { it.subject?.name }.distinct()
            val notify = notificationCreator.createNotify(
                channelId = Constants.Notification.CHANNEL_ID,
                title = reminderTitle,
                text = "",
                style = NotificationStyles.BigTextStyle(
                    text = buildString {
                        appendLine(reminderBodyPrefix)
                        subjects.forEachIndexed { index, name ->
                            appendLine(index.inc().toString() + ") " + name)
                        }
                    }
                ),
                smallIcon = R.drawable.ic_launcher_notification,
                category = NotificationCategory.CATEGORY_REMINDER,
                priority = NotificationPriority.MAX,
                contentIntent = pContentIntent,
            )

            notificationCreator.showNotify(notify, HOMEWORKS_NOTIFICATION_ID)
        } else if (afterTomorrowHomeworks.isNotEmpty()) {
            val notify = notificationCreator.createNotify(
                channelId = Constants.Notification.CHANNEL_ID,
                title = recommendationTitle,
                text = "",
                style = NotificationStyles.BigTextStyle(
                    text = buildString {
                        append(recommendationBodyPrefix)
                        append(afterTomorrowHomeworks.size)
                        append(recommendationBodySuffix)
                    },
                ),
                smallIcon = R.drawable.ic_launcher_notification,
                category = NotificationCategory.CATEGORY_REMINDER,
                priority = NotificationPriority.MAX,
                contentIntent = pContentIntent,
            )

            notificationCreator.showNotify(notify, HOMEWORKS_NOTIFICATION_ID)
        }
    }

    companion object {
        const val WORK_KEY = "HOMEWORKS_REMINDER_SERVICE"
        const val HOMEWORKS_NOTIFICATION_ID = 2481
    }
}
