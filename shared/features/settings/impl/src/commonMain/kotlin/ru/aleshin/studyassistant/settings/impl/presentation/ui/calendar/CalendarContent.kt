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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract.CalendarViewState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsSelectorView

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
@Composable
internal fun CalendarContent(
    state: CalendarViewState,
    modifier: Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onSelectedNumberOfWeek: (NumberOfRepeatWeek) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.padding(top = 8.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SettingsSelectorView(
            onSelect = onSelectedNumberOfWeek,
            modifier = Modifier.padding(horizontal = 16.dp),
            selected = settings?.numberOfWeek,
            allItems = NumberOfRepeatWeek.entries.toList(),
            icon = painterResource(SettingsThemeRes.icons.numberOfWeek),
            title = SettingsThemeRes.strings.numberOfRepeatWeekViewTitle,
            itemName = { it.mapToSting(StudyAssistantRes.strings) },
        )
    }
}