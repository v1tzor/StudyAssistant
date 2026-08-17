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
package ru.aleshin.studyassistant.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.InternalComposeUiApi
import androidx.compose.ui.LocalSystemTheme
import androidx.compose.ui.SystemTheme

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
@Composable
@OptIn(InternalComposeUiApi::class)
internal actual fun ProvideApplicationResourceTheme(
    isDark: Boolean,
    content: @Composable () -> Unit,
) {
    val theme = if (isDark) SystemTheme.Dark else SystemTheme.Light
    CompositionLocalProvider(LocalSystemTheme provides theme) {
        key(isDark) {
            content()
        }
    }
}
