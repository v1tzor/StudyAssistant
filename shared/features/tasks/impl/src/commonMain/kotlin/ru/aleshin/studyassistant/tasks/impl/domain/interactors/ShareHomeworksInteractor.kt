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

package ru.aleshin.studyassistant.tasks.impl.domain.interactors

import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.AnalyticsService
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
import ru.aleshin.studyassistant.core.domain.entities.share.HomeworkShare
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.MediatedHomework
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworkShareRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.HomeworkImportLink
import ru.aleshin.studyassistant.tasks.impl.domain.entities.HomeworkSharePreview
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal interface ShareHomeworksInteractor {

    suspend fun createShare(
        date: Instant,
        homeworks: List<MediatedHomework>,
    ): DomainResult<TasksFailures, ShareLink>

    suspend fun fetchSharePreview(code: String): DomainResult<TasksFailures, HomeworkSharePreview>

    suspend fun importShare(
        code: String,
        share: HomeworkShare,
        links: List<HomeworkImportLink>,
    ): UnitDomainResult<TasksFailures>

    class Base(
        private val shareRepository: HomeworkShareRepository,
        private val profileRepository: ProfileRepository,
        private val subjectsRepository: SubjectsRepository,
        private val organizationsRepository: OrganizationsRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val dateManager: DateManager,
        private val analyticsService: AnalyticsService,
        private val eitherWrapper: TasksEitherWrapper,
    ) : ShareHomeworksInteractor {

        override suspend fun createShare(
            date: Instant,
            homeworks: List<MediatedHomework>,
        ) = eitherWrapper.wrap {
            if (homeworks.isEmpty() || homeworks.size > MAX_HOMEWORKS) {
                throw ShareException.ItemLimit()
            }
            val profile = profileRepository.fetchProfile().filterNotNull().first()
            shareRepository.createShare(
                HomeworkShare(
                    senderName = profile.username,
                    date = date,
                    homeworks = homeworks,
                )
            ).apply {
                analyticsService.trackEvent(
                    name = CREATE_SHARE_EVENT,
                    eventParams = mapOf(Pair(HOMEWORKS_COUNT_KEY, homeworks.count().toString()))
                )
            }
        }

        override suspend fun fetchSharePreview(code: String) = eitherWrapper.wrap {
            if (shareRepository.isShareImported(code)) throw ShareException.Duplicate()
            val share = shareRepository.fetchShare(code)
            val subjects = subjectsRepository.fetchAllSubjectsByNames(
                share.homeworks.map { homework -> homework.subjectName },
            )
            val schedule = fetchSchedule(share.date.startThisDay())
            val classes = schedule.mapToValue(
                onBaseSchedule = { base -> base?.classes.orEmpty() },
                onCustomSchedule = { custom -> custom?.classes.orEmpty() },
            )
            val links = share.homeworks.map { homework ->
                val subject = subjects.find { it.name == homework.subjectName }
                val linkedClass = classes.find { it.subject?.name == homework.subjectName }
                HomeworkImportLink(
                    homework = homework,
                    receivedSubjectName = homework.subjectName,
                    actualSubject = subject,
                    actualClass = linkedClass,
                    classNumber = linkedClass?.let { classes.indexOf(it).inc() },
                )
            }
            HomeworkSharePreview(
                share = share,
                organizations = organizationsRepository.fetchAllShortOrganization().first().sortedByDescending { it.isMain },
                schedule = schedule,
                links = links,
            ).apply {
                analyticsService.trackEvent(CLAIM_SHARE_EVENT, mapOf())
            }
        }

        override suspend fun importShare(
            code: String,
            share: HomeworkShare,
            links: List<HomeworkImportLink>,
        ) = eitherWrapper.wrapUnit {
            val organizations = organizationsRepository.fetchAllShortOrganization().first()
                .associateBy { it.uid }
            val currentTime = dateManager.fetchCurrentInstant()
            val homeworks = links.map { link ->
                val subject = checkNotNull(link.actualSubject)
                Homework(
                    uid = randomUUID(),
                    classId = link.actualClass?.uid,
                    deadline = share.date.startThisDay(),
                    subject = subject,
                    organization = checkNotNull(organizations[subject.organizationId]),
                    theoreticalTasks = link.homework.theoreticalTasks,
                    practicalTasks = link.homework.practicalTasks,
                    presentationTasks = link.homework.presentationTasks,
                    test = link.homework.test,
                    priority = link.homework.priority,
                    isDone = false,
                    completeDate = null,
                    updatedAt = currentTime.toEpochMilliseconds(),
                )
            }
            shareRepository.importShare(code, homeworks).apply {
                analyticsService.trackEvent(IMPORT_SHARE_EVENT, mapOf())
            }
        }

        private suspend fun fetchSchedule(date: Instant): Schedule {
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings().first().numberOfWeek
            val numberOfWeek = date.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
            val customSchedule = customScheduleRepository.fetchScheduleByDate(date).first()
            if (customSchedule != null) {
                return Schedule.Custom(
                    data = customSchedule.copy(
                        classes = customSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
                    )
                )
            }
            val baseSchedule = baseScheduleRepository.fetchScheduleByDate(date, numberOfWeek).first()
            return Schedule.Base(
                data = baseSchedule?.copy(
                    classes = baseSchedule.classes.sortedBy { it.timeRange.from.dateTime().time }
                )
            )
        }

        private companion object {
            const val MAX_HOMEWORKS = 20
            const val CREATE_SHARE_EVENT = "homework_share_create"
            const val CLAIM_SHARE_EVENT = "homework_share_claim"
            const val IMPORT_SHARE_EVENT = "homework_share_import"
            const val HOMEWORKS_COUNT_KEY = "homework_share_count_value"
        }
    }
}
