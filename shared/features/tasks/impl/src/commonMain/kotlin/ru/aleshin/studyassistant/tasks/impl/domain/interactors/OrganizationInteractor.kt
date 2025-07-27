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

package ru.aleshin.studyassistant.tasks.impl.domain.interactors

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
internal interface OrganizationInteractor {

    suspend fun fetchAllShortOrganizations(): FlowDomainResult<TasksFailures, List<OrganizationShort>>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val eitherWrapper: TasksEitherWrapper,
    ) : OrganizationInteractor {

        override suspend fun fetchAllShortOrganizations() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllShortOrganization().map { organizations ->
                organizations.sortedByDescending { it.isMain }
            }
        }
    }
}