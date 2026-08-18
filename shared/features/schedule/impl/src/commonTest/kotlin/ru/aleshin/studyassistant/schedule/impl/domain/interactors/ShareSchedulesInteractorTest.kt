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

package ru.aleshin.studyassistant.schedule.impl.domain.interactors

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardChallenge
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardPurpose
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.settings.NotificationSettings
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShare
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShareClaim
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.users.Profile
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleShareRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleErrorHandler
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
class ShareSchedulesInteractorTest {

    @Test
    fun fetchShareableOrganizationsReturnsCurrentWeekOrganizations() = runBlocking {
        val interactor = createInteractor()

        val result = interactor.fetchShareableOrganizations()

        val organizations = assertIs<Either.Right<List<OrganizationShort>>>(result).data
        assertEquals(listOf("org-main", "org-extra"), organizations.map { it.uid })
        assertTrue(organizations.first().isMain)
    }

    @Test
    fun createShareKeepsOnlySelectedOrganizationClasses() = runBlocking {
        val shareRepository = RecordingShareRepository()
        val interactor = createInteractor(shareRepository)

        val result = interactor.createShare(listOf("org-main"))

        assertIs<Either.Right<ShareLink>>(result)
        val share = checkNotNull(shareRepository.createdShare)
        assertEquals(listOf("org-main"), share.organizations.map { it.uid })
        assertEquals(
            listOf("org-main"),
            share.schedules.flatMap { schedule -> schedule.classes.map { it.organizationId } }.distinct(),
        )
        assertEquals(1, share.schedules.single().classes.size)
    }

    @Test
    fun createShareRejectsEmptyOrganizationSelection() = runBlocking {
        val interactor = createInteractor()

        val result = interactor.createShare(emptyList())

        val failure = assertIs<Either.Left<ScheduleFailures>>(result).data
        val otherError = assertIs<ScheduleFailures.OtherError>(failure)
        assertTrue(otherError.throwable is ShareException.ItemLimit)
    }

