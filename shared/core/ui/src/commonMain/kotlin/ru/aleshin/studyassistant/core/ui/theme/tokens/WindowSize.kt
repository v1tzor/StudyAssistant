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

package ru.aleshin.studyassistant.core.ui.theme.tokens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
data class WindowSize(
    val widthWindowType: WindowType,
    val heightWindowType: WindowType,
    val widthWindowDpSize: Dp,
    val heightWindowDpSize: Dp
) {
    sealed class WindowType {
        data object COMPACT : WindowType()
        data object MEDIUM : WindowType()
        data object EXPANDED : WindowType()
    }

    companion object {
        fun specifySize(height: Dp, width: Dp) = WindowSize(
            widthWindowType = when {
                width < 0.dp -> throw IllegalArgumentException("Dp value cannot be negative")
                width < 600.dp -> WindowType.COMPACT
                width < 840.dp -> WindowType.MEDIUM
                else -> WindowType.EXPANDED
            },
            heightWindowType = when {
                height < 0.dp -> throw IllegalArgumentException("Dp value cannot be negative")
                height < 680.dp -> WindowType.COMPACT
                height < 900.dp -> WindowType.MEDIUM
                else -> WindowType.EXPANDED
            },
            widthWindowDpSize = width,
            heightWindowDpSize = height
        )
    }
}

@Composable
expect fun rememberScreenSizeInfo(): WindowSize

val LocalWindowSize = staticCompositionLocalOf<WindowSize> {
    error("CompositionLocal WindowSizeClass not present")
}