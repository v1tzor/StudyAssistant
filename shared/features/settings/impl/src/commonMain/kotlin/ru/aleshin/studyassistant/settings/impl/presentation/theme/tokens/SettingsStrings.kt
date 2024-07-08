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
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
internal data class SettingsStrings(
    val settingsHeader: String,
    val generalTabHeader: String,
    val notificationsTabHeader: String,
    val calendarTabHeader: String,
    val subscriptionTabHeader: String,
    val otherErrorMessage: String,
) {
    companion object {
        val RUSSIAN = SettingsStrings(
            settingsHeader = "Настройки",
            generalTabHeader = "Общие",
            notificationsTabHeader = "Уведомления",
            calendarTabHeader = "Каленарь",
            subscriptionTabHeader = "Подписка и данные",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = SettingsStrings(
            settingsHeader = "Settings",
            generalTabHeader = "General",
            notificationsTabHeader = "Notification",
            calendarTabHeader = "Calendar",
            subscriptionTabHeader = "Subscription and data",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalSettingsStrings = staticCompositionLocalOf<SettingsStrings> {
    error("Settings Strings is not provided")
}

internal fun fetchSettingsStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> SettingsStrings.ENGLISH
    StudyAssistantLanguage.RU -> SettingsStrings.RUSSIAN
}