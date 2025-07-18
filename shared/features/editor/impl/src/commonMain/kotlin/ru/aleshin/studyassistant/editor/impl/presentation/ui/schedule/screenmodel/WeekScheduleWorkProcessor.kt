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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseClassInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.BaseScheduleInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.editor.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleEffect

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal interface WeekScheduleWorkProcessor :
    FlowWorkProcessor<WeekScheduleWorkCommand, WeekScheduleAction, WeekScheduleEffect> {

    class Base(
        private val scheduleInteractor: BaseScheduleInteractor,
        private val baseClassInteractor: BaseClassInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val settingsInteractor: CalendarSettingsInteractor,
        private val dateManager: DateManager,
    ) : WeekScheduleWorkProcessor {

        override suspend fun work(command: WeekScheduleWorkCommand) = when (command) {
            is WeekScheduleWorkCommand.LoadWeekSchedule -> loadWeekScheduleWork(command.numberOfWeek)
            is WeekScheduleWorkCommand.LoadOrganizationsData -> loadOrganizationsDataWork()
            is WeekScheduleWorkCommand.UpdateOrganization -> updateOrganizationWork(command.organization)
            is WeekScheduleWorkCommand.DeleteClass -> deleteClassWork(command.uid, command.schedule)
        }

        private fun loadWeekScheduleWork(week: NumberOfRepeatWeek) = flow<WeekScheduleWorkResult> {
            val currentDateTime = dateManager.fetchCurrentInstant().dateTime()
            val weekTimeRange = currentDateTime.weekTimeRange()

            scheduleInteractor.fetchWeekScheduleByVersion(weekTimeRange, week).collectAndHandle(
                onLeftAction = { emit(EffectResult(WeekScheduleEffect.ShowError(it))) },
                onRightAction = { baseWeekSchedule ->
                    val weekSchedule = baseWeekSchedule.mapToUi()
                    emit(ActionResult(WeekScheduleAction.UpdateScheduleData(week, weekSchedule)))
                },
            )
        }.onStart {
            emit(ActionResult(WeekScheduleAction.UpdateLoading(true)))
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadOrganizationsDataWork() = flow {
            val organizationsFlow = organizationInteractor.fetchAllShortOrganizations()
            val settingsFlow = settingsInteractor.fetchSettings()

            organizationsFlow.combineWithResult(
                secondFlow = settingsFlow,
                onError = { WeekScheduleEffect.ShowError(it) },
                onData = { shortOrganizations, calendarSettings ->
                    val organizations = shortOrganizations.map { it.mapToUi() }
                    val settings = calendarSettings.mapToUi()
                    WeekScheduleAction.UpdateOrganizationData(organizations, settings)
                }
            ).collect { workResult ->
                emit(workResult)
            }
        }

        private fun updateOrganizationWork(organization: OrganizationShortUi) = flow {
            organizationInteractor.updateShortOrganization(organization.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(WeekScheduleEffect.ShowError(it))) },
            )
        }

        private fun deleteClassWork(uid: UID, schedule: BaseScheduleUi) = flow {
            baseClassInteractor.deleteClassBySchedule(uid, schedule.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(WeekScheduleEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class WeekScheduleWorkCommand : WorkCommand {
    data object LoadOrganizationsData : WeekScheduleWorkCommand()
    data class LoadWeekSchedule(val numberOfWeek: NumberOfRepeatWeek) : WeekScheduleWorkCommand()
    data class UpdateOrganization(val organization: OrganizationShortUi) : WeekScheduleWorkCommand()
    data class DeleteClass(val uid: UID, val schedule: BaseScheduleUi) : WeekScheduleWorkCommand()
}

internal typealias WeekScheduleWorkResult = WorkResult<WeekScheduleAction, WeekScheduleEffect>