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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfoType
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogNotSelectedItemView
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
@Composable
internal fun SharedOrganizationView(
    modifier: Modifier = Modifier,
    isLinked: Boolean,
    isLoadingLinkedOrganization: Boolean,
    shortName: String,
    type: OrganizationType,
    groupedContactInfo: Map<ContactInfoUi, ContactInfoType>,
    onLinkedChange: () -> Unit,
) {
    Surface(
        modifier = modifier.animateContentSize(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, bottom = 8.dp, end = 8.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = type.mapToSting(StudyAssistantRes.strings),
                        color = MaterialTheme.colorScheme.primary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = shortName,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Button(
                    onClick = onLinkedChange,
                    enabled = !isLoadingLinkedOrganization,
                    modifier = Modifier.height(32.dp).animateContentSize(),
                    colors = if (isLinked) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    },
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 4.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    ),
                ) {
                    Crossfade(
                        targetState = isLoadingLinkedOrganization,
                        animationSpec = spring(
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = Spring.DefaultDisplacementThreshold,
                        )
                    ) { loading ->
                        if (!loading) {
                            if (isLinked) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = ScheduleThemeRes.strings.unlinkButtonTitle, maxLines = 1)
                                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(text = ScheduleThemeRes.strings.linkButtonTitle, maxLines = 1)
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.ArrowRight,
                                        contentDescription = null
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.width(90.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 3.dp,
                                )
                            }
                        }
                    }
                }
            }
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState).animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var expandedContactInfo by remember { mutableStateOf<ContactInfoUi?>(null) }
                groupedContactInfo.forEach { contactInfoEntry ->
                    OrganizationContactInfoItem(
                        onClick = {
                            expandedContactInfo = if (expandedContactInfo == contactInfoEntry.key) {
                                null
                            } else {
                                contactInfoEntry.key
                            }
                        },
                        isExpanded = contactInfoEntry.key == expandedContactInfo,
                        icon = painterResource(contactInfoEntry.value.mapToIcon(StudyAssistantRes.icons)),
                        contactInfo = contactInfoEntry.key,
                    )
                }
            }
        }
    }
}

@Composable
private fun OrganizationContactInfoItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isExpanded: Boolean,
    icon: Painter,
    contactInfo: ContactInfoUi,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.animateContentSize(),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            if (isExpanded) {
                Column(modifier = Modifier.widthIn(max = 175.dp)) {
                    if (contactInfo.label != null) {
                        Text(
                            text = contactInfo.label,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Text(
                        text = contactInfo.value,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SharedOrganizationViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.fillMaxWidth().height(120.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationLinkedDialog(
    modifier: Modifier = Modifier,
    selected: OrganizationShortUi?,
    organizations: List<OrganizationShortUi>,
    onDismiss: () -> Unit,
    onConfirm: (OrganizationShortUi?) -> Unit,
) {
    var selectedOrganization by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedOrganization,
        items = organizations,
        header = ScheduleThemeRes.strings.organizationLinkerDialogHeader,
        title = ScheduleThemeRes.strings.organizationLinkerDialogTitle,
        itemView = { organization ->
            SelectorDialogItemView(
                onClick = { selectedOrganization = organization },
                selected = organization.uid == selectedOrganization?.uid,
                title = organization.shortName,
                label = organization.type.mapToSting(StudyAssistantRes.strings),
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedOrganization == null,
                onClick = { selectedOrganization = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}