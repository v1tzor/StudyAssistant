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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import extensions.boldWeight
import functional.TimeRange
import mappers.format
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 25.05.2024.
 */
@Composable
internal fun CommonClassView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    highlightContent: Boolean = false,
    number: Int,
    timeRange: TimeRange,
    subject: SubjectUi?,
    office: String,
    organization: OrganizationShortUi?,
    headerBadge: @Composable (() -> Unit)? = null,
    footer: @Composable (ColumnScope.() -> Unit)? = {
        OfficeAndOrganizationFooter(
            office = office,
            organization = if (organization?.isMain == false) organization.shortName else null,
        )
    },
    trailingIcon: @Composable (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Row(
        modifier = modifier
            .padding(all = 4.dp)
            .height(IntrinsicSize.Max)
            .clip(MaterialTheme.shapes.small)
            .clickable(
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CommonClassViewColorIndicator(
            modifier = Modifier.fillMaxHeight(),
            number = number,
            indicatorColor = subject?.color?.let { Color(it) },
            numberColor = when (highlightContent) {
                true -> MaterialTheme.colorScheme.primary
                false -> MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        CommonClassViewContent(
            modifier = Modifier.weight(1f),
            timeRange = timeRange,
            subject = subject?.name,
            headerBadge = headerBadge,
            footer = footer,
            highlightContent = highlightContent
        )
        trailingIcon?.invoke()
    }
}

@Composable
internal fun OfficeAndOrganizationFooter(
    modifier: Modifier = Modifier,
    office: String,
    organization: String?,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = office,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
        if (organization != null) {
            Text(
                text = organization,
                color = MaterialTheme.colorScheme.tertiary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun CommonClassViewColorIndicator(
    modifier: Modifier = Modifier,
    number: Int,
    indicatorColor: Color?,
    numberColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = number.toString(),
            color = numberColor,
            style = MaterialTheme.typography.labelSmall,
        )
        Surface(
            modifier = Modifier.padding(bottom = 2.dp).fillMaxHeight().width(4.dp),
            shape = MaterialTheme.shapes.small,
            color = indicatorColor ?: MaterialTheme.colorScheme.outline,
            content = { Box(modifier = Modifier.fillMaxHeight()) }
        )
    }
}

@Composable
private fun CommonClassViewContent(
    modifier: Modifier = Modifier,
    timeRange: TimeRange,
    subject: String?,
    headerBadge: (@Composable () -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    highlightContent: Boolean = false,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = when (highlightContent) {
            true -> MaterialTheme.colorScheme.primaryContainer
            false -> Color.Transparent
        }
    ) {
        val paddingValues = when (highlightContent) {
            true -> PaddingValues(horizontal = 6.dp, vertical = 4.dp)
            false -> PaddingValues()
        }
        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = timeRange.format(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall.boldWeight(),
                )
                headerBadge?.invoke()
            }
            Text(
                text = subject ?: StudyAssistantRes.strings.noneTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
            )
            if (footer != null) {
                footer(this)
            }
        }
    }
}