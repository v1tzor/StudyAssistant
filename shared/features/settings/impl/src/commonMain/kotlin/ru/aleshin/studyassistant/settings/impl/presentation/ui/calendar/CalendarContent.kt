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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.HolidaysUi
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.store.CalendarComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.views.HolidaysView
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsSelectorView

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
@Composable
internal fun CalendarContent(
    calendarComponent: CalendarComponent,
    modifier: Modifier = Modifier,
) {
    val store = calendarComponent.store
    val state by store.stateAsState()
    val strings = SettingsThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseCalendarContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onSelectedNumberOfWeek = {
                    store.dispatchEvent(CalendarEvent.ChangeNumberOfRepeatWeek(it))
                },
                onUpdateHolidays = {
                    store.dispatchEvent(CalendarEvent.UpdateHolidays(it))
                },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
    )

    store.handleEffects { effect ->
        when (effect) {
            is CalendarEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseCalendarContent(
    state: CalendarState,
    modifier: Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onSelectedNumberOfWeek: (NumberOfRepeatWeek) -> Unit,
    onUpdateHolidays: (List<HolidaysUi>) -> Unit,
) {
    Column(
        modifier = modifier.padding(vertical = 24.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSelectorView(
            onSelect = onSelectedNumberOfWeek,
            modifier = Modifier.padding(horizontal = 16.dp),
            enabled = state.settings != null,
            selected = state.settings?.numberOfWeek,
            allItems = remember { NumberOfRepeatWeek.entries.toList() },
            icon = painterResource(SettingsThemeRes.icons.numberOfWeek),
            title = SettingsThemeRes.strings.numberOfRepeatWeekViewTitle,
            itemName = { it.mapToSting(StudyAssistantRes.strings) },
        )
        HolidaysView(
            modifier = Modifier.padding(horizontal = 16.dp),
            allOrganizations = state.allOrganizations,
            holidays = state.settings?.holidays ?: emptyList(),
            onUpdateHolidays = onUpdateHolidays,
        )
    }
}