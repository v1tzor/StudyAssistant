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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToIcon
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.theme.material.topSide
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.info.impl.presentation.ui.InfoLayoutMode
import ru.aleshin.studyassistant.info.impl.resources.Res
import ru.aleshin.studyassistant.info.impl.resources.new_organization_bottom_title

/**
 * @author Stanislav Aleshin on 16.06.2024.
 */
@Composable
internal fun OrganizationsBottomBar(
    modifier: Modifier = Modifier,
    allOrganizations: List<OrganizationShortUi>?,
    pagerState: PagerState,
    organizationData: OrganizationUi?,
    onChangeOrganization: (OrganizationShortUi?) -> Unit,
    layoutMode: InfoLayoutMode = InfoLayoutMode.COMPACT,
) {
    val isExpanded = layoutMode == InfoLayoutMode.EXPANDED
    val pagerContentPadding = if (isExpanded) {
        PaddingValues(horizontal = AdaptiveLayoutDefaults.SpaceExtraLarge)
    } else {
        PaddingValues(horizontal = 12.dp)
    }
    val pagerSpacing = if (isExpanded) 12.dp else 8.dp

    Surface(
        modifier = modifier.animateContentSize(tween()),
        shape = MaterialTheme.shapes.extraLarge.topSide,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.TopCenter,
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth)
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                contentPadding = pagerContentPadding,
                pageSpacing = pagerSpacing,
            ) { page ->
                if (allOrganizations != null) {
                    val organization = allOrganizations.getOrNull(page)
                    if (organization != null) {
                        OrganizationBottomItem(
                            type = organization.type,
                            name = organization.shortName,
                        )
                    } else {
                        NewOrganizationBottomItem()
                    }
                }
            }
        }
        if (allOrganizations != null) {
            val currentOrganization = allOrganizations.getOrNull(pagerState.currentPage)
            LaunchedEffect(currentOrganization) {
                if (organizationData == null || organizationData.uid != currentOrganization?.uid) {
                    onChangeOrganization(currentOrganization)
                }
            }
        }
    }
}

@Composable
private fun OrganizationBottomItem(
    modifier: Modifier = Modifier,
    type: OrganizationType,
    name: String,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(60.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(56.dp, 36.dp),
                color = MaterialTheme.colorScheme.secondary,
                shape = MaterialTheme.shapes.full,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        painter = painterResource(type.mapToIcon()),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.mapToSting(),
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = name,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun NewOrganizationBottomItem(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(60.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                modifier = Modifier.align(Alignment.Center),
                text = stringResource(Res.string.new_organization_bottom_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}