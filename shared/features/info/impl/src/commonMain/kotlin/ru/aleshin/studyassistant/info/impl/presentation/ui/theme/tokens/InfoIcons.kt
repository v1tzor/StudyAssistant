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

package ru.aleshin.studyassistant.info.impl.presentation.ui.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import studyassistant.shared.features.info.impl.generated.resources.Res
import studyassistant.shared.features.info.impl.generated.resources.ic_star_circular

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Immutable
internal data class InfoIcons(
    val mainOrganization: DrawableResource,
) {
    companion object {
        val LIGHT = InfoIcons(
            mainOrganization = Res.drawable.ic_star_circular,
        )
        val DARK = InfoIcons(
            mainOrganization = Res.drawable.ic_star_circular,
        )
    }
}

internal val LocalInfoIcons = staticCompositionLocalOf<InfoIcons> {
    error("Info Icons is not provided")
}

internal fun fetchInfoIcons(isDark: Boolean) = when (isDark) {
    true -> InfoIcons.DARK
    false -> InfoIcons.LIGHT
}