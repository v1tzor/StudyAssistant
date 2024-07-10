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

package ru.aleshin.studyassistant.core.ui.mappers

import ru.aleshin.studyassistant.core.domain.entities.settings.LanguageType
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
fun LanguageType.mapToUi() = when (this) {
    LanguageType.DEFAULT -> LanguageUiType.DEFAULT
    LanguageType.EN -> LanguageUiType.EN
    LanguageType.RU -> LanguageUiType.RU
}

fun LanguageUiType.mapToDomain() = when (this) {
    LanguageUiType.DEFAULT -> LanguageType.DEFAULT
    LanguageUiType.EN -> LanguageType.EN
    LanguageUiType.RU -> LanguageType.RU
}

fun LanguageUiType.mapToString(strings: StudyAssistantStrings) = when (this) {
    LanguageUiType.DEFAULT -> strings.defaultTitle
    LanguageUiType.EN -> strings.englishLanguageTitle
    LanguageUiType.RU -> strings.russianLanguageTitle
}