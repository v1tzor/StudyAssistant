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

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.preferredLanguages
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
internal actual fun applyApplicationLanguage(
    languageType: LanguageUiType,
): StudyAssistantLanguage {
    val userDefaults = NSUserDefaults.standardUserDefaults
    when (languageType) {
        LanguageUiType.DEFAULT -> userDefaults.removeObjectForKey(APPLE_LANGUAGES_KEY)
        LanguageUiType.EN -> userDefaults.setObject(listOf(LanguageUiType.EN.code), APPLE_LANGUAGES_KEY)
        LanguageUiType.RU -> userDefaults.setObject(listOf(LanguageUiType.RU.code), APPLE_LANGUAGES_KEY)
        LanguageUiType.ZH -> userDefaults.setObject(listOf(LanguageUiType.ZH.code), APPLE_LANGUAGES_KEY)
        LanguageUiType.BE -> userDefaults.setObject(listOf(LanguageUiType.BE.code), APPLE_LANGUAGES_KEY)
        LanguageUiType.KK -> userDefaults.setObject(listOf(LanguageUiType.KK.code), APPLE_LANGUAGES_KEY)
    }
    userDefaults.synchronize()

    val language = languageType.code ?: NSLocale.preferredLanguages.firstOrNull() as? String
    return fetchAppLanguage(language.orEmpty())
}

private const val APPLE_LANGUAGES_KEY = "AppleLanguages"
