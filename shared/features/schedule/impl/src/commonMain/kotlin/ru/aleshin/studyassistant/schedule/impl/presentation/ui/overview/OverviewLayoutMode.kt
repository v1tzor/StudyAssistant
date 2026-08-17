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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.utils.isBookPosture
import ru.aleshin.studyassistant.core.ui.utils.isCompactHeight
import ru.aleshin.studyassistant.core.ui.utils.isCompactWidth
import ru.aleshin.studyassistant.core.ui.utils.isMediumWidth
import ru.aleshin.studyassistant.core.ui.utils.isTabletopPosture
import ru.aleshin.studyassistant.core.ui.utils.useExpandedLayout

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Immutable
internal enum class OverviewLayoutMode {
    COMPACT,
    MEDIUM,
    SUPPORTING_PANE,
    BOOK;

    val showScreenTopBar: Boolean
        get() = this == COMPACT || this == MEDIUM

    val showDateBottomBar: Boolean
        get() = this == COMPACT || this == MEDIUM
}

internal fun OverviewLayoutMode.supportingPaneWidth(useLargeLayout: Boolean): Dp {
    return when (this) {
        OverviewLayoutMode.BOOK -> AdaptiveLayoutDefaults.SupportingPanePreferredWidth
        OverviewLayoutMode.SUPPORTING_PANE -> if (useLargeLayout) {
            OVERVIEW_LARGE_SUPPORTING_PANE_WIDTH
        } else {
            AdaptiveLayoutDefaults.SupportingPanePreferredWidth
        }
        OverviewLayoutMode.COMPACT,
        OverviewLayoutMode.MEDIUM -> AdaptiveLayoutDefaults.SupportingPanePreferredWidth
    }
}

private val OVERVIEW_LARGE_SUPPORTING_PANE_WIDTH = 420.dp

internal fun WindowAdaptiveInfo.fetchOverviewLayoutMode(): OverviewLayoutMode {
    return when {
        isBookPosture && !isCompactHeight && !isTabletopPosture -> OverviewLayoutMode.BOOK
        isCompactWidth || isTabletopPosture -> OverviewLayoutMode.COMPACT
        isCompactHeight || isMediumWidth -> OverviewLayoutMode.MEDIUM
        useExpandedLayout -> OverviewLayoutMode.SUPPORTING_PANE
        else -> OverviewLayoutMode.MEDIUM
    }
}
