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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import theme.StudyAssistantRes
import views.PlaceholderBox

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
internal fun CommonClassView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    number: Int,
    timeRange: TimeRange,
    subject: SubjectUi?,
    office: String,
    organization: OrganizationShortUi?,
    headerBadge: (@Composable () -> Unit)? = null,
    footer: (@Composable ColumnScope.() -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    highlightContent: Boolean = false,
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
            office = office,
            organization = organization?.takeIf { !it.isMain }?.shortName,
            headerBadge = headerBadge,
            footer = footer,
            highlightContent = highlightContent
        )
        trailingIcon?.invoke()
    }
}

@Composable
internal fun CommonClassViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PlaceholderBox(
            modifier = Modifier.height(52.dp).width(4.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PlaceholderBox(
                modifier = Modifier.size(90.dp, 16.dp),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            )
            PlaceholderBox(
                modifier = Modifier.height(32.dp).fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainer,
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
    office: String,
    organization: String?,
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
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
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
        }
    }
}