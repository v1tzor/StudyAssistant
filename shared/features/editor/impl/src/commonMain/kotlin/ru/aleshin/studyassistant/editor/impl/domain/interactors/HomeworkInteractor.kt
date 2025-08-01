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

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 22.06.2024.
 */
internal interface HomeworkInteractor {

    suspend fun addOrUpdateHomework(homework: Homework): DomainResult<EditorFailures, UID>
    suspend fun fetchHomeworkById(homeworkId: UID): FlowDomainResult<EditorFailures, Homework?>
    suspend fun deleteHomework(targetId: UID): UnitDomainResult<EditorFailures>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val goalsRepository: DailyGoalsRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : HomeworkInteractor {

        override suspend fun addOrUpdateHomework(homework: Homework) = eitherWrapper.wrap {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val updatedHomework = homework.copy(updatedAt = updatedAt)
            homeworksRepository.addOrUpdateHomework(updatedHomework).apply {
                val linkedGoal = goalsRepository.fetchGoalByContentId(updatedHomework.uid).first()
                if (linkedGoal != null) {
                    val updatedGoal = linkedGoal.copy(contentHomework = updatedHomework, updatedAt = updatedAt)
                    goalsRepository.addOrUpdateGoal(updatedGoal)
                }
            }
        }

        override suspend fun fetchHomeworkById(homeworkId: UID) = eitherWrapper.wrapFlow {
            homeworksRepository.fetchHomeworkById(homeworkId)
        }

        override suspend fun deleteHomework(targetId: UID) = eitherWrapper.wrap {
            homeworksRepository.deleteHomework(targetId)
        }
    }
}