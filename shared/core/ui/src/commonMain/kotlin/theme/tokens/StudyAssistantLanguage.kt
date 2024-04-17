/*
 * Copyright 2023 Stanislav Aleshin
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
package theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import platform.JavaSerializable

/**
 * @author Stanislav Aleshin on 27.01.2024.
 */
enum class StudyAssistantLanguage(val code: String) {
    EN("en"),
    RU("ru")
}

enum class LanguageUiType(val code: String?) : JavaSerializable {
    DEFAULT(null),
    EN("en"),
    RU("ru")
}

val LocalStudyAssistantLanguage = staticCompositionLocalOf<StudyAssistantLanguage> {
    error("Language is not provided")
}

fun fetchAppLanguage(language: String) = when (language) {
    "ru" -> StudyAssistantLanguage.RU
    "en" -> StudyAssistantLanguage.EN
    else -> StudyAssistantLanguage.EN
}

fun fetchAppLanguage(languageType: LanguageUiType) = when (languageType) {
    LanguageUiType.DEFAULT -> fetchAppLanguage(Locale.current.language)
    LanguageUiType.EN -> StudyAssistantLanguage.EN
    LanguageUiType.RU -> StudyAssistantLanguage.RU
}