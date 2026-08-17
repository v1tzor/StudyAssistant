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

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsComparison
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsFailures
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGoalDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGranularity
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsLoadDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsOverview
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsRangeSelection
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsRegularity
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsSummary
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTaskDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.interactors.AnalyticsInteractor
import ru.aleshin.studyassistant.analytics.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsAction
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkResult
import ru.aleshin.studyassistant.core.common.functional.Either
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsPeriod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class AnalyticsWorkProcessorTest {

    @Test
    fun selectionObservationMapsDomainReportWithoutCalculations() = runBlocking {
        val report = report()
        val interactor = FakeAnalyticsInteractor(report)
        val processor = AnalyticsWorkProcessor.Base(interactor)

        val results = processor.work(
            AnalyticsWorkCommand.ObserveSelection(report.selection.mapToUi(), null),
        ).toList()

        val action = assertIs<WorkResult.Action<AnalyticsAction>>(results.single()).action
        assertEquals(AnalyticsAction.UpdateData(report.mapToUi(), false, false), action)
        assertEquals(report.selection, interactor.observedSelection)
    }

    private class FakeAnalyticsInteractor(
        private val report: AnalyticsOverview,
    ) : AnalyticsInteractor {

        var observedSelection: AnalyticsRangeSelection? = null

        override suspend fun fetchOverview(
            target: AnalyticsTarget?,
        ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview> = flowOf(Either.Right(report))

        override suspend fun fetchPeriod(
            period: AnalyticsPeriod,
            anchor: Instant,
            target: AnalyticsTarget?,
        ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview> = flowOf(Either.Right(report))

        override suspend fun fetchChangedPeriod(
            period: AnalyticsPeriod,
            selection: AnalyticsRangeSelection,
            target: AnalyticsTarget?,
        ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview> = flowOf(Either.Right(report))

        override suspend fun fetchCustom(
            from: Instant,
            to: Instant,
            target: AnalyticsTarget?,
        ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview> = flowOf(Either.Right(report))

        override suspend fun fetchShifted(
            selection: AnalyticsRangeSelection,
            amount: Int,
            target: AnalyticsTarget?,
        ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview> = flowOf(Either.Right(report))

        override suspend fun fetchSelection(
            selection: AnalyticsRangeSelection,
            target: AnalyticsTarget?,
        ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview> {
            observedSelection = selection
            return flowOf(Either.Right(report))
        }
    }

    private fun report(): AnalyticsOverview {
        val instant = Instant.fromEpochMilliseconds(0L)
        val selection = AnalyticsRangeSelection(
            period = AnalyticsPeriod.MONTH,
            range = TimeRange(instant, instant),
            previousRange = TimeRange(instant, instant),
            granularity = AnalyticsGranularity.DAY,
            comparisonCutoff = instant,
        )
        val summary = AnalyticsSummary(0L, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, null)
        return AnalyticsOverview(
            selection = selection,
            summary = summary,
            comparison = AnalyticsComparison(null, null, null, null),
            loadDistribution = AnalyticsLoadDistribution(emptyList(), 0f, null, 0, 7),
            taskDistribution = AnalyticsTaskDistribution(
                summary,
                emptyList(),
                0,
                0,
                0,
                0,
                0,
                0,
                0,
            ),
            goalDistribution = AnalyticsGoalDistribution(0, 0, 0, 0, 0, 0L, 0L, null, false),
            organizations = emptyList(),
            subjects = emptyList(),
            employees = emptyList(),
            regularity = AnalyticsRegularity(emptyMap(), 0, 0, 0),
            insights = emptyList(),
            targetDetails = null,
            hasData = false,
        )
    }
}
