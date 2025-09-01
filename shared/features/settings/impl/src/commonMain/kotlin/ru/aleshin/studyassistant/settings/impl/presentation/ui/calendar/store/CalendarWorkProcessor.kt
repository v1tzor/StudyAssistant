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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyOutput
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.settings.impl.domain.interactors.CalendarSettingsInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarEffect

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal interface CalendarWorkProcessor :
    FlowWorkProcessor<CalendarWorkCommand, CalendarAction, CalendarEffect, EmptyOutput> {

    class Base(
        private val settingsInteractor: CalendarSettingsInteractor,
        private val organizationsInteractor: OrganizationInteractor,
    ) : CalendarWorkProcessor {

        override suspend fun work(command: CalendarWorkCommand) = when (command) {
            is CalendarWorkCommand.LoadSettings -> loadSettingsWork()
            is CalendarWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is CalendarWorkCommand.UpdateSettings -> updateSettingsWork(command.settings)
        }

        private fun loadSettingsWork() = flow {
            settingsInteractor.fetchSettings().collectAndHandle(
                onLeftAction = { emit(EffectResult(CalendarEffect.ShowError(it))) },
                onRightAction = { settings ->
                    emit(ActionResult(CalendarAction.UpdateSettings(settings.mapToUi())))
                },
            )
        }

        private fun loadOrganizationsWork() = flow {
            organizationsInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(CalendarEffect.ShowError(it))) },
                onRightAction = { allOrganizations ->
                    val organizations = allOrganizations.map { it.mapToUi() }
                    emit(ActionResult(CalendarAction.UpdateOrganizations(organizations)))
                },
            )
        }

        private fun updateSettingsWork(settings: CalendarSettingsUi) = flow {
            settingsInteractor.updateSettings(settings.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(CalendarEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class CalendarWorkCommand : WorkCommand {
    data object LoadSettings : CalendarWorkCommand()
    data object LoadOrganizations : CalendarWorkCommand()
    data class UpdateSettings(val settings: CalendarSettingsUi) : CalendarWorkCommand()
}