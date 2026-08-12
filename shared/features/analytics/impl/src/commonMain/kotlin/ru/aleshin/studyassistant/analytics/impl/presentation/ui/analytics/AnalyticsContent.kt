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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsPeriod
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsRenderState
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsEffect
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.contract.AnalyticsEvent
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.store.AnalyticsComponent
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsErrorView
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsPeriodPicker
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsTopAppBar
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.analyticsCalendarLocale
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_cancel
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_confirm
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_date_picker_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_detail_employee
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_detail_organization
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_detail_subject
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_error_message
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_header
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_range_picker_title
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalStudyAssistantLanguage
import ru.aleshin.studyassistant.core.ui.views.dayMonthYearFormat

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun AnalyticsContent(
    component: AnalyticsComponent,
    isDetails: Boolean,
    modifier: Modifier = Modifier,
) {
    val store = component.store
    val state by store.stateAsState()
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val snackbarState = remember { SnackbarHostState() }
    val language = LocalStudyAssistantLanguage.current
    val errorMessage = stringResource(Res.string.analytics_error_message)
    var showDatePicker by remember { mutableStateOf(false) }
    var showRangePicker by remember { mutableStateOf(false) }
    val title = when (state.target) {
        null -> stringResource(Res.string.analytics_header)
        is AnalyticsTarget.Organization -> state.data?.targetDetails?.organization?.shortName
            ?: stringResource(Res.string.analytics_detail_organization)
        is AnalyticsTarget.Subject -> state.data?.targetDetails?.subject?.name
            ?: stringResource(Res.string.analytics_detail_subject)
        is AnalyticsTarget.Employee -> state.data?.targetDetails?.employee?.fullName()
            ?: stringResource(Res.string.analytics_detail_employee)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            AnalyticsTopAppBar(
                title = title,
                onBackClick = { store.dispatchEvent(AnalyticsEvent.ClickBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) },
        contentWindowInsets = WindowInsets(0.dp),
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            state.data?.selection?.let { selection ->
                Box(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    AnalyticsPeriodPicker(
                        selection = selection,
                        onPeriodChange = { period ->
                            if (period == AnalyticsPeriod.CUSTOM) {
                                showRangePicker = true
                            } else {
                                store.dispatchEvent(AnalyticsEvent.ChangePeriod(period))
                            }
                        },
                        onRangeClick = {
                            if (selection.period == AnalyticsPeriod.CUSTOM) {
                                showRangePicker = true
                            } else {
                                showDatePicker = true
                            }
                        },
                        onPreviousClick = {
                            store.dispatchEvent(AnalyticsEvent.ClickPreviousPeriod)
                        },
                        onNextClick = {
                            store.dispatchEvent(AnalyticsEvent.ClickNextPeriod)
                        },
                        modifier = Modifier.widthIn(max = PICKER_MAX_WIDTH),
                    )
                }
            }
            if (state.isLoading && state.data != null) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Crossfade(
                targetState = when {
                    state.isLoading && state.data == null -> AnalyticsRenderState.LOADING
                    state.isError && state.data == null -> AnalyticsRenderState.ERROR
                    state.data == null -> AnalyticsRenderState.LOADING
                    state.data?.hasData == false -> AnalyticsRenderState.EMPTY
                    else -> AnalyticsRenderState.CONTENT
                },
                modifier = Modifier.weight(1f),
            ) { renderState ->
                when (renderState) {
                    AnalyticsRenderState.LOADING -> AnalyticsLoadingLayout()
                    AnalyticsRenderState.ERROR -> AnalyticsErrorView(
                        onRetry = { store.dispatchEvent(AnalyticsEvent.Retry) },
                        modifier = Modifier.padding(16.dp),
                    )
                    AnalyticsRenderState.EMPTY -> AnalyticsEmptyLayout(Modifier.padding(16.dp))
                    AnalyticsRenderState.CONTENT -> state.data?.let { data ->
                        if (isDetails) {
                            AnalyticsDetailsLayout(
                                data = data,
                                adaptiveInfo = adaptiveInfo,
                                onTargetClick = {
                                    store.dispatchEvent(AnalyticsEvent.ClickTarget(it))
                                },
                            )
                        } else {
                            AnalyticsOverviewLayout(
                                data = data,
                                adaptiveInfo = adaptiveInfo,
                                onTargetClick = {
                                    store.dispatchEvent(AnalyticsEvent.ClickTarget(it))
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val selection = state.data?.selection
        val initialDate = selection?.range?.from?.toPickerMillis()
        val pickerState = remember(initialDate, language) {
            androidx.compose.material3.DatePickerState(
                locale = analyticsCalendarLocale(language.code),
                initialSelectedDateMillis = initialDate,
            )
        }
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let {
                            store.dispatchEvent(AnalyticsEvent.SelectPeriodAnchor(it.fromPickerMillis()))
                        }
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(Res.string.analytics_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.analytics_cancel))
                }
            },
        ) {
            DatePicker(
                state = pickerState,
                title = {
                    Text(
                        text = stringResource(Res.string.analytics_date_picker_title),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                        color = DatePickerDefaults.colors().titleContentColor,
                    )
                },
                headline = {
                    Text(
                        text = pickerState.selectedDateMillis?.toPickerTitle().orEmpty(),
                        modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                showModeToggle = false,
            )
        }
    }

    if (showRangePicker) {
        val selection = state.data?.selection
        val initialFrom = selection?.range?.from?.toPickerMillis()
        val initialTo = selection?.range?.to?.toPickerMillis()
        val pickerState = remember(initialFrom, initialTo, language) {
            androidx.compose.material3.DateRangePickerState(
                locale = analyticsCalendarLocale(language.code),
                initialSelectedStartDateMillis = initialFrom,
                initialSelectedEndDateMillis = initialTo,
            )
        }
        DatePickerDialog(
            onDismissRequest = { showRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val from = pickerState.selectedStartDateMillis
                        val to = pickerState.selectedEndDateMillis
                        if (from != null && to != null) {
                            store.dispatchEvent(
                                AnalyticsEvent.SelectCustomRange(
                                    from.fromPickerMillis(),
                                    to.fromPickerMillis(),
                                ),
                            )
                        }
                        showRangePicker = false
                    },
                ) {
                    Text(stringResource(Res.string.analytics_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showRangePicker = false }) {
                    Text(stringResource(Res.string.analytics_cancel))
                }
            },
        ) {
            DateRangePicker(
                state = pickerState,
                title = {
                    Text(
                        text = stringResource(Res.string.analytics_range_picker_title),
                        modifier = Modifier.padding(start = 64.dp, end = 12.dp),
                        color = DatePickerDefaults.colors().titleContentColor,
                    )
                },
                headline = {
                    val from = pickerState.selectedStartDateMillis?.toPickerTitle()
                    val to = pickerState.selectedEndDateMillis?.toPickerTitle()
                    Text(
                        text = when {
                            from == null -> ""
                            to == null -> from
                            else -> "$from – $to"
                        },
                        modifier = Modifier.padding(start = 64.dp, end = 12.dp, bottom = 12.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
                showModeToggle = false,
            )
        }
    }

    store.handleEffects { effect ->
        when (effect) {
            is AnalyticsEffect.ShowError -> snackbarState.showSnackbar(
                errorMessage,
            )
        }
    }
}

private fun Instant.toPickerMillis(): Long {
    return toLocalDateTime(TimeZone.currentSystemDefault()).date
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()
}

private fun Long.fromPickerMillis(): Instant {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .date
        .atStartOfDayIn(TimeZone.currentSystemDefault())
}

private fun Long.toPickerTitle(): String {
    return fromPickerMillis().formatByTimeZone(DateTimeComponents.Formats.dayMonthYearFormat())
}

private val PICKER_MAX_WIDTH = 1120.dp
