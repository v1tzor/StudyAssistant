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

package ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
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
import ru.aleshin.studyassistant.users.impl.resources.Res
import ru.aleshin.studyassistant.users.impl.resources.employee_profile_header

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EmployeeProfileTopBar(
    modifier: Modifier = Modifier,
    enabledEdit: Boolean,
    isExpanded: Boolean = false,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val navigationIcon: @Composable () -> Unit = {
        IconButton(onClick = onBackClick) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
        }
    }
    val actions: @Composable () -> Unit = {
        IconButton(
            enabled = enabledEdit,
            onClick = onEditClick,
        ) {
            Icon(imageVector = Icons.Default.Edit, contentDescription = null)
        }
    }
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
    )
    if (isExpanded) {
        TopAppBar(
            modifier = modifier,
            title = {
                TopAppBarTitle(
                    header = stringResource(Res.string.employee_profile_header),
                    textAlign = TextAlign.Start,
                )
            },
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = colors,
        )
    } else {
        CenterAlignedTopAppBar(
            modifier = modifier,
            title = {
                Text(text = stringResource(Res.string.employee_profile_header))
            },
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = colors,
        )
    }
}