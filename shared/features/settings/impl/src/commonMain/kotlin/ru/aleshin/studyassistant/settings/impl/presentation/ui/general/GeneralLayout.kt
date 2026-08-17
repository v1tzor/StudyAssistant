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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.general

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.models.ThemeUiType
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType
import ru.aleshin.studyassistant.settings.impl.presentation.ui.SettingsLayoutMode
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsExpandedPane
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsSelectorView
import ru.aleshin.studyassistant.settings.impl.presentation.ui.general.contract.GeneralState
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.delete_all_data_button
import ru.aleshin.studyassistant.settings.impl.resources.delete_schedule_button
import ru.aleshin.studyassistant.settings.impl.resources.ic_language
import ru.aleshin.studyassistant.settings.impl.resources.ic_palette
import ru.aleshin.studyassistant.settings.impl.resources.language_chooser_view_title
import ru.aleshin.studyassistant.settings.impl.resources.theme_chooser_view_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun GeneralLayout(
    modifier: Modifier = Modifier,
    layoutMode: SettingsLayoutMode,
    state: GeneralState,
    onSelectedLanguage: (LanguageUiType) -> Unit,
    onSelectedTheme: (ThemeUiType) -> Unit,
    onDeleteScheduleClick: () -> Unit,
    onDeleteAllDataClick: () -> Unit,
) {
    when (layoutMode) {
        SettingsLayoutMode.COMPACT -> GeneralCompactLayout(
            modifier = modifier,
            state = state,
            onSelectedLanguage = onSelectedLanguage,
            onSelectedTheme = onSelectedTheme,
            onDeleteScheduleClick = onDeleteScheduleClick,
            onDeleteAllDataClick = onDeleteAllDataClick,
        )
        SettingsLayoutMode.EXPANDED -> GeneralExpandedLayout(
            modifier = modifier,
            state = state,
            onSelectedLanguage = onSelectedLanguage,
            onSelectedTheme = onSelectedTheme,
            onDeleteScheduleClick = onDeleteScheduleClick,
            onDeleteAllDataClick = onDeleteAllDataClick,
        )
    }
}

@Composable
private fun GeneralCompactLayout(
    modifier: Modifier = Modifier,
    state: GeneralState,
    onSelectedLanguage: (LanguageUiType) -> Unit,
    onSelectedTheme: (ThemeUiType) -> Unit,
    onDeleteScheduleClick: () -> Unit,
    onDeleteAllDataClick: () -> Unit,
) {
    Column(
        modifier = modifier.padding(vertical = 24.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        GeneralSettingsItems(
            itemModifier = Modifier.padding(horizontal = 16.dp),
            useExpandedStyle = false,
            state = state,
            onSelectedLanguage = onSelectedLanguage,
            onSelectedTheme = onSelectedTheme,
            onDeleteScheduleClick = onDeleteScheduleClick,
            onDeleteAllDataClick = onDeleteAllDataClick,
        )
    }
}

@Composable
private fun GeneralExpandedLayout(
    modifier: Modifier = Modifier,
    state: GeneralState,
    onSelectedLanguage: (LanguageUiType) -> Unit,
    onSelectedTheme: (ThemeUiType) -> Unit,
    onDeleteScheduleClick: () -> Unit,
    onDeleteAllDataClick: () -> Unit,
) {
    SettingsExpandedPane(modifier = modifier) {
        GeneralSettingsItems(
            itemModifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth),
            useExpandedStyle = true,
            state = state,
            onSelectedLanguage = onSelectedLanguage,
            onSelectedTheme = onSelectedTheme,
            onDeleteScheduleClick = onDeleteScheduleClick,
            onDeleteAllDataClick = onDeleteAllDataClick,
        )
    }
}

@Composable
private fun GeneralSettingsItems(
    itemModifier: Modifier,
    useExpandedStyle: Boolean,
    state: GeneralState,
    onSelectedLanguage: (LanguageUiType) -> Unit,
    onSelectedTheme: (ThemeUiType) -> Unit,
    onDeleteScheduleClick: () -> Unit,
    onDeleteAllDataClick: () -> Unit,
) {
    SettingsSelectorView(
        onSelect = onSelectedLanguage,
        modifier = itemModifier,
        useExpandedStyle = useExpandedStyle,
        selected = state.settings?.languageType,
        allItems = remember { LanguageUiType.entries.toList() },
        icon = painterResource(Res.drawable.ic_language),
        title = stringResource(Res.string.language_chooser_view_title),
        itemName = { it.mapToString() },
    )
    SettingsSelectorView(
        onSelect = onSelectedTheme,
        modifier = itemModifier,
        useExpandedStyle = useExpandedStyle,
        selected = state.settings?.themeType,
        allItems = remember { ThemeUiType.entries.toList() },
        icon = painterResource(Res.drawable.ic_palette),
        title = stringResource(Res.string.theme_chooser_view_title),
        itemName = { it.mapToString() },
    )
    OutlinedButton(
        modifier = itemModifier.fillMaxWidth(),
        onClick = onDeleteScheduleClick,
    ) {
        Text(text = stringResource(Res.string.delete_schedule_button))
    }
    OutlinedButton(
        modifier = itemModifier.fillMaxWidth(),
        onClick = onDeleteAllDataClick,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.error,
        ),
    ) {
        Text(text = stringResource(Res.string.delete_all_data_button))
    }
}
