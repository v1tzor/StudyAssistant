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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.views.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileActionsSection
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileInfoSection

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ProfileCompactLayout(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    profile: ProfileUi?,
    onAboutAppClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onNotifySettingsClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onAiSettingsClick: () -> Unit,
    onShareScheduleClick: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = PROFILE_COMPACT_CONTENT_MAX_WIDTH),
        ) {
            ProfileInfoSection(
                isLoading = isLoading,
                profile = profile,
            )
            ProfileActionsSection(
                columns = 2,
                modifier = Modifier.weight(1f),
                onAboutAppClick = onAboutAppClick,
                onGeneralSettingsClick = onGeneralSettingsClick,
                onNotifySettingsClick = onNotifySettingsClick,
                onCalendarSettingsClick = onCalendarSettingsClick,
                onAiSettingsClick = onAiSettingsClick,
                onShareScheduleClick = onShareScheduleClick,
            )
        }
    }
}

private val PROFILE_COMPACT_CONTENT_MAX_WIDTH = 720.dp
