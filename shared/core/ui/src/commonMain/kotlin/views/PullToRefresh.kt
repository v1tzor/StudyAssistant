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

package views

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import functional.Constants.Delay
import kotlinx.coroutines.delay

/**
 * @author Stanislav Aleshin on 15.06.2024.
 */
@Composable
@ExperimentalMaterial3Api
fun PullToRefreshContainer(
    state: PullToRefreshState,
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    onRefresh: () -> Unit,
    indicator: @Composable (PullToRefreshState) -> Unit = { pullRefreshState ->
        PullToRefreshDefaults.Indicator(state = pullRefreshState)
    },
    shape: Shape = PullToRefreshDefaults.shape,
    containerColor: Color = PullToRefreshDefaults.containerColor,
    contentColor: Color = PullToRefreshDefaults.contentColor,
) {
    PullToRefreshContainer(
        state = state,
        modifier = modifier.size(38.dp),
        indicator = indicator,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
    )

    if (state.isRefreshing) {
        LaunchedEffect(true) { if (!isLoading) onRefresh() }
    }

    LaunchedEffect(isLoading) {
        if (!state.isRefreshing && isLoading) {
            state.startRefresh()
        } else if (state.isRefreshing && !isLoading) {
            delay(Delay.PULL_REFRESH)
            state.endRefresh()
        }
    }
}