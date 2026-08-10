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

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyOutput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseSimpleComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsState

/**
 * @author Stanislav Aleshin on 05.07.2026.
 */
internal class AiSettingsComposeStore(
    private val workProcessor: AiSettingsWorkProcessor,
    stateCommunicator: StateCommunicator<AiSettingsState>,
    effectCommunicator: EffectCommunicator<AiSettingsEffect>,
    coroutineManager: CoroutineManager,
) : BaseSimpleComposeStore<AiSettingsState, AiSettingsEvent, AiSettingsAction, AiSettingsEffect>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(AiSettingsEvent.Started)
    }

    override suspend fun WorkScope<AiSettingsState, AiSettingsAction, AiSettingsEffect, EmptyOutput>.handleEvent(
        event: AiSettingsEvent,
    ) {
        when (event) {
            AiSettingsEvent.Started -> launchBackgroundWork(BackgroundKey.LOAD) {
                workProcessor.work(AiSettingsWorkCommand.LoadSettings).collectAndHandleWork()
            }
            AiSettingsEvent.SelectSharedService -> launchBackgroundWork(BackgroundKey.ACTION) {
                workProcessor.work(AiSettingsWorkCommand.SelectSharedService).collectAndHandleWork()
            }
            AiSettingsEvent.SelectPersonalService -> launchBackgroundWork(BackgroundKey.ACTION) {
                workProcessor.work(AiSettingsWorkCommand.SelectPersonalService).collectAndHandleWork()
            }
            is AiSettingsEvent.TestPersonalKey -> launchBackgroundWork(BackgroundKey.ACTION) {
                workProcessor.work(AiSettingsWorkCommand.TestPersonalKey(event.apiKey)).collectAndHandleWork()
            }
            is AiSettingsEvent.SavePersonalKey -> launchBackgroundWork(BackgroundKey.ACTION) {
                workProcessor.work(AiSettingsWorkCommand.SavePersonalKey(event.apiKey)).collectAndHandleWork()
            }
            AiSettingsEvent.DeletePersonalKey -> launchBackgroundWork(BackgroundKey.ACTION) {
                workProcessor.work(AiSettingsWorkCommand.DeletePersonalKey).collectAndHandleWork()
            }
        }
    }

    override suspend fun reduce(action: AiSettingsAction, currentState: AiSettingsState) = when (action) {
        is AiSettingsAction.UpdateSettings -> currentState.copy(settings = action.settings)
        is AiSettingsAction.UpdateSaving -> currentState.copy(isSaving = action.isSaving)
    }

    private enum class BackgroundKey : BackgroundWorkKey {
        LOAD, ACTION
    }

    class Factory(
        private val workProcessor: AiSettingsWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseSimpleComposeStore.Factory<AiSettingsComposeStore, AiSettingsState> {
        override fun create(savedState: AiSettingsState) = AiSettingsComposeStore(
            workProcessor = workProcessor,
            stateCommunicator = StateCommunicator.Default(savedState),
            effectCommunicator = EffectCommunicator.Default(),
            coroutineManager = coroutineManager,
        )
    }
}
