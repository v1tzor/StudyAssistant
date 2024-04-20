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
import studyassistant.shared.features.auth.impl.generated.resources.ic_google
import studyassistant.shared.features.auth.impl.generated.resources.ic_mail_outline
import studyassistant.shared.features.auth.impl.generated.resources.ic_password_outline
import studyassistant.shared.features.auth.impl.generated.resources.ic_person_outline
import studyassistant.shared.features.auth.impl.generated.resources.ic_visibility
import studyassistant.shared.features.auth.impl.generated.resources.ic_visibility_off
import studyassistant.shared.features.auth.impl.generated.resources.il_forgot
import studyassistant.shared.features.auth.impl.generated.resources.il_forgot_dark
import studyassistant.shared.features.auth.impl.generated.resources.il_login
import studyassistant.shared.features.auth.impl.generated.resources.il_login_dark
import studyassistant.shared.features.auth.impl.generated.resources.il_register
import studyassistant.shared.features.auth.impl.generated.resources.il_register_dark

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
@OptIn(ExperimentalResourceApi::class)
internal data class AuthIcons(
    val loginIllustration: DrawableResource,
    val registerIllustration: DrawableResource,
    val forgotIllustration: DrawableResource,
    val email: DrawableResource,
    val password: DrawableResource,
    val username: DrawableResource,
    val visibility: DrawableResource,
    val visibilityOff: DrawableResource,
    val google: DrawableResource,
) {

    companion object {
        val LIGHT = AuthIcons(
            loginIllustration = Res.drawable.il_login,
            registerIllustration = Res.drawable.il_register,
            forgotIllustration = Res.drawable.il_forgot,
            email = Res.drawable.ic_mail_outline,
            password = Res.drawable.ic_password_outline,
            username = Res.drawable.ic_person_outline,
            visibility = Res.drawable.ic_visibility,
            visibilityOff = Res.drawable.ic_visibility_off,
            google = Res.drawable.ic_google,
        )
        val DARK = AuthIcons(
            loginIllustration = Res.drawable.il_login_dark,
            registerIllustration = Res.drawable.il_register_dark,
            forgotIllustration = Res.drawable.il_forgot_dark,
            email = Res.drawable.ic_mail_outline,
            password = Res.drawable.ic_password_outline,
            username = Res.drawable.ic_person_outline,
            visibility = Res.drawable.ic_visibility,
            visibilityOff = Res.drawable.ic_visibility_off,
            google = Res.drawable.ic_google,
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
