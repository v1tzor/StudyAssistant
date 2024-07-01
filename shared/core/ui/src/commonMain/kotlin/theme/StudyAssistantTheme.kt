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

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.legend.LegendLocation
import io.github.koalaplot.core.style.Axis
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.style.Sizes
import io.github.koalaplot.core.xygraph.TickPosition
import models.ThemeUiType
import theme.material.baseShapes
import theme.material.baseTypography
import theme.material.isDarkTheme
import theme.material.toColorAccents
import theme.material.toColorScheme
import theme.tokens.LanguageUiType
import theme.tokens.LocalStudyAssistantColors
import theme.tokens.LocalStudyAssistantElevations
import theme.tokens.LocalStudyAssistantIcons
import theme.tokens.LocalStudyAssistantLanguage
import theme.tokens.LocalStudyAssistantStrings
import theme.tokens.LocalWindowSize
import theme.tokens.StudyAssistantColors
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
    val appLanguage = fetchAppLanguage(languageType)
    val appElevations = fetchAppElevations()
    val appIcons = fetchCoreIcons()
    val coreColors = StudyAssistantColors(themeType.isDarkTheme(), themeType.toColorAccents())
    val coreStrings = fetchCoreStrings(appLanguage)

    MaterialTheme(
        colorScheme = themeType.toColorScheme(),
        shapes = baseShapes,
        typography = baseTypography,
    ) {
        CompositionLocalProvider(
            LocalStudyAssistantLanguage provides appLanguage,
            LocalStudyAssistantColors provides coreColors,
            LocalStudyAssistantElevations provides appElevations,
            LocalStudyAssistantStrings provides coreStrings,
            LocalStudyAssistantIcons provides appIcons,
            LocalWindowSize provides windowSize,
        ) {
            KoalaPlotTheme(
                sizes = Sizes(),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                axis = Axis(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    majorTickSize = 7.dp,
                    minorTickSize = 3.dp,
                    lineThickness = 0.dp,
                    xyGraphTickPosition = TickPosition.Outside,
                    majorGridlineStyle = LineStyle(
                        brush = SolidColor(MaterialTheme.colorScheme.outlineVariant),
                        strokeWidth = 1.dp,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                    ),
                    minorGridlineStyle = null,
                ),
                legendLocation = LegendLocation.BOTTOM,
                content = content,
            )
        }
    }
    NavigationBarColor(themeType.isDarkTheme())
}