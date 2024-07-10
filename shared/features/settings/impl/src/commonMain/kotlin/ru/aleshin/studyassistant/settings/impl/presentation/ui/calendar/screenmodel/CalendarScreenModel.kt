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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.screenmodel

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
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarViewState

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
internal class CalendarScreenModel(
    private val workProcessor: CalendarWorkProcessor,
    stateCommunicator: CalendarStateCommunicator,
    effectCommunicator: CalendarEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<CalendarViewState, CalendarEvent, CalendarAction, CalendarEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(CalendarEvent.Init)
        }
    }

    override suspend fun WorkScope<CalendarViewState, CalendarAction, CalendarEffect>.handleEvent(
        event: CalendarEvent,
    ) {
        when (event) {
            is CalendarEvent.Init -> {
                launchBackgroundWork(BackgroundKey.LOAD_SETTINGS) {
                    val command = CalendarWorkCommand.LoadSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is CalendarEvent.ChangeNumberOfRepeatWeek -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(numberOfWeek = event.numberOfWeek)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = CalendarWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
        }
    }

    override suspend fun reduce(
        action: CalendarAction,
        currentState: CalendarViewState,
    ) = when (action) {
        is CalendarAction.UpdateSettings -> currentState.copy(
            settings = action.settings,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SETTINGS, SETTINGS_ACTION,
    }
}

@Composable
internal fun Screen.rememberCalendarScreenModel(): CalendarScreenModel {
    val di = SettingsFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<CalendarScreenModel>() }
}