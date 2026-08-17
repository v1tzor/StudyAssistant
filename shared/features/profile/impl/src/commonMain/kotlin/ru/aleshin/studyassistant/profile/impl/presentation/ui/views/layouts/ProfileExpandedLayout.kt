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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileExpandedActions
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileExpandedHeader
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileExpandedTopBar

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ProfileExpandedLayout(
    modifier: Modifier = Modifier,
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
    Column(modifier = modifier.fillMaxSize()) {
        ProfileExpandedTopBar(
            modifier = Modifier.fillMaxWidth(),
            enabledEdit = !isLoading && profile != null,
            onEditClick = onEditClick,
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopStart,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(
                        insets = WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom),
                    )
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileExpandedHeader(
                    modifier = Modifier.widthIn(max = PROFILE_EXPANDED_CONTENT_MAX_WIDTH),
                    isLoading = isLoading,
                    profile = profile,
                )
                ProfileExpandedActions(
                    modifier = Modifier.widthIn(max = PROFILE_EXPANDED_CONTENT_MAX_WIDTH),
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
}

internal val PROFILE_EXPANDED_CONTENT_MAX_WIDTH = 720.dp
