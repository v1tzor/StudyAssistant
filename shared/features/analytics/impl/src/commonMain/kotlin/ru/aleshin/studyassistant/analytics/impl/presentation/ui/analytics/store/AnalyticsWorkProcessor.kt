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

import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsPeriod
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.domain.interactors.AnalyticsInteractor
import ru.aleshin.studyassistant.analytics.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.analytics.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsRangeSelectionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsAction
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsEffect
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsOutput
import ru.aleshin.studyassistant.core.common.architecture.store.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.store.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkCommand
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AnalyticsWorkProcessor :
    FlowWorkProcessor<AnalyticsWorkCommand, AnalyticsAction, AnalyticsEffect, AnalyticsOutput> {

    class Base(
        private val interactor: AnalyticsInteractor,
    ) : AnalyticsWorkProcessor {

        override suspend fun work(command: AnalyticsWorkCommand) = when (command) {
            is AnalyticsWorkCommand.ObserveDefault -> observeDefaultWork(command.target)
            is AnalyticsWorkCommand.ObserveSelection -> observeSelectionWork(command.selection, command.target)
            is AnalyticsWorkCommand.ChangePeriod -> changePeriodWork(
                command.period,
                command.selection,
                command.target,
            )
            is AnalyticsWorkCommand.SelectPeriodAnchor -> selectPeriodAnchorWork(
                command.period,
                command.anchor,
                command.target,
            )
            is AnalyticsWorkCommand.SelectCustomRange -> selectCustomRangeWork(
                command.from,
                command.to,
                command.target,
            )
            is AnalyticsWorkCommand.ShiftPeriod -> shiftPeriodWork(
                command.selection,
                command.amount,
                command.target,
            )
        }

        private fun observeDefaultWork(target: AnalyticsTarget?) = flow {
            interactor.fetchDefault(target).collectAndHandle(
                onLeftAction = {
                    emit(ActionResult(AnalyticsAction.UpdateLoading(false, true)))
                    emit(EffectResult(AnalyticsEffect.ShowError(it)))
                },
                onRightAction = {
                    emit(ActionResult(AnalyticsAction.UpdateData(it.mapToUi(), false, false)))
                },
            )
        }

        private fun observeSelectionWork(
            selection: AnalyticsRangeSelectionUi,
            target: AnalyticsTarget?,
        ) = flow {
            interactor.fetchSelection(selection.mapToDomain(), target).collectAndHandle(
                onLeftAction = {
                    emit(ActionResult(AnalyticsAction.UpdateLoading(false, true)))
                    emit(EffectResult(AnalyticsEffect.ShowError(it)))
                },
                onRightAction = {
                    emit(ActionResult(AnalyticsAction.UpdateData(it.mapToUi(), false, false)))
                },
            )
        }

        private fun changePeriodWork(
            period: AnalyticsPeriod,
            selection: AnalyticsRangeSelectionUi,
            target: AnalyticsTarget?,
        ) = flow {
            interactor.fetchChangedPeriod(period, selection.mapToDomain(), target).collectAndHandle(
                onLeftAction = {
                    emit(ActionResult(AnalyticsAction.UpdateLoading(false, true)))
                    emit(EffectResult(AnalyticsEffect.ShowError(it)))
                },
                onRightAction = {
                    emit(ActionResult(AnalyticsAction.UpdateData(it.mapToUi(), false, false)))
                },
            )
        }

        private fun selectPeriodAnchorWork(
            period: AnalyticsPeriod,
            anchor: Instant,
            target: AnalyticsTarget?,
        ) = flow {
            interactor.fetchPeriod(period, anchor, target).collectAndHandle(
                onLeftAction = {
                    emit(ActionResult(AnalyticsAction.UpdateLoading(false, true)))
                    emit(EffectResult(AnalyticsEffect.ShowError(it)))
                },
                onRightAction = {
                    emit(ActionResult(AnalyticsAction.UpdateData(it.mapToUi(), false, false)))
                },
            )
        }

        private fun selectCustomRangeWork(
            from: Instant,
            to: Instant,
            target: AnalyticsTarget?,
        ) = flow {
            interactor.fetchCustom(from, to, target).collectAndHandle(
                onLeftAction = {
                    emit(ActionResult(AnalyticsAction.UpdateLoading(false, true)))
                    emit(EffectResult(AnalyticsEffect.ShowError(it)))
                },
                onRightAction = {
                    emit(ActionResult(AnalyticsAction.UpdateData(it.mapToUi(), false, false)))
                },
            )
        }

        private fun shiftPeriodWork(
            selection: AnalyticsRangeSelectionUi,
            amount: Int,
            target: AnalyticsTarget?,
        ) = flow {
            interactor.fetchShifted(selection.mapToDomain(), amount, target).collectAndHandle(
                onLeftAction = {
                    emit(ActionResult(AnalyticsAction.UpdateLoading(false, true)))
                    emit(EffectResult(AnalyticsEffect.ShowError(it)))
                },
                onRightAction = {
                    emit(ActionResult(AnalyticsAction.UpdateData(it.mapToUi(), false, false)))
                },
            )
        }
    }
}

internal sealed class AnalyticsWorkCommand : WorkCommand {
    data class ObserveDefault(val target: AnalyticsTarget?) : AnalyticsWorkCommand()
    data class ObserveSelection(
        val selection: AnalyticsRangeSelectionUi,
        val target: AnalyticsTarget?,
    ) : AnalyticsWorkCommand()

    data class ChangePeriod(
        val period: AnalyticsPeriod,
        val selection: AnalyticsRangeSelectionUi,
        val target: AnalyticsTarget?,
    ) : AnalyticsWorkCommand()

    data class SelectPeriodAnchor(
        val period: AnalyticsPeriod,
        val anchor: Instant,
        val target: AnalyticsTarget?,
    ) : AnalyticsWorkCommand()

    data class SelectCustomRange(
        val from: Instant,
        val to: Instant,
        val target: AnalyticsTarget?,
    ) : AnalyticsWorkCommand()

    data class ShiftPeriod(
        val selection: AnalyticsRangeSelectionUi,
        val amount: Int,
        val target: AnalyticsTarget?,
    ) : AnalyticsWorkCommand()
}
