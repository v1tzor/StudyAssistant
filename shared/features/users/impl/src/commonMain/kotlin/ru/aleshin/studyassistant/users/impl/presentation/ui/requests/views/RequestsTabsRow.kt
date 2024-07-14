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

package ru.aleshin.studyassistant.users.impl.presentation.ui.requests.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.TabRowDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.MediumInfoBadge
import ru.aleshin.studyassistant.core.ui.views.pagerTabIndicatorOffset
import ru.aleshin.studyassistant.users.impl.presentation.models.FriendRequestsDetailsUi
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.friends.views.RequestsTab

/**
 * @author Stanislav Aleshin on 13.07.2024.
 */
@Composable
internal fun RequestsTabsRow(
    modifier: Modifier = Modifier,
    pagerState: PagerState,
    selectedTab: RequestsTab,
    requests: FriendRequestsDetailsUi?,
    onChangeTab: (RequestsTab) -> Unit,
) {
    TabRow(
        modifier = modifier.fillMaxWidth(),
        selectedTabIndex = selectedTab.index,
        containerColor = MaterialTheme.colorScheme.background,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                modifier = Modifier.pagerTabIndicatorOffset(pagerState, tabPositions),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    ) {
        RequestsTab.entries.forEach { tab ->
            Tab(
                selected = tab == selectedTab,
                onClick = { onChangeTab(tab) },
                modifier = Modifier.height(48.dp),
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    when (tab) {
                        RequestsTab.RECEIVED -> {
                            Text(
                                text = UsersThemeRes.strings.receivedRequestsTabTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            MediumInfoBadge(
                                containerColor = StudyAssistantRes.colors.accents.orangeContainer,
                                contentColor = StudyAssistantRes.colors.accents.orange,
                            ) {
                                Text(text = (requests?.received?.size ?: 0).toString())
                            }
                        }
                        RequestsTab.SENT -> {
                            Text(
                                text = UsersThemeRes.strings.sentRequestsTabTitle,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            MediumInfoBadge(
                                containerColor = StudyAssistantRes.colors.accents.yellowContainer,
                                contentColor = StudyAssistantRes.colors.accents.yellow,
                            ) {
                                Text(text = (requests?.send?.size ?: 0).toString())
                            }
                        }
                    }
                }
            }
        }
    }
}