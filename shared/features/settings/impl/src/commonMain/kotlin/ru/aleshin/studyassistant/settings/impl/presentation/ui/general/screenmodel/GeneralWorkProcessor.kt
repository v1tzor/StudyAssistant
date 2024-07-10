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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.general.screenmodel

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.settings.impl.domain.interactors.GeneralSettingsInteractor
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.GeneralSettingsUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEffect

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal interface GeneralWorkProcessor :
    FlowWorkProcessor<GeneralWorkCommand, GeneralAction, GeneralEffect> {

    class Base(
        private val settingsInteractor: GeneralSettingsInteractor,
    ) : GeneralWorkProcessor {

        override suspend fun work(command: GeneralWorkCommand) = when (command) {
            is GeneralWorkCommand.LoadSettings -> loadSettingsWork()
            is GeneralWorkCommand.UpdateSettings -> updateSettingsWork(command.settings)
        }

        private fun loadSettingsWork() = flow {
            settingsInteractor.fetchSettings().collectAndHandle(
                onLeftAction = { emit(EffectResult(GeneralEffect.ShowError(it))) },
                onRightAction = { settings ->
                    emit(ActionResult(GeneralAction.UpdateSettings(settings.mapToUi())))
                },
            )
        }

        private fun updateSettingsWork(settings: GeneralSettingsUi) = flow {
            settingsInteractor.updateSettings(settings.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(GeneralEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class GeneralWorkCommand : WorkCommand {
    data object LoadSettings : GeneralWorkCommand()
    data class UpdateSettings(val settings: GeneralSettingsUi) : GeneralWorkCommand()
}