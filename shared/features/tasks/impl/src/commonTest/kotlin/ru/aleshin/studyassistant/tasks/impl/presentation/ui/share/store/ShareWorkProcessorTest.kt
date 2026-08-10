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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.share.HomeworkShare
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.domain.entities.share.ShareLink
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.entities.tasks.MediatedHomework
import ru.aleshin.studyassistant.tasks.impl.domain.entities.HomeworkImportLink
import ru.aleshin.studyassistant.tasks.impl.domain.entities.HomeworkSharePreview
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ShareHomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.SubjectsInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.HomeworkShareStatus
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareAction
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class ShareWorkProcessorTest {

    @Test
    fun invalidShareChangesStatusWithoutErrorEffect() = runBlocking {
        val processor = ShareWorkProcessor.Base(
            shareInteractor = InvalidShareInteractor(),
            subjectsInteractor = UnusedSubjectsInteractor(),
        )

        val results = processor.work(ShareWorkCommand.FetchShare("AAAA-AAAA-AAAA")).toList()

        assertEquals(2, results.size)
        val loading = assertIs<WorkResult.Action<ShareAction>>(results[0])
        assertEquals(ShareAction.UpdateStatus(HomeworkShareStatus.LOADING), loading.action)
        val invalid = assertIs<WorkResult.Action<ShareAction>>(results[1])
        assertEquals(ShareAction.UpdateStatus(HomeworkShareStatus.INVALID), invalid.action)
    }

    private class InvalidShareInteractor : ShareHomeworksInteractor {

        override suspend fun createShare(
            date: Instant,
            homeworks: List<MediatedHomework>,
        ): DomainResult<TasksFailures, ShareLink> = error("Unused")

        override suspend fun fetchSharePreview(
            code: String,
        ): DomainResult<TasksFailures, HomeworkSharePreview> {
            return Either.Left(TasksFailures.OtherError(ShareException.InvalidCode()))
        }

        override suspend fun importShare(
            code: String,
            share: HomeworkShare,
            links: List<HomeworkImportLink>,
        ): UnitDomainResult<TasksFailures> = error("Unused")
    }

    private class UnusedSubjectsInteractor : SubjectsInteractor {

        override suspend fun fetchSubjectsByOrganization(
            organizationId: UID,
        ): FlowDomainResult<TasksFailures, List<Subject>> = error("Unused")

        override suspend fun fetchSubjectsByNames(
            names: List<String>,
        ): DomainResult<TasksFailures, List<Subject>> = error("Unused")
    }
}
