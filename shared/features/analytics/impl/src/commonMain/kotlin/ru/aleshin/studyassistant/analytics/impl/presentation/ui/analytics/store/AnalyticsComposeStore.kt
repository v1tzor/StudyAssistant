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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.store

import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsAction
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsEffect
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsEvent
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsInput
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsOutput
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsState
import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal class AnalyticsComposeStore(
    private val workProcessor: AnalyticsWorkProcessor,
    stateCommunicator: StateCommunicator<AnalyticsState>,
    effectCommunicator: EffectCommunicator<AnalyticsEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<AnalyticsState, AnalyticsEvent, AnalyticsAction, AnalyticsEffect, AnalyticsInput, AnalyticsOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: AnalyticsInput, isRestore: Boolean) {
        dispatchEvent(AnalyticsEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<AnalyticsState, AnalyticsAction, AnalyticsEffect, AnalyticsOutput>.handleEvent(
        event: AnalyticsEvent,
    ) {
        when (event) {
            is AnalyticsEvent.Started -> {
                sendAction(AnalyticsAction.UpdateTarget(event.inputData.target))
                launchBackgroundWork(BackgroundKey.REPORT) {
                    val restoredSelection = state().data?.selection
                    val inputSelection = event.inputData.selection
                    val command = when {
                        event.isRestore && restoredSelection != null -> AnalyticsWorkCommand.ObserveSelection(
                            selection = restoredSelection,
                            target = event.inputData.target,
                        )
                        inputSelection != null -> AnalyticsWorkCommand.ObserveSelection(
                            selection = inputSelection,
                            target = event.inputData.target,
                        )
                        else -> AnalyticsWorkCommand.ObserveDefault(event.inputData.target)
                    }
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AnalyticsEvent.Retry -> with(state()) {
                sendAction(AnalyticsAction.UpdateLoading(true, false))
                launchBackgroundWork(BackgroundKey.REPORT) {
                    val command = data?.selection?.let {
                        AnalyticsWorkCommand.ObserveSelection(it, target)
                    } ?: AnalyticsWorkCommand.ObserveDefault(target)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AnalyticsEvent.ChangePeriod -> with(state()) {
                val selection = data?.selection ?: return
                sendAction(AnalyticsAction.UpdateLoading(true, false))
                launchBackgroundWork(BackgroundKey.REPORT) {
                    val command = AnalyticsWorkCommand.ChangePeriod(event.period, selection, target)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AnalyticsEvent.SelectPeriodAnchor -> with(state()) {
                val period = data?.selection?.period ?: return
                sendAction(AnalyticsAction.UpdateLoading(true, false))
                launchBackgroundWork(BackgroundKey.REPORT) {
                    val command = AnalyticsWorkCommand.SelectPeriodAnchor(period, event.anchor, target)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AnalyticsEvent.SelectCustomRange -> with(state()) {
                sendAction(AnalyticsAction.UpdateLoading(true, false))
                launchBackgroundWork(BackgroundKey.REPORT) {
                    val command = AnalyticsWorkCommand.SelectCustomRange(event.from, event.to, target)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AnalyticsEvent.ClickPreviousPeriod -> with(state()) {
                val selection = data?.selection ?: return
                sendAction(AnalyticsAction.UpdateLoading(true, false))
                launchBackgroundWork(BackgroundKey.REPORT) {
                    val command = AnalyticsWorkCommand.ShiftPeriod(selection, -1, target)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AnalyticsEvent.ClickNextPeriod -> with(state()) {
                val selection = data?.selection ?: return
                sendAction(AnalyticsAction.UpdateLoading(true, false))
                launchBackgroundWork(BackgroundKey.REPORT) {
                    val command = AnalyticsWorkCommand.ShiftPeriod(selection, 1, target)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is AnalyticsEvent.ClickTarget -> with(state()) {
                val selection = data?.selection ?: return
                consumeOutput(AnalyticsOutput.NavigateToTarget(event.target, selection))
            }
            is AnalyticsEvent.ClickBack -> consumeOutput(AnalyticsOutput.NavigateToBack)
        }
    }

    override suspend fun reduce(
        action: AnalyticsAction,
        currentState: AnalyticsState,
    ) = when (action) {
        is AnalyticsAction.UpdateTarget -> currentState.copy(
            target = action.target,
        )
        is AnalyticsAction.UpdateData -> currentState.copy(
            data = action.data,
            isLoading = action.isLoading,
            isError = action.isError,
        )
        is AnalyticsAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
            isError = action.isError,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        REPORT,
    }

    class Factory(
        private val workProcessor: AnalyticsWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<AnalyticsComposeStore, AnalyticsState> {

        override fun create(savedState: AnalyticsState): AnalyticsComposeStore {
            return AnalyticsComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}
