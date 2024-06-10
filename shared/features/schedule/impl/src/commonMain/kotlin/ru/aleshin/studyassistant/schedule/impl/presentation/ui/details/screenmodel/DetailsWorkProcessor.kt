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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.screenmodel

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import functional.TimeRange
import functional.collectAndHandle
import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEffect

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal interface DetailsWorkProcessor : FlowWorkProcessor<DetailsWorkCommand, DetailsAction, DetailsEffect> {

    class Base(
        private val scheduleInteractor: ScheduleInteractor,
    ) : DetailsWorkProcessor {

        override suspend fun work(command: DetailsWorkCommand) = when (command) {
            is DetailsWorkCommand.LoadWeekSchedule -> loadWeekScheduleWork(command.week)
        }
        private fun loadWeekScheduleWork(week: TimeRange) = flow {
            emit(ActionResult(DetailsAction.UpdateLoading(true)))
            scheduleInteractor.fetchDetailsWeekSchedule(week).collectAndHandle(
                onLeftAction = { emit(EffectResult(DetailsEffect.ShowError(it))) },
                onRightAction = { weekScheduleDetails ->
                    val weekSchedule = weekScheduleDetails.mapToUi()
                    emit(ActionResult(DetailsAction.UpdateWeekSchedule(weekSchedule)))
                }
            )
        }
    }
}

internal sealed class DetailsWorkCommand : WorkCommand {
    data class LoadWeekSchedule(val week: TimeRange) : DetailsWorkCommand()
}