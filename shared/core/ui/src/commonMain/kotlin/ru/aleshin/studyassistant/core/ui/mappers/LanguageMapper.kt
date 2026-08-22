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

package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.settings.LanguageType
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.belarusian_language_title as core_belarusian_language_title
import ru.aleshin.studyassistant.core.ui.resources.chinese_language_title as core_chinese_language_title
import ru.aleshin.studyassistant.core.ui.resources.default_title as core_default_title
import ru.aleshin.studyassistant.core.ui.resources.english_language_title as core_english_language_title
import ru.aleshin.studyassistant.core.ui.resources.kazakh_language_title as core_kazakh_language_title
import ru.aleshin.studyassistant.core.ui.resources.russian_language_title as core_russian_language_title

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
fun LanguageType.mapToUi() = when (this) {
    LanguageType.DEFAULT -> LanguageUiType.DEFAULT
    LanguageType.EN -> LanguageUiType.EN
    LanguageType.RU -> LanguageUiType.RU
    LanguageType.ZH -> LanguageUiType.ZH
    LanguageType.BE -> LanguageUiType.BE
    LanguageType.KK -> LanguageUiType.KK
}

fun LanguageUiType.mapToDomain() = when (this) {
    LanguageUiType.DEFAULT -> LanguageType.DEFAULT
    LanguageUiType.EN -> LanguageType.EN
    LanguageUiType.RU -> LanguageType.RU
    LanguageUiType.ZH -> LanguageType.ZH
    LanguageUiType.BE -> LanguageType.BE
    LanguageUiType.KK -> LanguageType.KK
}

@Composable
fun LanguageUiType.mapToString() = when (this) {
    LanguageUiType.DEFAULT -> stringResource(CoreRes.string.core_default_title)
    LanguageUiType.EN -> stringResource(CoreRes.string.core_english_language_title)
    LanguageUiType.RU -> stringResource(CoreRes.string.core_russian_language_title)
    LanguageUiType.ZH -> stringResource(CoreRes.string.core_chinese_language_title)
    LanguageUiType.BE -> stringResource(CoreRes.string.core_belarusian_language_title)
    LanguageUiType.KK -> stringResource(CoreRes.string.core_kazakh_language_title)
}