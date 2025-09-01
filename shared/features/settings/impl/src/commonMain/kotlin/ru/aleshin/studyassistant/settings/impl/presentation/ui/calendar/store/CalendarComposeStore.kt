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

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyOutput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseSimpleComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarState

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
internal class CalendarComposeStore(
    private val workProcessor: CalendarWorkProcessor,
    stateCommunicator: StateCommunicator<CalendarState>,
    effectCommunicator: EffectCommunicator<CalendarEffect>,
    coroutineManager: CoroutineManager,
) : BaseSimpleComposeStore<CalendarState, CalendarEvent, CalendarAction, CalendarEffect>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(CalendarEvent.Started)
    }

    override suspend fun WorkScope<CalendarState, CalendarAction, CalendarEffect, EmptyOutput>.handleEvent(
        event: CalendarEvent,
    ) {
        when (event) {
            is CalendarEvent.Started -> {
                launchBackgroundWork(BackgroundKey.LOAD_SETTINGS) {
                    val command = CalendarWorkCommand.LoadSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ORGANIZATION) {
                    val command = CalendarWorkCommand.LoadOrganizations
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
            is CalendarEvent.UpdateHolidays -> with(state()) {
                val settings = checkNotNull(settings)
                val updatedSettings = settings.copy(holidays = event.holidays)

                launchBackgroundWork(BackgroundKey.SETTINGS_ACTION) {
                    val command = CalendarWorkCommand.UpdateSettings(updatedSettings)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
        }
    }

    override suspend fun reduce(
        action: CalendarAction,
        currentState: CalendarState,
    ) = when (action) {
        is CalendarAction.UpdateSettings -> currentState.copy(
            settings = action.settings,
        )
        is CalendarAction.UpdateOrganizations -> currentState.copy(
            allOrganizations = action.organizations,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SETTINGS, LOAD_ORGANIZATION, SETTINGS_ACTION,
    }

    class Factory(
        private val workProcessor: CalendarWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseSimpleComposeStore.Factory<CalendarComposeStore, CalendarState> {

        override fun create(savedState: CalendarState): CalendarComposeStore {
            return CalendarComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}