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

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.di.holder.SettingsFeatureDIHolder
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralViewState

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
internal class GeneralScreenModel(
    private val workProcessor: GeneralWorkProcessor,
    stateCommunicator: GeneralStateCommunicator,
    effectCommunicator: GeneralEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<GeneralViewState, GeneralEvent, GeneralAction, GeneralEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(GeneralEvent.Init)
        }
    }

    override suspend fun WorkScope<GeneralViewState, GeneralAction, GeneralEffect>.handleEvent(
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
        currentState: GeneralViewState,
    ) = when (action) {
        is GeneralAction.UpdateSettings -> currentState.copy(
            settings = action.settings,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SETTINGS, SETTINGS_ACTION,
    }
}

@Composable
internal fun Screen.rememberGeneralScreenModel(): GeneralScreenModel {
    val di = SettingsFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<GeneralScreenModel>() }
}