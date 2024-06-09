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
 * imitations under the License.
 */

package theme.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import theme.material.greenContainerDark
import theme.material.greenContainerLight
import theme.material.greenDark
import theme.material.greenLight
import theme.material.onGreenContainerDark
import theme.material.onGreenContainerLight
import theme.material.onOrangeContainerDark
import theme.material.onOrangeContainerLight
import theme.material.onRedContainerDark
import theme.material.onRedContainerLight
import theme.material.onYellowContainerDark
import theme.material.onYellowContainerLight
import theme.material.orangeContainerDark
import theme.material.orangeContainerLight
import theme.material.orangeDark
import theme.material.orangeLight
import theme.material.redContainerDark
import theme.material.redContainerLight
import theme.material.redDark
import theme.material.redLight
import theme.material.yellowContainerDark
import theme.material.yellowContainerLight
import theme.material.yellowDark
import theme.material.yellowLight

/**
 * @author Stanislav Aleshin on 27.01.2024.
 */
data class StudyAssistantColors(
    val isDark: Boolean,
    val accents: ColorAccents,
)

data class ColorAccents(
    val red: Color,
    val redContainer: Color,
    val onRedContainer: Color,
    val orange: Color,
    val orangeContainer: Color,
    val onOrangeContainer: Color,
    val yellow: Color,
    val yellowContainer: Color,
    val onYellowContainer: Color,
    val green: Color,
    val greenContainer: Color,
    val onGreenContainer: Color,
)

fun lightColorAccents(
    red: Color = redLight,
    redContainer: Color = redContainerLight,
    onRedContainer: Color = onRedContainerLight,
    orange: Color = orangeLight,
    orangeContainer: Color = orangeContainerLight,
    onOrangeContainer: Color = onOrangeContainerLight,
    yellow: Color = yellowLight,
    yellowContainer: Color = yellowContainerLight,
    onYellowContainer: Color = onYellowContainerLight,
    green: Color = greenLight,
    greenContainer: Color = greenContainerLight,
    onGreenContainer: Color = onGreenContainerLight,
) = ColorAccents(
    red = red,
    redContainer = redContainer,
    onRedContainer = onRedContainer,
    orange = orange,
    orangeContainer = orangeContainer,
    onOrangeContainer = onOrangeContainer,
    yellow = yellow,
    yellowContainer = yellowContainer,
    onYellowContainer = onYellowContainer,
    green = green,
    greenContainer = greenContainer,
    onGreenContainer = onGreenContainer,
)

fun darkColorAccents(
    red: Color = redDark,
    redContainer: Color = redContainerDark,
    onRedContainer: Color = onRedContainerDark,
    orange: Color = orangeDark,
    orangeContainer: Color = orangeContainerDark,
    onOrangeContainer: Color = onOrangeContainerDark,
    yellow: Color = yellowDark,
    yellowContainer: Color = yellowContainerDark,
    onYellowContainer: Color = onYellowContainerDark,
    green: Color = greenDark,
    greenContainer: Color = greenContainerDark,
    onGreenContainer: Color = onGreenContainerDark,
) = ColorAccents(
    red = red,
    redContainer = redContainer,
    onRedContainer = onRedContainer,
    orange = orange,
    orangeContainer = orangeContainer,
    onOrangeContainer = onOrangeContainer,
    yellow = yellow,
    yellowContainer = yellowContainer,
    onYellowContainer = onYellowContainer,
    green = green,
    greenContainer = greenContainer,
    onGreenContainer = onGreenContainer,
)

enum class CustomColors(val dark: Long, val light: Long) {
    RED(0xFFDE2419, 0xFFEE5E55),
    ORANGE(0xFFFF961A, 0xFFDFA96A),
    YELLOW(0xFFE1CC0F, 0xFFE7DA6B),
    PISTACHIO(0xFFC5E01C, 0xFFD3E36E),
    LIME(0xFF89E514, 0xFFB3E672),
    GREEN(0xFF26E415, 0xFF89F080),
    EMERALD(0xFF19EF7C, 0xFF8AEFB8),
    CYAN(0xFF1CE8C3, 0xFF7AEEE7),
    BLUE(0xFF16AFF1, 0xFF9CD8F1),
    DARK_BLUE(0xFF3A56EB, 0xFF8B9CF6),
    INDIGO(0xFF7F37F3, 0xFFB085F7),
    LAVENDER(0xFFAA2CF6, 0xFFD58FFF),
    LIGHT_PINK(0xFFF22CF6, 0xFFF179F4),
    PINK(0xFFF118B4, 0xFFF77DD4),
    FUCHSIA(0xFFEC1C80, 0xFFF876B4),
}

@Stable
@Composable
fun ColorAccents.contentColorFor(backgroundColor: Color): Color =
    when (backgroundColor) {
        redContainer -> onRedContainer
        orangeContainer -> onOrangeContainer
        yellowContainer -> onYellowContainer
        greenContainer -> onGreenContainer
        else -> MaterialTheme.colorScheme.contentColorFor(backgroundColor)
    }

val LocalStudyAssistantColors = staticCompositionLocalOf<StudyAssistantColors> {
    error("Colors type is not provided")
}
