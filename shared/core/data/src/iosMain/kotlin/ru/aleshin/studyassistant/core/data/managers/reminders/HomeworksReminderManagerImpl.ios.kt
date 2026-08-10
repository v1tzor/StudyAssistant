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

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.getString
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
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
actual class HomeworksReminderManagerImpl(
    private val notificationScheduler: NotificationScheduler,
    private val homeworksRepository: HomeworksRepository,
    private val dateManager: DateManager,
) : HomeworksReminderManager {

    actual override suspend fun startOrRetryReminderService(time: Instant) {
        val reminderTitle = getString(CoreRes.string.core_homeworks_reminder_title)
        val reminderBodyPrefix = getString(CoreRes.string.core_homeworks_reminder_body_prefix)
        val recommendationTitle = getString(CoreRes.string.core_homeworks_recommendation_title)
        val recommendationBodyPrefix = getString(CoreRes.string.core_homeworks_recommendation_body_prefix)
        val recommendationBodySuffix = getString(CoreRes.string.core_homeworks_recommendation_body_suffix)
        val currentTime = dateManager.fetchCurrentInstant()
        val currentDate = dateManager.fetchBeginningCurrentInstant()

        val notificationIds = buildList {
            repeat(SCHEDULE_DAYS) { dayShift ->
                val targetDate = currentDate.shiftDay(dayShift)
                val tomorrow = targetDate.shiftDay(1)
                val afterTomorrow = targetDate.shiftDay(2)
                val targetTime = targetDate.setHoursAndMinutes(time)
                if (targetTime <= currentTime) return@repeat

                val homeworks = homeworksRepository.fetchHomeworksByTimeRange(
                    TimeRange(targetDate, afterTomorrow.endThisDay())
                ).first().filterNot { it.isDone }
                val nearestHomeworks = homeworks.filter { homework ->
                    val deadline = homework.deadline.startThisDay()
                    deadline == targetDate || deadline == tomorrow
                }
                val afterTomorrowHomeworks = homeworks.filter { homework ->
                    homework.deadline.startThisDay() == afterTomorrow
                }

                val content = when {
                    nearestHomeworks.isNotEmpty() -> {
                        val subjects = nearestHomeworks.mapNotNull { it.subject?.name }.distinct()
                        reminderTitle to buildString {
                            append(reminderBodyPrefix)
                            subjects.forEachIndexed { index, subject ->
                                append("\n${index + 1}) $subject")
                            }
                        }
                    }

                    afterTomorrowHomeworks.isNotEmpty() -> {
                        recommendationTitle to buildString {
                            append(recommendationBodyPrefix)
                            append(afterTomorrowHomeworks.size)
                            append(recommendationBodySuffix)
                        }
                    }

                    else -> null
                }
                content?.let { (title, body) ->
                    val id = "$NOTIFICATION_PREFIX:$targetDate".hashCode()
                    add(id)
                    notificationScheduler.scheduleNotification(id, title, body, targetTime)
                }
            }
        }
        notificationScheduler.updateNotificationGroup(NOTIFICATION_GROUP, notificationIds)
    }

    actual override suspend fun stopReminderService() {
        notificationScheduler.clearNotificationGroup(NOTIFICATION_GROUP)
    }

    companion object {
        private const val SCHEDULE_DAYS = 5
        private const val NOTIFICATION_GROUP = "IOS_HOMEWORKS_REMINDERS"
        private const val NOTIFICATION_PREFIX = "homeworks"
    }
}
