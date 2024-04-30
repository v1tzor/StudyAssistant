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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.views

import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileThemeRes
import views.TopAppBarButton
import views.TopAppBarTitle

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun ProfileTopBar(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSignOutClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TopAppBarTitle(
                textAlign = TextAlign.Center,
                text = ProfileThemeRes.strings.profileHeader,
            )
        },
        navigationIcon = {
            TopAppBarButton(
                enabled = enabled,
                imagePainter = painterResource(ProfileThemeRes.icons.signOut),
                imageDescription = ProfileThemeRes.strings.signOutDesc,
                onButtonClick = onSignOutClick,
            )
        },
        actions = {
            TopAppBarButton(
                enabled = enabled,
                imagePainter = painterResource(ProfileThemeRes.icons.edit),
                imageDescription = ProfileThemeRes.strings.editProfileDesc,
                onButtonClick = onEditClick,
            )
        },
        backgroundColor = MaterialTheme.colorScheme.background,
        elevation = 0.dp,
    )
}