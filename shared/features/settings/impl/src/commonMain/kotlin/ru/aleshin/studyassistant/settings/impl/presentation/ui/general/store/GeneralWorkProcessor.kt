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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.OutputResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi
import ru.aleshin.studyassistant.settings.impl.domain.interactors.GeneralSettingsInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.UserDataInteractor
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.GeneralSettingsUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralOutput

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal interface GeneralWorkProcessor :
    FlowWorkProcessor<GeneralWorkCommand, GeneralAction, GeneralEffect, GeneralOutput> {

    class Base(
        private val settingsInteractor: GeneralSettingsInteractor,
        private val organizationInteractor: OrganizationInteractor,
        private val userDataInteractor: UserDataInteractor,
    ) : GeneralWorkProcessor {

        override suspend fun work(command: GeneralWorkCommand) = when (command) {
            is GeneralWorkCommand.LoadSettings -> loadSettingsWork()
            is GeneralWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is GeneralWorkCommand.UpdateSettings -> updateSettingsWork(command.settings)
            is GeneralWorkCommand.DeleteCurrentSchedule -> deleteCurrentScheduleWork(command.organizationIds)
            is GeneralWorkCommand.DeleteAllData -> deleteAllDataWork()
        }

        private fun loadSettingsWork() = flow {
            settingsInteractor.fetchSettings().collectAndHandle(
                onLeftAction = { emit(EffectResult(GeneralEffect.ShowError(it))) },
                onRightAction = { settings ->
                    emit(ActionResult(GeneralAction.UpdateSettings(settings.mapToUi())))
                },
            )
        }

        private fun loadOrganizationsWork() = flow {
            organizationInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(GeneralEffect.ShowError(it))) },
                onRightAction = { organizations ->
                    emit(ActionResult(GeneralAction.UpdateOrganizations(organizations.map { it.mapToUi() })))
                },
            )
        }

        private fun updateSettingsWork(settings: GeneralSettingsUi) = flow {
            settingsInteractor.updateSettings(settings.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(GeneralEffect.ShowError(it))) },
            )
        }

        private fun deleteCurrentScheduleWork(organizationIds: Set<UID>) = flow {
            userDataInteractor.deleteCurrentSchedule(organizationIds).handle(
                onLeftAction = { emit(EffectResult(GeneralEffect.ShowError(it))) },
            )
        }

        private fun deleteAllDataWork() = flow {
            userDataInteractor.deleteAllUserData().handle(
                onLeftAction = { emit(EffectResult(GeneralEffect.ShowError(it))) },
                onRightAction = { emit(OutputResult(GeneralOutput.NavigateToOnboarding)) },
            )
        }
    }
}

internal sealed class GeneralWorkCommand : WorkCommand {
    data object LoadSettings : GeneralWorkCommand()
    data object LoadOrganizations : GeneralWorkCommand()
    data class UpdateSettings(val settings: GeneralSettingsUi) : GeneralWorkCommand()
    data class DeleteCurrentSchedule(val organizationIds: Set<UID>) : GeneralWorkCommand()
    data object DeleteAllData : GeneralWorkCommand()
}
