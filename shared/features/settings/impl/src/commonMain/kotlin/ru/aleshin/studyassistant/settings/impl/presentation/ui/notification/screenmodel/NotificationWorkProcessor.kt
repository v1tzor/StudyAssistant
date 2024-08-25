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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.settings.impl.domain.interactors.NotificationSettingsInteractor
import ru.aleshin.studyassistant.settings.impl.domain.interactors.OrganizationInteractor
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.NotificationSettingsUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEffect

/**
 * @author Stanislav Aleshin on 25.08.2024.
 */
internal interface NotificationWorkProcessor :
    FlowWorkProcessor<NotificationWorkCommand, NotificationAction, NotificationEffect> {

    class Base(
        private val settingsInteractor: NotificationSettingsInteractor,
        private val organizationInteractor: OrganizationInteractor,
    ) : NotificationWorkProcessor {

        override suspend fun work(command: NotificationWorkCommand) = when (command) {
            is NotificationWorkCommand.LoadSettings -> loadSettingsWork()
            is NotificationWorkCommand.LoadOrganizations -> loadOrganizationsWork()
            is NotificationWorkCommand.UpdateSettings -> updateSettingsWork(command.settings)
        }

        private fun loadSettingsWork() = flow {
            settingsInteractor.fetchSettings().collectAndHandle(
                onLeftAction = { emit(EffectResult(NotificationEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(NotificationAction.UpdateSettings(it.mapToUi()))) },
            )
        }

        private fun loadOrganizationsWork() = flow {
            organizationInteractor.fetchAllShortOrganizations().collectAndHandle(
                onLeftAction = { emit(EffectResult(NotificationEffect.ShowError(it))) },
                onRightAction = { allOrganizations ->
                    val organizations = allOrganizations.map { it.mapToUi() }
                    emit(ActionResult(NotificationAction.UpdateOrganizations(organizations)))
                },
            )
        }

        private fun updateSettingsWork(settings: NotificationSettingsUi) = flow {
            settingsInteractor.updateSettings(settings.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(NotificationEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class NotificationWorkCommand : WorkCommand {
    data object LoadSettings : NotificationWorkCommand()
    data object LoadOrganizations : NotificationWorkCommand()
    data class UpdateSettings(val settings: NotificationSettingsUi) : NotificationWorkCommand()
}