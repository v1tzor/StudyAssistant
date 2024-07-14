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

package ru.aleshin.studyassistant.core.ui.views

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.TabPosition
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.lerp
import kotlin.math.absoluteValue

/**
 * @author Stanislav Aleshin on 13.07.2024.
 */
fun Modifier.pagerTabIndicatorOffset(
    pagerState: PagerState,
    tabPositions: List<TabPosition>,
): Modifier = composed {
    val targetIndicatorOffset: Dp
    val indicatorWidth: Dp

    val currentTab = tabPositions[pagerState.settledPage]
    var isRightMove by remember { mutableStateOf(false) }
    val targetPage = if (pagerState.currentPageOffsetFraction != 0f) {
        val initValue = remember { pagerState.currentPageOffsetFraction }
        if (initValue >= 0) {
            isRightMove = true
            pagerState.settledPage + 1
        } else {
            isRightMove = false
            pagerState.settledPage - 1
        }
    } else {
        pagerState.currentPage
    }
    val targetTab = tabPositions.getOrNull(targetPage)
    if (targetTab != null) {
        val pageFraction = pagerState.currentPageOffsetFraction
        val fraction = when (pageFraction > 0f) {
            true -> pageFraction
            false -> if (pageFraction == 0f && !isRightMove) 0f else 1 - pageFraction.absoluteValue
        }
        targetIndicatorOffset = when (isRightMove) {
            true -> lerp(currentTab.left, targetTab.left, fraction)
            false -> lerp(targetTab.left, currentTab.left, fraction)
        }
        indicatorWidth = lerp(currentTab.width, targetTab.width, fraction)
    } else {
        targetIndicatorOffset = currentTab.left
        indicatorWidth = currentTab.width
    }

    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = targetIndicatorOffset)
        .width(indicatorWidth)
}