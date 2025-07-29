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

import kotlinx.coroutines.flow.catch
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 24.07.2024.
 */
internal interface UsersInteractor {

    suspend fun fetchAllFriends(): FlowDomainResult<TasksFailures, List<AppUser>>

    suspend fun fetchAppUserPaidStatus(): FlowDomainResult<TasksFailures, Boolean>

    class Base(
        private val usersRepository: UsersRepository,
        private val eitherWrapper: TasksEitherWrapper,
    ) : UsersInteractor {

        override suspend fun fetchAllFriends() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserFriends().catch { exception ->
                if (exception is InternetConnectionException) {
                    emit(emptyList())
                } else {
                    throw exception
                }
            }
        }

        override suspend fun fetchAppUserPaidStatus() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserPaidStatus()
        }
    }
}