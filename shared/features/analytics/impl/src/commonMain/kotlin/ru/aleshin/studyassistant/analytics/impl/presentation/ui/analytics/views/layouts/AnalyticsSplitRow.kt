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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AnalyticsSplitRow(
    modifier: Modifier = Modifier,
    leadingWeight: Float,
    trailingWeight: Float,
    spacing: Dp,
    leading: @Composable ColumnScope.() -> Unit,
    trailing: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(spacing),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .weight(leadingWeight)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = leading,
        )
        Column(
            modifier = Modifier
                .weight(trailingWeight)
                .fillMaxHeight(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            content = trailing,
        )
    }
}

internal fun analyticsBalancedSplitWeights(): Pair<Float, Float> = 1f to 1f

internal fun analyticsWideSplitWeights(isBookPosture: Boolean): Pair<Float, Float> {
    return if (isBookPosture) analyticsBalancedSplitWeights() else 3f to 2f
}

internal fun analyticsSplitSpacing(isBookPosture: Boolean): Dp {
    return if (isBookPosture) 32.dp else 24.dp
}
