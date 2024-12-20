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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.endSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.timeFormat
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 14.07.2024.
 */
@Composable
internal fun DetailsClassViewItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    number: Int,
    timeRange: TimeRange,
    subject: SubjectUi?,
    eventType: EventType,
    office: String,
    organization: OrganizationShortUi?,
    teacher: EmployeeUi?,
    location: ContactInfoUi?,
    trailingActions: @Composable (RowScope.() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailsClassTime(
            modifier = Modifier.fillMaxHeight(),
            number = number,
            timeRange = timeRange,
        )
        Row(modifier = Modifier.weight(1f)) {
            DetailsClassView(
                modifier = Modifier.weight(1f),
                onClick = onClick,
                enabled = enabled,
                subject = subject,
                eventType = eventType,
                office = office,
                organization = organization,
                teacher = teacher,
                location = location,
                interactionSource = interactionSource,
            )
            if (trailingActions != null) {
                Row(
                    modifier = Modifier.fillMaxHeight(),
                    verticalAlignment = Alignment.CenterVertically,
                    content = trailingActions,
                )
            }
        }
    }
}

@Composable
internal fun DetailsClassViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.size(36.dp, 125.dp)) {
            PlaceholderBox(
                modifier = Modifier.size(36.dp, 20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            Spacer(modifier = Modifier.weight(1f))
            PlaceholderBox(
                modifier = Modifier.size(36.dp, 20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
        }
        Surface(
            modifier = Modifier.fillMaxWidth().height(125.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PlaceholderBox(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                    PlaceholderBox(
                        modifier = Modifier.height(40.dp).fillMaxWidth(0.8f),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlaceholderBox(
                        modifier = Modifier.height(18.dp).fillMaxWidth(0.6f),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                }
            }
        }
    }
}

@Composable
internal fun AddClassView(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(42.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = EditorThemeRes.strings.addTitle,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun DetailsClassTime(
    modifier: Modifier = Modifier,
    number: Int,
    timeRange: TimeRange,
) {
    Column(
        modifier = modifier.defaultMinSize(minWidth = 36.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = timeRange.from.formatByTimeZone(DateTimeComponents.Formats.timeFormat()),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text = number.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
        Text(
            text = timeRange.to.formatByTimeZone(DateTimeComponents.Formats.timeFormat()),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun DetailsClassView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    subject: SubjectUi?,
    eventType: EventType,
    office: String,
    teacher: EmployeeUi?,
    organization: OrganizationShortUi?,
    location: ContactInfoUi?,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        interactionSource = interactionSource,
    ) {
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            DetailsClassColorIndicator(
                modifier = Modifier.fillMaxHeight(),
                indicatorColor = subject?.color?.let { Color(it) }
            )
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    DetailsClassViewHeader(eventType = eventType)
                    DetailsClassViewContent(subject = subject, eventType = eventType)
                }
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    DetailsClassViewFooter(
                        office = office,
                        teacher = teacher ?: subject?.teacher,
                        organization = organization,
                        location = location,
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsClassViewHeader(
    modifier: Modifier = Modifier,
    eventType: EventType,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = painterResource(eventType.mapToIcon(StudyAssistantRes.icons)),
            contentDescription = eventType.mapToString(StudyAssistantRes.strings),
            tint = MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DetailsClassViewContent(
    modifier: Modifier = Modifier,
    subject: SubjectUi?,
    eventType: EventType,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = eventType.mapToString(StudyAssistantRes.strings),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = subject?.name ?: StudyAssistantRes.strings.noneTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
private fun DetailsClassViewFooter(
    modifier: Modifier = Modifier,
    office: String,
    teacher: EmployeeUi?,
    organization: OrganizationShortUi?,
    location: ContactInfoUi?,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (teacher != null) {
                DetailsClassViewFooterItem(
                    icon = painterResource(StudyAssistantRes.icons.employee),
                    text = teacher.officialName(),
                )
            }
            if (organization?.isMain == false) {
                DetailsClassViewFooterItem(
                    icon = painterResource(StudyAssistantRes.icons.organization),
                    text = organization.shortName,
                )
            }
            if (location != null) {
                DetailsClassViewFooterItem(
                    icon = painterResource(StudyAssistantRes.icons.location),
                    text = location.label ?: location.value,
                )
            }
        }
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.surfaceVariant,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                text = office,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun DetailsClassViewFooterItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    maxLines: Int = 1,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = icon,
            contentDescription = null,
            tint = color,
        )
        Text(
            text = text,
            color = color,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun DetailsClassColorIndicator(
    modifier: Modifier = Modifier,
    indicatorColor: Color?,
) {
    Surface(
        modifier = modifier.fillMaxHeight().width(4.dp).padding(vertical = 16.dp),
        shape = MaterialTheme.shapes.full.endSide,
        color = indicatorColor ?: MaterialTheme.colorScheme.outline,
        content = { Box(modifier = Modifier.fillMaxHeight()) }
    )
}