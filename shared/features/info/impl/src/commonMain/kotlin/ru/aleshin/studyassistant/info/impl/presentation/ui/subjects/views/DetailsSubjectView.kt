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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.material.endSide
import ru.aleshin.studyassistant.core.ui.views.InfoBadge
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dialog.WarningAlertDialog
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.delete_subject_warning_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_employee as core_ic_employee
import ru.aleshin.studyassistant.core.ui.resources.ic_map_marker as core_ic_map_marker
import ru.aleshin.studyassistant.core.ui.resources.warning_delete_confirm_title as core_warning_delete_confirm_title
import ru.aleshin.studyassistant.core.ui.resources.warning_dialog_title as core_warning_dialog_title

/**
 * @author Stanislav Aleshin on 18.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DetailsSubjectViewItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useExpandedStyle: Boolean = false,
    eventType: EventType,
    office: String,
    color: Color,
    name: String,
    teacher: EmployeeUi?,
    location: ContactInfoUi?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var deleteWarningDialogStatus by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissBoxValue ->
            when (dismissBoxValue) {
                SwipeToDismissBoxValue.EndToStart -> Unit
                SwipeToDismissBoxValue.StartToEnd -> {
                    deleteWarningDialogStatus = true
                }
                SwipeToDismissBoxValue.Settled -> Unit
            }
            return@rememberSwipeToDismissBoxState false
        },
        positionalThreshold = { it * .50f },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clipToBounds(),
        backgroundContent = {
            SwipeToDismissBackground(
                dismissState = dismissState,
                startToEndContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
                startToEndColor = MaterialTheme.colorScheme.errorContainer,
            )
        },
        enableDismissFromEndToStart = false,
        enableDismissFromStartToEnd = enabled,
    ) {
        DetailsSubjectView(
            onClick = onEdit,
            enabled = enabled,
            useExpandedStyle = useExpandedStyle,
            eventType = eventType,
            office = office,
            color = color,
            name = name,
            teacher = teacher,
            location = location,
        )
    }

    if (deleteWarningDialogStatus) {
        WarningAlertDialog(
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            },
            title = { Text(text = stringResource(CoreRes.string.core_warning_dialog_title)) },
            text = { Text(text = stringResource(Res.string.delete_subject_warning_title)) },
            confirmTitle = stringResource(CoreRes.string.core_warning_delete_confirm_title),
            onDismiss = { deleteWarningDialogStatus = false },
            onConfirm = {
                onDelete()
                deleteWarningDialogStatus = false
            },
        )
    }
}

@Composable
private fun DetailsSubjectView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    useExpandedStyle: Boolean = false,
    eventType: EventType,
    office: String,
    color: Color,
    name: String,
    teacher: EmployeeUi?,
    location: ContactInfoUi?,
) {
    val shape = if (useExpandedStyle) {
        MaterialTheme.shapes.extraLarge
    } else {
        MaterialTheme.shapes.large
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Surface(
                modifier = Modifier.width(4.dp).padding(vertical = 16.dp),
                shape = MaterialTheme.shapes.small.endSide,
                color = color,
                content = { Box(modifier = Modifier.fillMaxHeight()) }
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailsSubjectViewHeader(
                        eventType = eventType,
                        office = office,
                    )
                    DetailsSubjectViewContent(
                        eventType = eventType,
                        name = name,
                        useExpandedStyle = useExpandedStyle,
                    )
                }
                if (teacher != null || location != null) {
                    DetailsSubjectViewFooter(
                        teacher = teacher,
                        location = location,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsSubjectViewHeader(
    modifier: Modifier = Modifier,
    eventType: EventType,
    office: String,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(eventType.mapToIcon()),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.weight(1f))
        if (office.isNotBlank()) {
            InfoBadge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
                Text(
                    text = office,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun DetailsSubjectViewContent(
    modifier: Modifier = Modifier,
    eventType: EventType,
    name: String,
    useExpandedStyle: Boolean = false,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = eventType.mapToString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            text = name,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
            style = if (useExpandedStyle) {
                MaterialTheme.typography.titleLarge
            } else {
                MaterialTheme.typography.titleMedium
            },
        )
    }
}

@Composable
private fun DetailsSubjectViewFooter(
    modifier: Modifier = Modifier,
    teacher: EmployeeUi?,
    location: ContactInfoUi?,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (teacher != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_employee),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = teacher.officialName(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
        if (location != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(CoreRes.drawable.core_ic_map_marker),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = location.label ?: location.value,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
            }
        }
    }
}
