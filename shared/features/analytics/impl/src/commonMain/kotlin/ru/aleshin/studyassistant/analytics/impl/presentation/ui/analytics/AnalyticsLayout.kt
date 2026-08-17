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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsOverviewUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsRenderState
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsErrorView
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.layouts.AnalyticsCompactLayout
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.layouts.AnalyticsCompactLoadingLayout
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.layouts.AnalyticsEmptyLayout
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.layouts.AnalyticsExpandedLayout
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.layouts.AnalyticsExpandedLoadingLayout
import ru.aleshin.studyassistant.core.ui.utils.isBookPosture
import ru.aleshin.studyassistant.core.ui.utils.isCompactHeight
import ru.aleshin.studyassistant.core.ui.utils.isCompactWidth
import ru.aleshin.studyassistant.core.ui.utils.isTabletopPosture
import ru.aleshin.studyassistant.core.ui.utils.useExpandedLayout

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AnalyticsLayout(
    modifier: Modifier = Modifier,
    renderState: AnalyticsRenderState,
    data: AnalyticsOverviewUi?,
    isDetails: Boolean,
    adaptiveInfo: WindowAdaptiveInfo,
    onRetry: () -> Unit,
    onTargetClick: (AnalyticsTarget) -> Unit,
) {
    val useExpandedLayout = adaptiveInfo.useAnalyticsExpandedLayout()

    when (renderState) {
        AnalyticsRenderState.LOADING -> if (useExpandedLayout) {
            AnalyticsExpandedLoadingLayout(modifier = modifier)
        } else {
            AnalyticsCompactLoadingLayout(modifier = modifier)
        }
        AnalyticsRenderState.ERROR -> AnalyticsErrorView(
            modifier = modifier.padding(16.dp),
            onRetry = onRetry,
        )
        AnalyticsRenderState.EMPTY -> AnalyticsEmptyLayout(
            modifier = modifier.padding(16.dp),
        )
        AnalyticsRenderState.CONTENT -> data?.let { overview ->
            if (useExpandedLayout) {
                AnalyticsExpandedLayout(
                    modifier = modifier,
                    data = overview,
                    isDetails = isDetails,
                    isBookPosture = adaptiveInfo.isBookPosture,
                    onTargetClick = onTargetClick,
                )
            } else {
                AnalyticsCompactLayout(
                    modifier = modifier,
                    data = overview,
                    isDetails = isDetails,
                    onTargetClick = onTargetClick,
                )
            }
        }
    }
}

internal fun WindowAdaptiveInfo.useAnalyticsExpandedLayout(): Boolean {
    return !isCompactHeight &&
        !isTabletopPosture &&
        (useExpandedLayout || isBookPosture || !isCompactWidth)
}
