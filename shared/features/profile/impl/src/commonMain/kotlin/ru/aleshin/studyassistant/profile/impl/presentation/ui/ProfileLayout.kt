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

package ru.aleshin.studyassistant.profile.impl.presentation.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.layouts.ProfileCompactLayout
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.layouts.ProfileExpandedLayout

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ProfileLayout(
    modifier: Modifier = Modifier,
    layoutMode: ProfileLayoutMode,
    isLoading: Boolean,
    profile: ProfileUi?,
    onEditClick: () -> Unit,
    onAboutAppClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onNotifySettingsClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onAiSettingsClick: () -> Unit,
    onShareScheduleClick: () -> Unit,
) {
    when (layoutMode) {
        ProfileLayoutMode.COMPACT -> ProfileCompactLayout(
            modifier = modifier,
            isLoading = isLoading,
            profile = profile,
            onAboutAppClick = onAboutAppClick,
            onGeneralSettingsClick = onGeneralSettingsClick,
            onNotifySettingsClick = onNotifySettingsClick,
            onCalendarSettingsClick = onCalendarSettingsClick,
            onAiSettingsClick = onAiSettingsClick,
            onShareScheduleClick = onShareScheduleClick,
        )
        ProfileLayoutMode.EXPANDED -> ProfileExpandedLayout(
            modifier = modifier,
            isLoading = isLoading,
            profile = profile,
            onEditClick = onEditClick,
            onAboutAppClick = onAboutAppClick,
            onGeneralSettingsClick = onGeneralSettingsClick,
            onNotifySettingsClick = onNotifySettingsClick,
            onCalendarSettingsClick = onCalendarSettingsClick,
            onAiSettingsClick = onAiSettingsClick,
            onShareScheduleClick = onShareScheduleClick,
        )
    }
}
