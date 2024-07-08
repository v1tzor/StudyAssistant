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

package ru.aleshin.studyassistant.info.impl.domain.interactors

import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.info.impl.domain.common.InfoEitherWrapper
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
internal interface SubjectsInteractor {

    suspend fun fetchSubjectsByOrganization(organizationId: UID): FlowDomainResult<InfoFailures, List<Subject>>
    suspend fun deleteSubjectById(targetId: UID): UnitDomainResult<InfoFailures>

    class Base(
        private val subjectsRepository: SubjectsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: InfoEitherWrapper,
    ) : SubjectsInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchSubjectsByOrganization(organizationId: UID) = eitherWrapper.wrapFlow {
            subjectsRepository.fetchAllSubjectsByOrganization(organizationId, targetUser)
        }

        override suspend fun deleteSubjectById(targetId: UID) = eitherWrapper.wrap {
            subjectsRepository.deleteSubject(targetId, targetUser)
        }
    }
}