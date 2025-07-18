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

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface SubjectInteractor {

    suspend fun addOrUpdateSubject(subject: Subject): DomainResult<EditorFailures, UID>
    suspend fun fetchAllSubjectsByOrganization(organizationId: UID): FlowDomainResult<EditorFailures, List<Subject>>
    suspend fun fetchSubjectById(uid: UID): FlowDomainResult<EditorFailures, Subject?>

    class Base(
        private val subjectsRepository: SubjectsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : SubjectInteractor {

        override suspend fun addOrUpdateSubject(subject: Subject) = eitherWrapper.wrap {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            subjectsRepository.addOrUpdateSubject(subject, targetUser)
        }

        override suspend fun fetchAllSubjectsByOrganization(organizationId: UID) = eitherWrapper.wrapFlow {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            subjectsRepository.fetchAllSubjectsByOrganization(organizationId, targetUser).map { subjects ->
                subjects.sortedBy { subject -> subject.name }
            }
        }

        override suspend fun fetchSubjectById(uid: UID) = eitherWrapper.wrapFlow {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            subjectsRepository.fetchSubjectById(uid, targetUser)
        }
    }
}