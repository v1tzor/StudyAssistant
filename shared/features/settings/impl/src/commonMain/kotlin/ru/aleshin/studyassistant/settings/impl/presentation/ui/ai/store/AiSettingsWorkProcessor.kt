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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store

import kotlinx.coroutines.flow.flow
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyOutput
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.settings.impl.domain.interactors.AiSettingsInteractor
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsEffect

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface AiSettingsWorkProcessor :
    FlowWorkProcessor<AiSettingsWorkCommand, AiSettingsAction, AiSettingsEffect, EmptyOutput> {

    class Base(
        private val interactor: AiSettingsInteractor,
    ) : AiSettingsWorkProcessor {

        override suspend fun work(command: AiSettingsWorkCommand) = when (command) {
            AiSettingsWorkCommand.LoadSettings -> loadSettingsWork()
        }

        private fun loadSettingsWork() = flow {
            interactor.fetchSettings().collectAndHandle(
                onLeftAction = { emit(EffectResult(AiSettingsEffect.ShowError(it))) },
                onRightAction = { emit(ActionResult(AiSettingsAction.UpdateSettings(it.mapToUi()))) },
            )
        }
    }
}

internal sealed class AiSettingsWorkCommand : WorkCommand {
    data object LoadSettings : AiSettingsWorkCommand()
}
