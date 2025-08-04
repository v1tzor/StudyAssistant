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

package ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import studyassistant.shared.features.settings.impl.generated.resources.Res
import studyassistant.shared.features.settings.impl.generated.resources.ic_calendar_week
import studyassistant.shared.features.settings.impl.generated.resources.ic_github
import studyassistant.shared.features.settings.impl.generated.resources.ic_language
import studyassistant.shared.features.settings.impl.generated.resources.ic_palette

/**
 * @author Stanislav Aleshin on 21.06.2023.
 */
internal data class SettingsIcons(
    val theme: DrawableResource,
    val language: DrawableResource,
    val numberOfWeek: DrawableResource,
    val git: DrawableResource,
) {
    companion object {
        val LIGHT = SettingsIcons(
            theme = Res.drawable.ic_palette,
            language = Res.drawable.ic_language,
            numberOfWeek = Res.drawable.ic_calendar_week,
            git = Res.drawable.ic_github,
        )
        val DARK = SettingsIcons(
            theme = Res.drawable.ic_palette,
            language = Res.drawable.ic_language,
            numberOfWeek = Res.drawable.ic_calendar_week,
            git = Res.drawable.ic_github,
        )
    }
}

internal val LocalSettingsIcons = staticCompositionLocalOf<SettingsIcons> {
    error("Settings Icons is not provided")
}

internal fun fetchSettingsIcons(isDark: Boolean) = when (isDark) {
    true -> SettingsIcons.DARK
    false -> SettingsIcons.LIGHT
}