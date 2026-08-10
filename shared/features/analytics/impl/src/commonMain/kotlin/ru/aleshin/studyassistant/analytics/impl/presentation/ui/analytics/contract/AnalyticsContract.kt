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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsFailures
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsPeriod
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsOverviewUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsRangeSelectionUi
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Serializable
internal data class AnalyticsState(
    val data: AnalyticsOverviewUi? = null,
    val target: AnalyticsTarget? = null,
    val isLoading: Boolean = true,
    val isError: Boolean = false,
) : StoreState

internal sealed class AnalyticsEvent : StoreEvent {
    data class Started(val inputData: AnalyticsInput, val isRestore: Boolean) : AnalyticsEvent()
    data object Retry : AnalyticsEvent()
    data class ChangePeriod(val period: AnalyticsPeriod) : AnalyticsEvent()
    data class SelectPeriodAnchor(val anchor: Instant) : AnalyticsEvent()
    data class SelectCustomRange(val from: Instant, val to: Instant) : AnalyticsEvent()
    data object ClickPreviousPeriod : AnalyticsEvent()
    data object ClickNextPeriod : AnalyticsEvent()
    data class ClickTarget(val target: AnalyticsTarget) : AnalyticsEvent()
    data object ClickBack : AnalyticsEvent()
}

internal sealed class AnalyticsEffect : StoreEffect {
    data class ShowError(val failures: AnalyticsFailures) : AnalyticsEffect()
}

internal sealed class AnalyticsAction : StoreAction {
    data class UpdateTarget(val target: AnalyticsTarget?) : AnalyticsAction()

    data class UpdateData(
        val data: AnalyticsOverviewUi,
        val isLoading: Boolean,
        val isError: Boolean,
    ) : AnalyticsAction()

    data class UpdateLoading(
        val isLoading: Boolean,
        val isError: Boolean,
    ) : AnalyticsAction()
}

internal data class AnalyticsInput(
    val target: AnalyticsTarget? = null,
    val selection: AnalyticsRangeSelectionUi? = null,
) : BaseInput

internal sealed class AnalyticsOutput : BaseOutput {
    data object NavigateToBack : AnalyticsOutput()
    data class NavigateToTarget(
        val target: AnalyticsTarget,
        val selection: AnalyticsRangeSelectionUi,
    ) : AnalyticsOutput()
}
