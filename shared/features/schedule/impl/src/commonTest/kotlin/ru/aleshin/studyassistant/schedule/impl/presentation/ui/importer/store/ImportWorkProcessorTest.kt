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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.store

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardChallenge
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportSession
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleImportInteractor
import ru.aleshin.studyassistant.schedule.impl.platform.CompressedScheduleImage
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportSessionUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
class ImportWorkProcessorTest {

    @Test
    fun applyDraftMarksAppliedAfterSuccess() = runBlocking {
        val interactor = FakeScheduleImportInteractor()
        val processor = ImportWorkProcessor.Base(interactor, FakeOrganizationsInteractor())
        val session = ScheduleImportSessionUi(
            title = "Week",
            organizationId = "org-1",
            classes = emptyList(),
            subjects = emptyList(),
            employees = emptyList(),
            originalSubjectIds = emptySet(),
            originalEmployeeIds = emptySet(),
            unparsedLines = emptyList(),
        )

        val results = processor.work(
            ImportWorkCommand.ApplySession(
                session = session,
                rewardChallengeId = "challenge-1",
            )
        ).toList()

        assertEquals("challenge-1", interactor.completedChallengeId)
        assertTrue(results.any { result ->
            result is WorkResult.Action && result.action == ImportAction.UpdateApplied(true)
        })
        val last = assertIs<WorkResult.Action<ImportAction>>(results.last())
        assertEquals(ImportAction.UpdateRewardChallenge(null, false), last.action)
    }

    private class FakeScheduleImportInteractor : ScheduleImportInteractor {

        var completedChallengeId: String? = null

        override suspend fun prepareImage(
            imageBytes: ByteArray,
        ): DomainResult<ScheduleFailures, CompressedScheduleImage> = error("Unused")

        override suspend fun extractDraft(
            requestId: UID,
            image: CompressedScheduleImage,
            note: String?,
            organizationId: UID,
        ): DomainResult<ScheduleFailures, ScheduleImportSession> = error("Unused")

        override suspend fun createImportReward(
            requestId: UID,
            session: ScheduleImportSession,
        ): DomainResult<ScheduleFailures, AdRewardChallenge> = error("Unused")

        override suspend fun applySession(
            session: ScheduleImportSession,
            rewardChallengeId: String,
        ): UnitDomainResult<ScheduleFailures> {
            completedChallengeId = rewardChallengeId
            return Either.Right(Unit)
        }
    }

    private class FakeOrganizationsInteractor : OrganizationsInteractor {

        override suspend fun addOrUpdateOrganizationsData(
            organizations: List<Organization>,
        ): UnitDomainResult<ScheduleFailures> = error("Unused")

        override suspend fun fetchAllShortOrganizations(): FlowDomainResult<ScheduleFailures, List<OrganizationShort>> {
            error("Unused")
        }

        override suspend fun fetchOrganizationById(
            uid: UID,
        ): FlowDomainResult<ScheduleFailures, Organization> {
            error("Unused")
        }
    }
}
