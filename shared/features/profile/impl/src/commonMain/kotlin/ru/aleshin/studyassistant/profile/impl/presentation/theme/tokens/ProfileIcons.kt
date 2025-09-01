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

package ru.aleshin.studyassistant.profile.impl.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import studyassistant.shared.features.profile.impl.generated.resources.Res
import studyassistant.shared.features.profile.impl.generated.resources.ic_calendar
import studyassistant.shared.features.profile.impl.generated.resources.ic_edit
import studyassistant.shared.features.profile.impl.generated.resources.ic_email_alert
import studyassistant.shared.features.profile.impl.generated.resources.ic_friends
import studyassistant.shared.features.profile.impl.generated.resources.ic_notifications
import studyassistant.shared.features.profile.impl.generated.resources.ic_payment
import studyassistant.shared.features.profile.impl.generated.resources.ic_send_circle
import studyassistant.shared.features.profile.impl.generated.resources.ic_settings_common
import studyassistant.shared.features.profile.impl.generated.resources.ic_settings_privacy
import studyassistant.shared.features.profile.impl.generated.resources.ic_sign_out
import studyassistant.shared.features.profile.impl.generated.resources.ic_table

/**
 * @author Stanislav Aleshin on 21.06.2023.
 */
@Immutable
internal data class ProfileIcons(
    val edit: DrawableResource,
    val signOut: DrawableResource,
    val emailAlert: DrawableResource,
    val sendCircular: DrawableResource,
    val friends: DrawableResource,
    val privacySettings: DrawableResource,
    val generalSettings: DrawableResource,
    val notifySettings: DrawableResource,
    val calendarSettings: DrawableResource,
    val paymentsSettings: DrawableResource,
    val shareSchedule: DrawableResource,
) {
    companion object {
        val LIGHT = ProfileIcons(
            edit = Res.drawable.ic_edit,
            signOut = Res.drawable.ic_sign_out,
            emailAlert = Res.drawable.ic_email_alert,
            sendCircular = Res.drawable.ic_send_circle,
            friends = Res.drawable.ic_friends,
            privacySettings = Res.drawable.ic_settings_privacy,
            generalSettings = Res.drawable.ic_settings_common,
            notifySettings = Res.drawable.ic_notifications,
            calendarSettings = Res.drawable.ic_calendar,
            paymentsSettings = Res.drawable.ic_payment,
            shareSchedule = Res.drawable.ic_table,
        )
        val DARK = ProfileIcons(
            edit = Res.drawable.ic_edit,
            signOut = Res.drawable.ic_sign_out,
            emailAlert = Res.drawable.ic_email_alert,
            sendCircular = Res.drawable.ic_send_circle,
            friends = Res.drawable.ic_friends,
            privacySettings = Res.drawable.ic_settings_privacy,
            generalSettings = Res.drawable.ic_settings_common,
            notifySettings = Res.drawable.ic_notifications,
            calendarSettings = Res.drawable.ic_calendar,
            paymentsSettings = Res.drawable.ic_payment,
            shareSchedule = Res.drawable.ic_table,
        )
    }
}

internal val LocalProfileIcons = staticCompositionLocalOf<ProfileIcons> {
    error("Profile Icons is not provided")
}

internal fun fetchProfileIcons(isDark: Boolean) = when (isDark) {
    true -> ProfileIcons.DARK
    false -> ProfileIcons.LIGHT
}