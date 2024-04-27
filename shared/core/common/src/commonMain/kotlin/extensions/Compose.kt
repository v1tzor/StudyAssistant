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
package extensions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
@Composable
fun Modifier.alphaByEnabled(enabled: Boolean, disabledAlpha: Float = 0.6f) = alpha(
    alpha = if (enabled) 1f else disabledAlpha,
)

fun LazyGridScope.emptyItem(modifier: Modifier = Modifier) {
    item { Spacer(modifier = modifier.fillMaxWidth()) }
}

@ExperimentalFoundationApi
fun PagerState.pageProgress(): Float {
    return currentPage.toFloat() / pageCount
}