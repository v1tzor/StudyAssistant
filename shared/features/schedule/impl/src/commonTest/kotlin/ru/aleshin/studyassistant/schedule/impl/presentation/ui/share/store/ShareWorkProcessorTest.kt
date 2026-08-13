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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardChallenge
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.MediatedBaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.share.ScheduleShareClaim
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleLinkResult
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleOrganizationLink
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleSharePreview
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ShareSchedulesInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ScheduleShareClaimUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ShareStatus
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareOutput
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class ShareWorkProcessorTest {

    @Test
    fun invalidClaimChangesStatusWithoutErrorEffect() = runBlocking {
        val interactor = FakeShareSchedulesInteractor().apply {
            claimFailure = ScheduleFailures.OtherError(ShareException.InvalidCode())
        }
        val processor = ShareWorkProcessor.Base(interactor)

        val results = processor.work(ShareWorkCommand.ClaimShare("AAAA-AAAA-AAAA")).toList()

        assertEquals(2, results.size)
        val loading = assertIs<WorkResult.Action<ShareAction>>(results[0])
        assertEquals(ShareAction.UpdateStatus(ShareStatus.LOADING), loading.action)
        val invalid = assertIs<WorkResult.Action<ShareAction>>(results[1])
        assertEquals(ShareAction.UpdateStatus(ShareStatus.INVALID), invalid.action)
    }

    @Test
    fun backFromClaimReleasesItBeforeNavigation() = runBlocking {
        val interactor = FakeShareSchedulesInteractor()
        val processor = ShareWorkProcessor.Base(interactor)
        val claim = ScheduleShareClaimUi(
            claimId = "claim-token",
            senderName = "Sender",
            schedules = emptyList(),
            organizations = emptyList(),
        )

        val results = processor.work(
            ShareWorkCommand.ReleaseShare(claim = claim, navigateBack = true)
        ).toList()

        assertEquals("claim-token", interactor.releasedClaim?.claimId)
        val reset = assertIs<WorkResult.Action<ShareAction>>(results[0])
        assertEquals(ShareAction.Reset, reset.action)
        val navigation = assertIs<WorkResult.Output<ShareOutput>>(results[1])
        assertEquals(ShareOutput.NavigateToBack, navigation.output)
    }

    private class FakeShareSchedulesInteractor : ShareSchedulesInteractor {

        var releasedClaim: ScheduleShareClaim? = null
        var claimFailure: ScheduleFailures? = null

        override suspend fun createShare(): DomainResult<ScheduleFailures, ShareLink> = error("Unused")

        override suspend fun claimShare(
            code: String,
        ): DomainResult<ScheduleFailures, ScheduleSharePreview> {
            return claimFailure?.let { failure -> Either.Left(failure) } ?: error("Unused")
        }

        override suspend fun releaseShare(
            claim: ScheduleShareClaim,
        ): UnitDomainResult<ScheduleFailures> {
            releasedClaim = claim
            return Either.Right(Unit)
        }

        override suspend fun createImportReward(
            claim: ScheduleShareClaim,
        ): DomainResult<ScheduleFailures, AdRewardChallenge> = error("Unused")

        override suspend fun linkOrganization(
            links: List<ScheduleOrganizationLink>,
            schedules: List<MediatedBaseSchedule>,
            sharedOrganizationId: UID,
            targetOrganizationId: UID?,
        ): DomainResult<ScheduleFailures, ScheduleLinkResult> = error("Unused")

        override suspend fun updateLinkedSubjects(
            links: List<ScheduleOrganizationLink>,
            schedules: List<MediatedBaseSchedule>,
            sharedOrganizationId: UID,
            subjects: Map<UID, Subject>,
        ): DomainResult<ScheduleFailures, ScheduleLinkResult> = error("Unused")

        override suspend fun updateLinkedEmployees(
            links: List<ScheduleOrganizationLink>,
            schedules: List<MediatedBaseSchedule>,
            sharedOrganizationId: UID,
            employees: Map<UID, Employee>,
        ): DomainResult<ScheduleFailures, ScheduleLinkResult> = error("Unused")

        override suspend fun importShare(
            rewardChallengeId: String,
            claim: ScheduleShareClaim,
            links: List<ScheduleOrganizationLink>,
        ): UnitDomainResult<ScheduleFailures> = error("Unused")
    }
}
