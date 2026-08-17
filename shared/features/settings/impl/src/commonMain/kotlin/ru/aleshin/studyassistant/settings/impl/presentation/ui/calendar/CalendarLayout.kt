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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.presentation.models.settings.HolidaysUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.settings.impl.presentation.ui.SettingsLayoutMode
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.views.HolidaysView
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsExpandedPane
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsSelectorView
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.ic_calendar_week
import ru.aleshin.studyassistant.settings.impl.resources.number_of_repeat_week_view_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun CalendarLayout(
    modifier: Modifier = Modifier,
    layoutMode: SettingsLayoutMode,
    state: CalendarState,
    onSelectedNumberOfWeek: (NumberOfRepeatWeek) -> Unit,
    onUpdateHolidays: (List<HolidaysUi>) -> Unit,
) {
    when (layoutMode) {
        SettingsLayoutMode.COMPACT -> CalendarCompactLayout(
            modifier = modifier,
            state = state,
            onSelectedNumberOfWeek = onSelectedNumberOfWeek,
            onUpdateHolidays = onUpdateHolidays,
        )
        SettingsLayoutMode.EXPANDED -> CalendarExpandedLayout(
            modifier = modifier,
            state = state,
            onSelectedNumberOfWeek = onSelectedNumberOfWeek,
            onUpdateHolidays = onUpdateHolidays,
        )
    }
}

@Composable
private fun CalendarCompactLayout(
    modifier: Modifier = Modifier,
    state: CalendarState,
    onSelectedNumberOfWeek: (NumberOfRepeatWeek) -> Unit,
    onUpdateHolidays: (List<HolidaysUi>) -> Unit,
) {
    Column(
        modifier = modifier.padding(vertical = 24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        CalendarSettingsItems(
            itemModifier = Modifier.padding(horizontal = 16.dp),
            useExpandedStyle = false,
            state = state,
            onSelectedNumberOfWeek = onSelectedNumberOfWeek,
            onUpdateHolidays = onUpdateHolidays,
        )
    }
}

@Composable
private fun CalendarExpandedLayout(
    modifier: Modifier = Modifier,
    state: CalendarState,
    onSelectedNumberOfWeek: (NumberOfRepeatWeek) -> Unit,
    onUpdateHolidays: (List<HolidaysUi>) -> Unit,
) {
    SettingsExpandedPane(modifier = modifier) {
        CalendarSettingsItems(
            itemModifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth),
            useExpandedStyle = true,
            state = state,
            onSelectedNumberOfWeek = onSelectedNumberOfWeek,
            onUpdateHolidays = onUpdateHolidays,
        )
    }
}

@Composable
private fun CalendarSettingsItems(
    itemModifier: Modifier,
    useExpandedStyle: Boolean,
    state: CalendarState,
    onSelectedNumberOfWeek: (NumberOfRepeatWeek) -> Unit,
    onUpdateHolidays: (List<HolidaysUi>) -> Unit,
) {
    SettingsSelectorView(
        onSelect = onSelectedNumberOfWeek,
        modifier = itemModifier,
        enabled = state.settings != null,
        useExpandedStyle = useExpandedStyle,
        selected = state.settings?.numberOfWeek,
        allItems = remember { NumberOfRepeatWeek.entries.toList() },
        icon = painterResource(Res.drawable.ic_calendar_week),
        title = stringResource(Res.string.number_of_repeat_week_view_title),
        itemName = { it.mapToSting() },
    )
    HolidaysView(
        modifier = itemModifier,
        useExpandedStyle = useExpandedStyle,
        allOrganizations = state.allOrganizations,
        holidays = state.settings?.holidays ?: emptyList(),
        onUpdateHolidays = onUpdateHolidays,
    )
}
