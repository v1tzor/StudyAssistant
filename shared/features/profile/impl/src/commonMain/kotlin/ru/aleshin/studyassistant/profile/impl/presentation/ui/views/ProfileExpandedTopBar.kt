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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.views

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.views.TopAppBarButton
import ru.aleshin.studyassistant.core.ui.views.TopAppBarTitle
import ru.aleshin.studyassistant.profile.impl.resources.Res
import ru.aleshin.studyassistant.profile.impl.resources.edit_profile_desc
import ru.aleshin.studyassistant.profile.impl.resources.ic_edit
import ru.aleshin.studyassistant.profile.impl.resources.profile_header

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ProfileExpandedTopBar(
    modifier: Modifier = Modifier,
    enabledEdit: Boolean = true,
    onEditClick: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TopAppBarTitle(
                header = stringResource(Res.string.profile_header),
                textAlign = TextAlign.Start,
            )
        },
        actions = {
            TopAppBarButton(
                enabled = enabledEdit,
                imagePainter = painterResource(Res.drawable.ic_edit),
                imageDescription = stringResource(Res.string.edit_profile_desc),
                onButtonClick = onEditClick,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}
