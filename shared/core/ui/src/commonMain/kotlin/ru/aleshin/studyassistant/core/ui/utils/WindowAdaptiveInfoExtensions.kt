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

package ru.aleshin.studyassistant.core.ui.utils

import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.window.core.layout.WindowSizeClass

/**
 * @author Stanislav Aleshin on 04.08.2026.
 */
val WindowAdaptiveInfo.isCompactWidth: Boolean
    get() = !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)

val WindowAdaptiveInfo.isMediumWidth: Boolean
    get() = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

val WindowAdaptiveInfo.isExpandedWidth: Boolean
    get() = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND) &&
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND)

val WindowAdaptiveInfo.isLargeWidth: Boolean
    get() = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND) &&
        !windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND)

val WindowAdaptiveInfo.isExtraLargeWidth: Boolean
    get() = windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND)

val WindowAdaptiveInfo.isCompactHeight: Boolean
    get() = !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

val WindowAdaptiveInfo.isMediumHeight: Boolean
    get() = windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND) &&
        !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)

val WindowAdaptiveInfo.isExpandedHeight: Boolean
    get() = windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_EXPANDED_LOWER_BOUND)

val WindowAdaptiveInfo.isBookPosture: Boolean
    get() = windowPosture.hingeList.any { hinge ->
        hinge.isVertical && (hinge.isSeparating || hinge.isOccluding)
    }

val WindowAdaptiveInfo.isTabletopPosture: Boolean
    get() = windowPosture.isTabletop

val WindowAdaptiveInfo.useNavigationRail: Boolean
    get() = !isCompactWidth && !isCompactHeight && !isTabletopPosture

val WindowAdaptiveInfo.useExpandedLayout: Boolean
    get() = (isExpandedWidth || isLargeWidth || isExtraLargeWidth) &&
        !isCompactHeight &&
        !isTabletopPosture

val WindowAdaptiveInfo.useLargeLayout: Boolean
    get() = (isLargeWidth || isExtraLargeWidth) &&
        !isCompactHeight &&
        !isTabletopPosture
