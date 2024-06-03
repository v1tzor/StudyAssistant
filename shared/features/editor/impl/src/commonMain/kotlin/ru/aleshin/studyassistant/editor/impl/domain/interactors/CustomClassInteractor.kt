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

import entities.classes.Class
import functional.DomainResult
import functional.FlowDomainResult
import functional.UID
import functional.UnitDomainResult
import repositories.CustomScheduleRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface CustomClassInteractor {

    suspend fun addClass(classModel: Class): DomainResult<EditorFailures, UID>
    suspend fun fetchClass(classId: UID, scheduleId: UID): FlowDomainResult<EditorFailures, Class?>
    suspend fun updateClass(classModel: Class): DomainResult<EditorFailures, UID>
    suspend fun deleteClass(targetClass: Class): UnitDomainResult<EditorFailures>

    class Base(
        private val customScheduleRepository: CustomScheduleRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : CustomClassInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addClass(classModel: Class) = eitherWrapper.wrap {
            TODO("Not yet implemented")
        }

        override suspend fun fetchClass(classId: UID, scheduleId: UID) = eitherWrapper.wrapFlow {
            customScheduleRepository.fetchClassById(classId, scheduleId, targetUser)
        }

        override suspend fun updateClass(classModel: Class) = eitherWrapper.wrap {
            TODO("Not yet implemented")
        }

        override suspend fun deleteClass(targetClass: Class) = eitherWrapper.wrapUnit {
            TODO("Not yet implemented")
        }
    }
}
