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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.HomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEffect

/**
 * @author Stanislav Aleshin on 27.06.2024.
 */
internal interface HomeworksWorkProcessor :
    FlowWorkProcessor<HomeworksWorkCommand, HomeworksAction, HomeworksEffect> {

    class Base(
        private val homeworksInteractor: HomeworksInteractor,
        private val scheduleInteractor: ScheduleInteractor,
        private val dateManager: DateManager,
    ) : HomeworksWorkProcessor {

        override suspend fun work(command: HomeworksWorkCommand) = when (command) {
            is HomeworksWorkCommand.LoadHomeworks -> loadHomeworksWork(command.timeRange)
            is HomeworksWorkCommand.LoadActiveSchedule -> loadActiveScheduleWork(command.currentDate)
            is HomeworksWorkCommand.UpdateHomework -> updateHomeworkWork(command.homework)
        }

        private fun loadHomeworksWork(timeRange: TimeRange) = flow<HomeworksWorkResult> {
            homeworksInteractor.fetchHomeworksByTimeRange(timeRange).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
                onRightAction = { homeworks ->
                    val homeworksMap = homeworks.mapValues { it.value.mapToUi() }
                    emit(ActionResult(HomeworksAction.UpdateHomeworks(homeworksMap)))
                },
            )
        }.onStart {
            emit(ActionResult(HomeworksAction.UpdateLoading(true)))
        }

        private fun loadActiveScheduleWork(currentDate: Instant) = flow<HomeworksWorkResult> {
            scheduleInteractor.fetchScheduleByDate(currentDate).collectAndHandle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
                onRightAction = { schedule ->
                    emit(ActionResult(HomeworksAction.UpdateActiveSchedule(schedule.mapToUi())))
                },
            )
        }

        private fun updateHomeworkWork(homework: HomeworkDetailsUi) = flow {
            homeworksInteractor.updateHomework(homework.convertToBase().mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(HomeworksEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class HomeworksWorkCommand : WorkCommand {
    data class LoadHomeworks(val timeRange: TimeRange) : HomeworksWorkCommand()
    data class LoadActiveSchedule(val currentDate: Instant) : HomeworksWorkCommand()
    data class UpdateHomework(val homework: HomeworkDetailsUi) : HomeworksWorkCommand()
}

internal typealias HomeworksWorkResult = WorkResult<HomeworksAction, HomeworksEffect>