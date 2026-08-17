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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.layouts

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsOverviewUi

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AnalyticsCompactLayout(
    modifier: Modifier = Modifier,
    data: AnalyticsOverviewUi,
    isDetails: Boolean,
    onTargetClick: (AnalyticsTarget) -> Unit,
) {
    AnalyticsItemsGrid(
        modifier = modifier,
        columns = 1,
        maxContentWidth = ANALYTICS_CONTENT_MAX_WIDTH,
        horizontalSpacing = 16.dp,
    ) {
        if (isDetails) {
            analyticsDetailsItems(
                data = data,
                onTargetClick = onTargetClick,
            )
        } else {
            analyticsOverviewItems(
                data = data,
                onTargetClick = onTargetClick,
            )
        }
    }
}

@Composable
internal fun AnalyticsCompactLoadingLayout(
    modifier: Modifier = Modifier,
) {
    AnalyticsLoadingLayout(
        modifier = modifier,
        maxContentWidth = ANALYTICS_COMPACT_LOADING_WIDTH,
    )
}
