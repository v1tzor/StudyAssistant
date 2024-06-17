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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import entities.organizations.OrganizationType
import functional.UID
import mappers.mapToIcon
import mappers.mapToSting
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes
import theme.StudyAssistantRes
import theme.material.full

/**
 * @author Stanislav Aleshin on 16.06.2024.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun OrganizationsBottomBar(
    modifier: Modifier = Modifier,
    allOrganizations: List<OrganizationShortUi>?,
    pagerState: PagerState,
    selectedOrganization: UID?,
    onChangeOrganization: (OrganizationShortUi?) -> Unit,
) {
    Surface(
        modifier = modifier.animateContentSize(tween()),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            pageSpacing = 8.dp,
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
        if (allOrganizations != null) {
            val currentPage by derivedStateOf { pagerState.currentPage }
            LaunchedEffect(currentPage) {
                val visibleOrganization = allOrganizations.getOrNull(currentPage)
                if (selectedOrganization != visibleOrganization?.uid) {
                    onChangeOrganization(visibleOrganization)
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
                shape = MaterialTheme.shapes.full(),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Icon(
                        modifier = Modifier.align(Alignment.Center),
                        painter = painterResource(type.mapToIcon(StudyAssistantRes.icons)),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = type.mapToSting(StudyAssistantRes.strings),
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
                text = InfoThemeRes.strings.newOrganizationBottomTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )
        }
    }
}