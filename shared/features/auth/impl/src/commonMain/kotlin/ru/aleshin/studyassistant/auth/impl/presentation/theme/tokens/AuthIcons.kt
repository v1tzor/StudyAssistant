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

package ru.aleshin.studyassistant.auth.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import studyassistant.shared.features.auth.impl.generated.resources.Res
import studyassistant.shared.features.auth.impl.generated.resources.il_login
import studyassistant.shared.features.auth.impl.generated.resources.il_login_dark

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
@OptIn(ExperimentalResourceApi::class)
internal data class AuthIcons(
    val loginIllustration: DrawableResource,
) {
    companion object {
        val LIGHT = AuthIcons(
            loginIllustration = Res.drawable.il_login,
        )
        val DARK = AuthIcons(
            loginIllustration = Res.drawable.il_login_dark,
        )
    }
}

internal val LocalAuthIcons = staticCompositionLocalOf<AuthIcons> {
    error("Auth Icons is not provided")
}

internal fun fetchAuthIcons(isDark: Boolean) = when (isDark) {
    true -> AuthIcons.DARK
    false -> AuthIcons.LIGHT
}
