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

package ru.aleshin.studyassistant.profile.impl.presentation.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileViewState
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileActionsSection
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileInfoSection

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
@Composable
internal fun ProfileContent(
    state: ProfileViewState,
    modifier: Modifier,
    onFriendsClick: () -> Unit,
    onPrivacySettingsClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onNotifySettingsClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onPaymentsSettingsClick: () -> Unit,
) {
    Column(modifier = modifier) {
        ProfileInfoSection(
            profile = state.myProfile,
        )
        ProfileActionsSection(
            modifier = Modifier.weight(1f),
            profile = state.myProfile,
            requests = state.myFriendRequest,
            onFriendsClick = onFriendsClick,
            onPrivacySettingsClick = onPrivacySettingsClick,
            onGeneralSettingsClick = onGeneralSettingsClick,
            onNotifySettingsClick = onNotifySettingsClick,
            onCalendarSettingsClick = onCalendarSettingsClick,
            onPaymentsSettingsClick = onPaymentsSettingsClick,
        )
    }
}
