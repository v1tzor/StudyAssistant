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

package ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.add_organization_title
import ru.aleshin.studyassistant.info.impl.resources.classes_duration_in_week_label
import ru.aleshin.studyassistant.info.impl.resources.edit_organization_title
import ru.aleshin.studyassistant.info.impl.resources.ic_star_circular
import ru.aleshin.studyassistant.info.impl.resources.main_organization_status
import ru.aleshin.studyassistant.info.impl.resources.number_of_classes_in_week_label
import ru.aleshin.studyassistant.info.impl.resources.organization_status_label
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_duration as core_ic_duration

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun OrganizationSelectCard(
    modifier: Modifier = Modifier,
    organization: OrganizationShortUi,
    isSelected: Boolean,
    classesInfo: OrganizationClassesInfoUi?,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
) {
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.surfaceContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }
    val titleColor = if (isSelected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val typeColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .width(ORGANIZATION_SELECT_CARD_WIDTH)
            .height(ORGANIZATION_SELECT_CARD_HEIGHT),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarView(
                    modifier = Modifier.size(72.dp),
                    firstName = organization.shortName.split(' ').getOrElse(0) { "-" },
                    secondName = organization.shortName.split(' ').getOrNull(1),
                    imageUrl = organization.avatar,
                    shape = MaterialTheme.shapes.extraLarge,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = organization.type.mapToSting(),
                        color = typeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = organization.shortName,
                        color = titleColor,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Min).weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (organization.isMain) {
                    OrganizationSelectInfoItem(
                        icon = painterResource(Res.drawable.ic_star_circular),
                        label = stringResource(Res.string.organization_status_label),
                        text = stringResource(Res.string.main_organization_status),
                        isSelected = isSelected,
                    )
                }
                if (organization.isMain && classesInfo != null) {
                    VerticalDivider(modifier = Modifier.padding(top = 8.dp))
                }
                if (classesInfo != null) {
                    OrganizationSelectInfoItem(
                        icon = painterResource(CoreRes.drawable.core_ic_duration),
                        label = stringResource(Res.string.classes_duration_in_week_label),
                        text = remember(classesInfo) { classesInfo.classesDurationString() },
                        isSelected = true,
                    )
                    VerticalDivider(modifier = Modifier.padding(top = 8.dp))
                    OrganizationSelectInfoItem(
                        icon = painterResource(CoreRes.drawable.core_ic_class),
                        label = stringResource(Res.string.number_of_classes_in_week_label),
                        text = remember(classesInfo) { classesInfo.numberOfClassesString() },
                        isSelected = true,
                    )
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth().height(40.dp),
                onClick = onEditClick,
            ) {
                Text(text = stringResource(Res.string.edit_organization_title))
            }
        }
    }
}

@Composable
internal fun AddOrganizationSelectCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .width(ORGANIZATION_SELECT_CARD_WIDTH)
            .height(ORGANIZATION_SELECT_CARD_HEIGHT),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.add_organization_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun OrganizationSelectInfoItem(
    icon: Painter,
    label: String,
    text: String,
    isSelected: Boolean,
) {
    val iconTint = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = iconTint,
        )
        Column {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = text,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

internal val ORGANIZATION_SELECT_CARD_WIDTH = 560.dp
internal val ORGANIZATION_SELECT_CARD_HEIGHT = 220.dp
