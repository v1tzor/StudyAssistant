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
package theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import models.ThemeUiType
import theme.material.baseShapes
import theme.material.baseTypography
import theme.material.isDarkTheme
import theme.material.toColorScheme
import theme.tokens.StudyAssistantColors
import theme.tokens.LanguageUiType
import theme.tokens.LocalStudyAssistantColors
import theme.tokens.LocalStudyAssistantElevations
import theme.tokens.LocalStudyAssistantIcons
import theme.tokens.LocalStudyAssistantLanguage
import theme.tokens.LocalStudyAssistantStrings
import theme.tokens.LocalWindowSize
import theme.tokens.fetchAppElevations
import theme.tokens.fetchAppLanguage
import theme.tokens.fetchCoreIcons
import theme.tokens.fetchCoreStrings
import theme.tokens.rememberScreenSizeInfo
import views.NavigationBarColor

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
@Composable
fun StudyAssistantTheme(
    themeType: ThemeUiType = ThemeUiType.DEFAULT,
    languageType: LanguageUiType = LanguageUiType.DEFAULT,
    content: @Composable () -> Unit,
) {
    val windowSize = rememberScreenSizeInfo()
    val colorsType = StudyAssistantColors(themeType.isDarkTheme())
    val appLanguage = fetchAppLanguage(languageType)
    val coreStrings = fetchCoreStrings(appLanguage)
    val appElevations = fetchAppElevations()
    val appIcons = fetchCoreIcons()

    MaterialTheme(
        colorScheme = themeType.toColorScheme(),
        shapes = baseShapes,
        typography = baseTypography,
    ) {
        CompositionLocalProvider(
            LocalStudyAssistantLanguage provides appLanguage,
            LocalStudyAssistantColors provides colorsType,
            LocalStudyAssistantElevations provides appElevations,
            LocalStudyAssistantStrings provides coreStrings,
            LocalStudyAssistantIcons provides appIcons,
            LocalWindowSize provides windowSize,
            content = content,
        )
    }
    NavigationBarColor(themeType.isDarkTheme())
}
