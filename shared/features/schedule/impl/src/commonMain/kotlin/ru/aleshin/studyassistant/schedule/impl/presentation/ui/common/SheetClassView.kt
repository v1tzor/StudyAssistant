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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
@Composable
internal fun SheetClassView(
    modifier: Modifier = Modifier,
    subject: SubjectUi?,
    office: String,
    organization: OrganizationShortUi?,
    teacher: EmployeeUi?,
    location: ContactInfoUi?,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            SheetClassViewHeader(
                subject = subject,
                office = office,
            )
            SheetClassViewFooter(
                organization = organization,
                teacher = teacher ?: subject?.teacher,
                location = location,
            )
        }
    }
}

@Composable
private fun SheetClassViewHeader(
    modifier: Modifier = Modifier,
    subject: SubjectUi?,
    office: String,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = (subject?.eventType ?: EventType.CLASS).mapToString(StudyAssistantRes.strings),
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
@OptIn(ExperimentalLayoutApi::class)
private fun SheetClassViewFooter(
    modifier: Modifier = Modifier,
    organization: OrganizationShortUi?,
    teacher: EmployeeUi?,
    location: ContactInfoUi?,
) {
    FlowRow(
        modifier = modifier.heightIn(max = 66.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        maxItemsInEachRow = 2,
    ) {
        if (teacher != null) {
            SheetClassViewFooterItem(
                icon = painterResource(StudyAssistantRes.icons.employee),
                text = teacher.officialName()
            )
        }
        if (organization?.isMain == false) {
            SheetClassViewFooterItem(
                icon = painterResource(StudyAssistantRes.icons.organization),
                text = organization.shortName,
            )
        }
        if (location != null) {
            SheetClassViewFooterItem(
                icon = painterResource(StudyAssistantRes.icons.location),
                text = location.label ?: location.value,
            )
        }
    }
}

@Composable
private fun SheetClassViewFooterItem(
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