    private fun createInteractor(
        shareRepository: RecordingShareRepository = RecordingShareRepository(),
    ): ShareSchedulesInteractor {
        val mainOrganization = organization(uid = "org-main", isMain = true, shortName = "School")
        val extraOrganization = organization(uid = "org-extra", isMain = false, shortName = "Courses")
        val schedules = listOf(
            BaseSchedule(
                uid = "schedule-1",
                dateVersion = DateVersion(
                    from = Instant.fromEpochMilliseconds(1_000L),
                    to = Instant.fromEpochMilliseconds(2_000L),
                ),
                dayOfWeek = DayOfWeek.MONDAY,
                week = NumberOfRepeatWeek.ONE,
                classes = listOf(
                    classModel("class-main", "schedule-1", mainOrganization),
                    classModel("class-extra", "schedule-1", extraOrganization),
                ),
            )
        )
        return ShareSchedulesInteractor.Base(
            shareRepository = shareRepository,
            profileRepository = object : ProfileRepository {
                override suspend fun fetchProfile() = flowOf(
                    Profile(uid = "user-1", username = "Sender", updatedAt = 0L)
                )
                override suspend fun updateProfile(profile: Profile) = error("Unused")
                override suspend fun uploadAvatar(oldAvatar: String?, file: InputFile) = error("Unused")
                override suspend fun deleteAvatar(avatar: String) = error("Unused")
            },
            organizationRepository = object : OrganizationsRepository {
                override suspend fun addOrUpdateOrganization(organization: Organization) = error("Unused")
                override suspend fun addOrUpdateOrganizationsGroup(organizations: List<Organization>) = error("Unused")
                override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile) = error("Unused")
                override suspend fun fetchOrganizationById(uid: UID) = error("Unused")
                override suspend fun fetchOrganizationsById(uid: List<UID>) = flowOf(
                    listOf(mainOrganization, extraOrganization).filter { organization ->
                        organization.uid in uid
                    }
                )
                override suspend fun fetchShortOrganizationById(uid: UID) = error("Unused")
                override suspend fun fetchAllOrganization() = error("Unused")
                override suspend fun fetchAllShortOrganization() = error("Unused")
                override suspend fun deleteAvatar(avatarUrl: String) = error("Unused")
            },
            baseSchedulesRepository = object : BaseScheduleRepository {
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
            },
            adRewardRepository = UnusedAdRewardRepository,
            notificationSettingsRepository = UnusedNotificationSettingsRepository,
            startClassesReminderManager = UnusedStartClassesReminderManager,
            endClassesReminderManager = UnusedEndClassesReminderManager,
            dateManager = FakeDateManager,
            eitherWrapper = ScheduleEitherWrapper.Base(
                errorHandler = ScheduleErrorHandler.Base(),
                crashlyticsService = UnusedCrashlyticsService,
            ),
        )
    }

    private fun organization(
        uid: UID,
        isMain: Boolean,
        shortName: String,
    ) = Organization(
        uid = uid,
        isMain = isMain,
        shortName = shortName,
        updatedAt = 0L,
    )

    private fun classModel(
        uid: UID,
        scheduleId: UID,
        organization: Organization,
    ) = Class(
        uid = uid,
        scheduleId = scheduleId,
        organization = OrganizationShort(
            uid = organization.uid,
            isMain = organization.isMain,
            shortName = organization.shortName,
            updatedAt = organization.updatedAt,
        ),
        eventType = EventType.LESSON,
        subject = null,
        teacher = null,
        office = "101",
        location = null,
        timeRange = TimeRange(
            from = Instant.fromEpochMilliseconds(1_000L),
            to = Instant.fromEpochMilliseconds(2_000L),
        ),
    )

    private class RecordingShareRepository : ScheduleShareRepository {
        var createdShare: ScheduleShare? = null

        override suspend fun createShare(share: ScheduleShare): ShareLink {
            createdShare = share
            return ShareLink(
                code = "AAAA-AAAA-AAAA",
                deepLink = "studyassistant://share/schedule?code=AAAA-AAAA-AAAA",
                createdAt = Instant.fromEpochMilliseconds(1L),
                expiresAt = Instant.fromEpochMilliseconds(2L),
            )
        }

        override suspend fun claimShare(code: String) = error("Unused")
        override suspend fun confirmShare(claim: ScheduleShareClaim) = error("Unused")
        override suspend fun releaseShare(claim: ScheduleShareClaim) = error("Unused")
        override suspend fun importShare(
            organizations: List<Organization>,
            schedules: List<BaseSchedule>,
        ) = error("Unused")
    }

    private object FakeDateManager : DateManager {
        override fun fetchCurrentInstant() = Instant.fromEpochMilliseconds(1_500L)
        override fun fetchCurrentWeek() = TimeRange(
            from = Instant.fromEpochMilliseconds(1_000L),
            to = Instant.fromEpochMilliseconds(2_000L),
        )
        override fun fetchBeginningCurrentInstant(): Instant = error("Unused")
        override fun fetchEndCurrentInstant(): Instant = error("Unused")
        override fun isCurrentDay(date: Instant): Boolean = error("Unused")
        override fun calculateLeftDateTime(endDateTime: Instant): Long = error("Unused")
        override fun calculateLeftTime(endTime: LocalTime): Long = error("Unused")
        override fun calculateProgress(startTime: Instant, endTime: Instant): Float = error("Unused")
        override fun secondTicker(): Flow<Unit> = emptyFlow()
        override fun minuteTicker(): Flow<Instant> = emptyFlow()
    }

    private object UnusedAdRewardRepository : AdRewardRepository {
        override suspend fun createChallenge(
            purpose: AdRewardPurpose,
            subject: String?,
        ): AdRewardChallenge = error("Unused")
        override suspend fun completeChallenge(challengeId: String) = error("Unused")
    }

    private object UnusedNotificationSettingsRepository : NotificationSettingsRepository {
        override suspend fun fetchSettings(): Flow<NotificationSettings> = error("Unused")
        override suspend fun updateSettings(settings: NotificationSettings) = error("Unused")
    }

    private object UnusedStartClassesReminderManager : StartClassesReminderManager {
        override suspend fun startOrRetryReminderService() = error("Unused")
        override suspend fun stopReminderService(allOrganizations: List<UID>) = error("Unused")
    }

    private object UnusedEndClassesReminderManager : EndClassesReminderManager {
        override suspend fun startOrRetryReminderService() = error("Unused")
        override suspend fun stopReminderService(allOrganizations: List<UID>) = error("Unused")
    }

    private object UnusedCrashlyticsService : CrashlyticsService {
        override fun sendLog(message: String) = Unit
        override fun recordException(tag: String, message: String, exception: Throwable) = Unit
        override fun initializeService() = Unit
        override fun setupUser(id: UID?) = Unit
    }
}
