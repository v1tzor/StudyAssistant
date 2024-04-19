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

package theme.tokens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo

/**
 * @author Stanislav Aleshin on 19.04.2024.
 */
@Composable
@OptIn(ExperimentalComposeUiApi::class)
actual fun rememberScreenSizeInfo(): WindowSize {
    val density = LocalDensity.current
    val config = LocalWindowInfo.current.containerSize

    return remember {
        WindowSize.specifySize(
            height = with(density) { config.height.toDp() },
            width = with(density) { config.width.toDp() },
        )
    }
}