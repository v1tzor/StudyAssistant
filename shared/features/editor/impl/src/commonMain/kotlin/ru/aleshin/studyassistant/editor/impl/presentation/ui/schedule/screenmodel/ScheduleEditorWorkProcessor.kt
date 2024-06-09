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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import entities.common.NumberOfRepeatWeek
import extensions.weekTimeRange
import functional.UID
import functional.collectAndHandle
import functional.handle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import managers.DateManager
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorEffect

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal interface ScheduleEditorWorkProcessor :
    FlowWorkProcessor<ScheduleEditorWorkCommand, ScheduleEditorAction, ScheduleEditorEffect> {

    class Base(
        private val scheduleInteractor: BaseScheduleInteractor,
        private val baseClassInteractor: BaseClassInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val settingsInteractor: CalendarSettingsInteractor,
        private val dateManager: DateManager,
    ) : ScheduleEditorWorkProcessor {
        override suspend fun work(command: ScheduleEditorWorkCommand) = when (command) {
            is ScheduleEditorWorkCommand.LoadWeekSchedule -> loadWeekScheduleWork(command.numberOfWeek)
            is ScheduleEditorWorkCommand.LoadOrganizationsData -> loadOrganizationsDataWork()
            is ScheduleEditorWorkCommand.UpdateOrganization -> updateOrganizationWork(command.organization)
            is ScheduleEditorWorkCommand.DeleteClass -> deleteClassWork(command.uid, command.schedule)
        }

        private fun loadWeekScheduleWork(week: NumberOfRepeatWeek) = flow {
            val currentDateTime = dateManager.fetchCurrentDate().toLocalDateTime(TimeZone.UTC)
            val weekTimeRange = currentDateTime.weekTimeRange(TimeZone.UTC)

            emit(ActionResult(ScheduleEditorAction.UpdateLoading(true)))

            scheduleInteractor.fetchScheduleByTimeRange(
                timeRange = weekTimeRange,
                numberOfWeek = week,
            ).collectAndHandle(
                onLeftAction = { emit(EffectResult(ScheduleEditorEffect.ShowError(it))) },
                onRightAction = { baseWeekSchedule ->
                    val weekSchedule = baseWeekSchedule.mapToUi()
                    emit(ActionResult(ScheduleEditorAction.UpdateScheduleData(week, weekSchedule)))
                },
            )
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadOrganizationsDataWork() = flow {
            val organizationsFlow = organizationInteractor.fetchAllShortOrganizations()
            val settingsFlow = settingsInteractor.fetchSettings()

            organizationsFlow.flatMapLatestWithResult(
                secondFlow = settingsFlow,
                onError = { ScheduleEditorEffect.ShowError(it) },
                onData = { shortOrganizations, calendarSettings ->
                    val organizations = shortOrganizations.map { it.mapToUi() }
                    val settings = calendarSettings.mapToUi()
                    ScheduleEditorAction.UpdateOrganizationData(organizations, settings)
                }
            ).collect { result ->
                emit(result)
            }
        }

        private fun updateOrganizationWork(organization: OrganizationShortUi) = flow {
            organizationInteractor.updateShortOrganization(organization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ScheduleEditorEffect.ShowError(it))) },
            )
        }

        private fun deleteClassWork(uid: UID, schedule: BaseScheduleUi) = flow {
            baseClassInteractor.deleteClassBySchedule(uid, schedule.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(ScheduleEditorEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class ScheduleEditorWorkCommand : WorkCommand {
    data object LoadOrganizationsData : ScheduleEditorWorkCommand()
    data class LoadWeekSchedule(val numberOfWeek: NumberOfRepeatWeek) : ScheduleEditorWorkCommand()
    data class UpdateOrganization(val organization: OrganizationShortUi) : ScheduleEditorWorkCommand()
    data class DeleteClass(val uid: UID, val schedule: BaseScheduleUi) : ScheduleEditorWorkCommand()
}
