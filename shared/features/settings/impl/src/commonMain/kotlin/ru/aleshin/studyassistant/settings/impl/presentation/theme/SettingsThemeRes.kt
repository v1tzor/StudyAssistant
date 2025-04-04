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

package ru.aleshin.studyassistant.settings.impl.presentation.theme

import androidx.compose.runtime.Composable
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.LocalSettingsIcons
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.LocalSettingsStrings
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.SettingsIcons
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.SettingsStrings

/**
 * @author Stanislav Aleshin on 14.06.2023
 */
internal object SettingsThemeRes {

    val icons: SettingsIcons
        @Composable get() = LocalSettingsIcons.current

    val strings: SettingsStrings
        @Composable get() = LocalSettingsStrings.current
}