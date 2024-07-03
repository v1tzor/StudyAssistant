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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import mappers.mapToSting
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
@Composable
internal fun OrganizationView(
    modifier: Modifier = Modifier,
    organizationData: OrganizationUi,
    classesInfo: OrganizationClassesInfoUi?,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OrganizationAvatarView(
                    username = organizationData.shortName,
                    imageUrl = organizationData.avatar,
                )
                Column {
                    Text(
                        text = organizationData.type.mapToSting(StudyAssistantRes.strings),
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
                        style = MaterialTheme.typography.titleMedium,
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
                        icon = painterResource(InfoThemeRes.icons.mainOrganization),
                        label = InfoThemeRes.strings.organizationStatusLabel,
                        text = InfoThemeRes.strings.mainOrganizationStatus
                    )
                }
                if (organizationData.isMain && classesInfo != null) {
                    VerticalDivider(modifier = Modifier.padding(top = 12.dp))
                }
                if (classesInfo != null) {
                    OrganizationInfoViewItem(
                        icon = painterResource(StudyAssistantRes.icons.duration),
                        label = InfoThemeRes.strings.classesDurationInWeekLabel,
                        text = classesInfo.classesDurationString(),
                    )
                    VerticalDivider(modifier = Modifier.padding(top = 12.dp))
                    OrganizationInfoViewItem(
                        icon = painterResource(StudyAssistantRes.icons.classes),
                        label = InfoThemeRes.strings.numberOfClassesInWeekLabel,
                        text = classesInfo.numberOfClassesString(),
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
                    text = InfoThemeRes.strings.noneOrganizationTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun OrganizationAvatarView(
    modifier: Modifier = Modifier,
    username: String,
    imageUrl: String?,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        modifier = modifier.size(100.dp),
        shape = MaterialTheme.shapes.extraLarge,
        color = containerColor,
        contentColor = contentColor,
    ) {
        if (imageUrl != null) {
            // TODO Get image from firebase storage
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = username.substring(0, 2).uppercase(),
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.headlineLarge,
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