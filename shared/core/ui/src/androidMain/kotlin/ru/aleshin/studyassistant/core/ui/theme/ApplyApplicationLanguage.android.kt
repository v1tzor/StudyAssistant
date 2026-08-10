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

import android.content.res.Resources
import android.os.LocaleList
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage
import ru.aleshin.studyassistant.core.ui.theme.tokens.fetchAppLanguage

/**
 * @author Stanislav Aleshin on 05.08.2026.
 */
internal actual fun applyApplicationLanguage(
    languageType: LanguageUiType,
): StudyAssistantLanguage {
    val locales = when (languageType) {
        LanguageUiType.DEFAULT -> Resources.getSystem().configuration.locales
        LanguageUiType.EN -> LocaleList.forLanguageTags(LanguageUiType.EN.code)
        LanguageUiType.RU -> LocaleList.forLanguageTags(LanguageUiType.RU.code)
    }
    LocaleList.setDefault(locales)
    return fetchAppLanguage(locales[0].language)
}
