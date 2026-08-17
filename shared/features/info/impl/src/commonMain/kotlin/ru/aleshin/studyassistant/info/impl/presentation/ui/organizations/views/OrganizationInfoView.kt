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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.classes_duration_in_week_label
import ru.aleshin.studyassistant.info.impl.resources.ic_star_circular
import ru.aleshin.studyassistant.info.impl.resources.main_organization_status
import ru.aleshin.studyassistant.info.impl.resources.none_organization_title
import ru.aleshin.studyassistant.info.impl.resources.number_of_classes_in_week_label
import ru.aleshin.studyassistant.info.impl.resources.organization_status_label
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_duration as core_ic_duration

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
@Composable
internal fun OrganizationView(
    modifier: Modifier = Modifier,
    organizationData: OrganizationUi,
    classesInfo: OrganizationClassesInfoUi?,
    useExpandedStyle: Boolean = false,
) {
    val contentPadding = if (useExpandedStyle) 16.dp else 12.dp
    val avatarSize = if (useExpandedStyle) 120.dp else 100.dp
    val nameStyle = if (useExpandedStyle) {
        MaterialTheme.typography.titleLarge
    } else {
        MaterialTheme.typography.titleMedium
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarView(
                    modifier = Modifier.size(avatarSize),
                    firstName = organizationData.shortName.split(' ').getOrElse(0) { "-" },
                    secondName = organizationData.shortName.split(' ').getOrNull(1),
                    imageUrl = organizationData.avatar,
                    shape = MaterialTheme.shapes.extraLarge,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
                Column {
                    Text(
                        text = organizationData.type.mapToSting(),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = organizationData.shortName,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        style = nameStyle,
                    )
                }
            }
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (organizationData.isMain) {
                    OrganizationInfoViewItem(
                        icon = painterResource(Res.drawable.ic_star_circular),
                        label = stringResource(Res.string.organization_status_label),
                        text = stringResource(Res.string.main_organization_status)
                    )
                }
                if (organizationData.isMain && classesInfo != null) {
                    VerticalDivider(modifier = Modifier.padding(top = 12.dp))
                }
                if (classesInfo != null) {
                    OrganizationInfoViewItem(
                        icon = painterResource(CoreRes.drawable.core_ic_duration),
                        label = stringResource(Res.string.classes_duration_in_week_label),
                        text = remember(classesInfo) { classesInfo.classesDurationString() },
                    )
                    VerticalDivider(modifier = Modifier.padding(top = 12.dp))
                    OrganizationInfoViewItem(
                        icon = painterResource(CoreRes.drawable.core_ic_class),
                        label = stringResource(Res.string.number_of_classes_in_week_label),
                        text = remember(classesInfo) { classesInfo.numberOfClassesString() },
                    )
                }
            }
        }
    }
}

@Composable
internal fun NoneOrganizationView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(201.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    text = stringResource(Res.string.none_organization_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun OrganizationInfoViewItem(
    modifier: Modifier = Modifier,
    icon: Painter,
    label: String,
    text: String,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}