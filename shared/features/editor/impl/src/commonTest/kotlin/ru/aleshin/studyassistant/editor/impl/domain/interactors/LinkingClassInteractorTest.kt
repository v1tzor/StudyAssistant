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

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.DayOfNumberedWeek
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorErrorHandler
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
internal class LinkingClassInteractorTest {

    @Test
    fun importedFridaySubjectIsOfferedAsHomeworkDeadline() = runBlocking {
        val timeZone = TimeZone.currentSystemDefault()
        val thursday = LocalDate(2026, 8, 20).atStartOfDayIn(timeZone)
        val friday = LocalDate(2026, 8, 21).atStartOfDayIn(timeZone)
        val chemistry = subject(uid = "subj-chem", name = "Chemistry")
        val schedule = BaseSchedule.createActual(
            currentDate = thursday,
            dayOfNumberedWeek = DayOfNumberedWeek(
                dayOfWeek = DayOfWeek.FRIDAY,
                week = NumberOfRepeatWeek.ONE,
            ),
            classes = listOf(
                classModel(
                    uid = "class-chem",
                    scheduleId = "schedule-friday",
                    subject = chemistry,
                    date = friday,
                    start = LocalTime(11, 40),
                    end = LocalTime(12, 20),
                ),
            ),
            createdAt = thursday.toEpochMilliseconds(),
        )

        val interactor = LinkingClassInteractor.Base(
            baseScheduleRepository = FakeBaseScheduleRepository(listOf(schedule.copy(uid = "schedule-friday"))),
            customScheduleRepository = FakeCustomScheduleRepository(),
            calendarRepository = FakeCalendarSettingsRepository(),
            eitherWrapper = EditorEitherWrapper.Base(
                errorHandler = EditorErrorHandler.Base(),
                crashlyticsService = CrashlyticsService.Empty(),
            ),
        )

        val result = interactor.fetchFreeClassesForHomework(chemistry.uid, thursday).first()
        val classes = (result as Either.Right).data
        val fridayClasses = classes.entries.first { entry -> entry.key.equalsDay(friday) }.value

        assertTrue(fridayClasses.any { classModel -> classModel.subject?.uid == chemistry.uid })
        assertTrue(fridayClasses.any { classModel -> classModel.uid == "class-chem" })
    }

    private fun classModel(
        uid: UID,
        scheduleId: UID,
        subject: Subject,
        date: Instant,
        start: LocalTime,
        end: LocalTime,
    ) = Class(
        uid = uid,
        scheduleId = scheduleId,
        organization = OrganizationShort(
            uid = "org-1",
            shortName = "School",
            type = OrganizationType.SCHOOL,
            updatedAt = 1L,
        ),
        eventType = EventType.LESSON,
        subject = subject,
        teacher = null,
        office = "205",
        location = null,
        timeRange = TimeRange(
            from = date.setHoursAndMinutes(start),
            to = date.setHoursAndMinutes(end),
        ),
        number = 5,
    )

    private fun subject(uid: UID, name: String) = Subject(
        uid = uid,
        organizationId = "org-1",
        eventType = EventType.LESSON,
        name = name,
        teacher = null,
        office = "",
        color = 1,
        location = null,
        updatedAt = 1L,
    )

    private class FakeBaseScheduleRepository(
        private val schedules: List<BaseSchedule>,
    ) : BaseScheduleRepository {
        override suspend fun addOrUpdateSchedule(schedule: BaseSchedule) = error("Unused")
        override suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedule>) = error("Unused")
        override suspend fun fetchScheduleById(uid: UID) = error("Unused")
        override suspend fun fetchScheduleByDate(
            date: Instant,
            numberOfWeek: NumberOfRepeatWeek,
        ) = error("Unused")
        override suspend fun fetchSchedulesByVersion(
            version: TimeRange,
            numberOfWeek: NumberOfRepeatWeek?,
        ) = flowOf(schedules)
        override suspend fun fetchClassById(uid: UID, scheduleId: UID) = error("Unused")
        override suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange) = error("Unused")
    }

    private class FakeCustomScheduleRepository : CustomScheduleRepository {
        override suspend fun addOrUpdateSchedule(schedule: CustomSchedule) = error("Unused")
        override suspend fun fetchScheduleById(uid: UID) = error("Unused")
        override suspend fun fetchScheduleByDate(date: Instant) = flowOf(null)
        override suspend fun fetchSchedulesByTimeRange(timeRange: TimeRange) = flowOf(emptyList<CustomSchedule>())
        override suspend fun fetchClassById(uid: UID, scheduleId: UID) = error("Unused")
        override suspend fun deleteScheduleById(scheduleId: UID) = error("Unused")
        override suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange) = error("Unused")
    }

    private class FakeCalendarSettingsRepository : CalendarSettingsRepository {
        override suspend fun fetchSettings(): Flow<CalendarSettings> = flowOf(
            CalendarSettings(holidays = emptyList(), updatedAt = 1L),
        )
        override suspend fun updateSettings(settings: CalendarSettings) = error("Unused")
    }
}
