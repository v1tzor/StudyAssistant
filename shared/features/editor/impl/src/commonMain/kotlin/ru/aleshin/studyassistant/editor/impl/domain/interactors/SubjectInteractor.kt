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

import entities.subject.Subject
import functional.DomainResult
import functional.FlowDomainResult
import functional.UID
import repositories.SubjectsRepository
import repositories.UsersRepository
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

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addOrUpdateSubject(subject: Subject) = eitherWrapper.wrap {
            subjectsRepository.addOrUpdateSubject(subject, targetUser)
        }

        override suspend fun fetchAllSubjectsByOrganization(organizationId: UID) = eitherWrapper.wrapFlow {
            subjectsRepository.fetchAllSubjectsByOrganization(organizationId, targetUser)
        }

        override suspend fun fetchSubjectById(uid: UID) = eitherWrapper.wrapFlow {
            subjectsRepository.fetchSubjectById(uid, targetUser)
        }
    }
}