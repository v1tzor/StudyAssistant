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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.store

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.HomeworkInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsOutput

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface DetailsWorkProcessor :
    FlowWorkProcessor<DetailsWorkCommand, DetailsAction, DetailsEffect, DetailsOutput> {

    class Base(
        private val scheduleInteractor: ScheduleInteractor,
        private val homeworkInteractor: HomeworkInteractor,
        private val dateManager: DateManager,
    ) : DetailsWorkProcessor {

        override suspend fun work(command: DetailsWorkCommand) = when (command) {
            is DetailsWorkCommand.LoadWeekSchedule -> loadWeekScheduleWork(command.week)
            is DetailsWorkCommand.UpdateIsHomeworkDone -> updateIsHomeworkDoneWork(command.homework, command.isDone)
        }

        private fun loadWeekScheduleWork(week: TimeRange) = flow<DetailsWorkResult> {
            scheduleInteractor.fetchDetailsWeekSchedule(week).collectAndHandle(
                onLeftAction = { emit(EffectResult(DetailsEffect.ShowError(it))) },
                onRightAction = { overview ->
                    emit(
                        ActionResult(
                            DetailsAction.UpdateWeekSchedule(
                                schedule = overview.schedule.mapToUi(),
                                activeClass = overview.activeClass?.mapToUi(),
                            )
                        )
                    )
                },
            )
        }.onStart {
            emit(ActionResult(DetailsAction.UpdateLoading(true)))
        }

        private fun updateIsHomeworkDoneWork(homework: HomeworkDetailsUi, isDone: Boolean) = flow {
            val currentDate = dateManager.fetchCurrentInstant()
            val updatedHomework = homework.copy(
                isDone = isDone,
                completeDate = currentDate.takeIf { isDone },
            )
            homeworkInteractor.updateHomework(updatedHomework.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(DetailsEffect.ShowError(it))) },
            )
        }

    }
}

internal sealed class DetailsWorkCommand : WorkCommand {
    data class LoadWeekSchedule(val week: TimeRange) : DetailsWorkCommand()
    data class UpdateIsHomeworkDone(val homework: HomeworkDetailsUi, val isDone: Boolean) : DetailsWorkCommand()
}

internal typealias DetailsWorkResult = WorkResult<DetailsAction, DetailsEffect, DetailsOutput>
