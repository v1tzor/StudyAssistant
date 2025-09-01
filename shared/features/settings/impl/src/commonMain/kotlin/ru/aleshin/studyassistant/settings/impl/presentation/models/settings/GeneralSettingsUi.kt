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

package ru.aleshin.studyassistant.settings.impl.presentation.models.settings

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.models.ThemeUiType
import ru.aleshin.studyassistant.core.ui.theme.tokens.LanguageUiType

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
@Immutable
@Serializable
internal data class GeneralSettingsUi(
    val isFirstStart: Boolean = true,
    val isUnfinishedSetup: UID? = null,
    val languageType: LanguageUiType = LanguageUiType.DEFAULT,
    val themeType: ThemeUiType = ThemeUiType.DEFAULT,
)