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

import androidx.compose.runtime.staticCompositionLocalOf
import theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
internal data class ProfileStrings(
    val profileHeader: String,
    val editProfileDesc: String,
    val signOutDesc: String,
    val emailIsNotConfirmed: String,
    val friendsTitle: String,
    val privacySettingsTitle: String,
    val generalSettingsTitle: String,
    val notifySettingsTitle: String,
    val calendarSettingsTitle: String,
    val paymentsSettingsTitle: String,
    val otherErrorMessage: String,
) {

    companion object {
        val RUSSIAN = ProfileStrings(
            profileHeader = "Профиль",
            editProfileDesc = "Редактировать профиль",
            signOutDesc = "Выйти из аккаунта",
            emailIsNotConfirmed = "Email не подтверждён",
            friendsTitle = "Друзья",
            privacySettingsTitle = "Приватность\nаккаунта",
            generalSettingsTitle = "Общие\nнастройки",
            notifySettingsTitle = "Уведомления и\nоповещения",
            calendarSettingsTitle = "Конфигурация\nкалендаря",
            paymentsSettingsTitle = "Подписка и \nплатные услуги",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = ProfileStrings(
            profileHeader = "Profile",
            editProfileDesc = "Edit profile",
            signOutDesc = "Sign out",
            emailIsNotConfirmed = "Email not confirmed",
            friendsTitle = "Friends",
            privacySettingsTitle = "Account\nprivacy",
            generalSettingsTitle = "General\nsettings",
            notifySettingsTitle = "Notifications and\nalerts",
            calendarSettingsTitle = "Calendar\nconfiguration",
            paymentsSettingsTitle = "Subscription and\npaid services",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalProfileStrings = staticCompositionLocalOf<ProfileStrings> {
    error("Profile Strings is not provided")
}

internal fun fetchProfileStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> ProfileStrings.ENGLISH
    StudyAssistantLanguage.RU -> ProfileStrings.RUSSIAN
}