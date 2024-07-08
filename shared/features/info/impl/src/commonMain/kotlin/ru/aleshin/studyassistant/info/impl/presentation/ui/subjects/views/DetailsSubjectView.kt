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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.InfoBadge
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.info.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 18.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DetailsSubjectViewItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    eventType: EventType,
    office: String,
    color: Color,
    name: String,
    teacher: EmployeeUi?,
    location: ContactInfoUi?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissBoxValue ->
            when (dismissBoxValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> false
                SwipeToDismissBoxValue.Settled -> false
            }
        },
        positionalThreshold = { it * .50f },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.clipToBounds(),
        backgroundContent = {
            SwipeToDismissBackground(
                dismissState = dismissState,
                endToStartContent = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                    )
                },
                endToStartColor = MaterialTheme.colorScheme.errorContainer,
            )
        },
        enableDismissFromEndToStart = enabled,
        enableDismissFromStartToEnd = false,
    ) {
        DetailsSubjectView(
            onClick = onEdit,
            enabled = enabled,
            eventType = eventType,
            office = office,
            color = color,
            name = name,
            teacher = teacher,
            location = location,
        )
    }
}

@Composable
private fun DetailsSubjectView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    eventType: EventType,
    office: String,
    color: Color,
    name: String,
    teacher: EmployeeUi?,
    location: ContactInfoUi?,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            Surface(
                modifier = Modifier.width(4.dp).padding(vertical = 16.dp),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
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
                    )
                }
                DetailsSubjectViewFooter(
                    teacher = teacher,
                    location = location,
                )
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
            painter = painterResource(eventType.mapToIcon(StudyAssistantRes.icons)),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.weight(1f))
        InfoBadge(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
            Text(
                text = office,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DetailsSubjectViewContent(
    modifier: Modifier = Modifier,
    eventType: EventType,
    name: String,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = eventType.mapToString(StudyAssistantRes.strings),
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
            style = MaterialTheme.typography.titleMedium,
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = painterResource(StudyAssistantRes.icons.employee),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = teacher?.officialName() ?: StudyAssistantRes.strings.noneTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
        }
        if (location != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(StudyAssistantRes.icons.location),
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