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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.views.TopAppBarTitle
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.share_header
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.back_icon_desc as core_back_icon_desc

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ShareTopBar(
    modifier: Modifier = Modifier,
    titleAlign: TextAlign = TextAlign.Center,
    onBackClick: () -> Unit,
) {
    val navigationIcon: @Composable () -> Unit = {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = stringResource(CoreRes.string.core_back_icon_desc),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
    )
    if (titleAlign == TextAlign.Start) {
        TopAppBar(
            modifier = modifier,
            title = {
                TopAppBarTitle(
                    header = stringResource(Res.string.share_header),
                    textAlign = TextAlign.Start,
                )
            },
            navigationIcon = navigationIcon,
            colors = colors,
        )
    } else {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = { Text(text = stringResource(Res.string.share_header)) },
            navigationIcon = navigationIcon,
            colors = colors,
        )
    }
}