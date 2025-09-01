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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.general.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyOutput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseSimpleComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralState

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
internal class GeneralComposeStore(
    private val workProcessor: GeneralWorkProcessor,
    stateCommunicator: StateCommunicator<GeneralState>,
    effectCommunicator: EffectCommunicator<GeneralEffect>,
    coroutineManager: CoroutineManager,
) : BaseSimpleComposeStore<GeneralState, GeneralEvent, GeneralAction, GeneralEffect>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(GeneralEvent.Init)
    }

    override suspend fun WorkScope<GeneralState, GeneralAction, GeneralEffect, EmptyOutput>.handleEvent(
        event: GeneralEvent,
    ) {
        when (event) {
            is GeneralEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_SETTINGS) {
                    val command = GeneralWorkCommand.LoadSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is GeneralEvent.ChangeLanguage -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(languageType = event.language)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = GeneralWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is GeneralEvent.ChangeTheme -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(themeType = event.theme)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = GeneralWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
        }
    }

    override suspend fun reduce(
        action: GeneralAction,
        currentState: GeneralState,
    ) = when (action) {
        is GeneralAction.UpdateSettings -> currentState.copy(
            settings = action.settings,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SETTINGS, SETTINGS_ACTION,
    }

    class Factory(
        private val workProcessor: GeneralWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseSimpleComposeStore.Factory<GeneralComposeStore, GeneralState> {

        override fun create(savedState: GeneralState): GeneralComposeStore {
            return GeneralComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}