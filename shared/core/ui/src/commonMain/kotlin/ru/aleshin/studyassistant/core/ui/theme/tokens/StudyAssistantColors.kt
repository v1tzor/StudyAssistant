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

package ru.aleshin.studyassistant.core.ui.theme.tokens

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import ru.aleshin.studyassistant.core.ui.theme.material.greenContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.greenContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.greenDark
import ru.aleshin.studyassistant.core.ui.theme.material.greenLight
import ru.aleshin.studyassistant.core.ui.theme.material.onGreenContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onGreenContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onGreenDark
import ru.aleshin.studyassistant.core.ui.theme.material.onGreenLight
import ru.aleshin.studyassistant.core.ui.theme.material.onOrangeContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onOrangeContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onOrangeDark
import ru.aleshin.studyassistant.core.ui.theme.material.onOrangeLight
import ru.aleshin.studyassistant.core.ui.theme.material.onRedContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onRedContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onRedDark
import ru.aleshin.studyassistant.core.ui.theme.material.onRedLight
import ru.aleshin.studyassistant.core.ui.theme.material.onYellowContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onYellowContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onYellowDark
import ru.aleshin.studyassistant.core.ui.theme.material.onYellowLight
import ru.aleshin.studyassistant.core.ui.theme.material.orangeContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.orangeContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.orangeDark
import ru.aleshin.studyassistant.core.ui.theme.material.orangeLight
import ru.aleshin.studyassistant.core.ui.theme.material.redContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.redContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.redDark
import ru.aleshin.studyassistant.core.ui.theme.material.redLight
import ru.aleshin.studyassistant.core.ui.theme.material.yellowContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.yellowContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.yellowDark
import ru.aleshin.studyassistant.core.ui.theme.material.yellowLight

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
    val onRed: Color,
    val onRedContainer: Color,
    val orange: Color,
    val orangeContainer: Color,
    val onOrange: Color,
    val onOrangeContainer: Color,
    val yellow: Color,
    val yellowContainer: Color,
    val onYellow: Color,
    val onYellowContainer: Color,
    val green: Color,
    val greenContainer: Color,
    val onGreen: Color,
    val onGreenContainer: Color,
)

fun lightColorAccents(
    red: Color = redLight,
    redContainer: Color = redContainerLight,
    onRed: Color = onRedLight,
    onRedContainer: Color = onRedContainerLight,
    orange: Color = orangeLight,
    orangeContainer: Color = orangeContainerLight,
    onOrange: Color = onOrangeLight,
    onOrangeContainer: Color = onOrangeContainerLight,
    yellow: Color = yellowLight,
    yellowContainer: Color = yellowContainerLight,
    onYellow: Color = onYellowLight,
    onYellowContainer: Color = onYellowContainerLight,
    green: Color = greenLight,
    greenContainer: Color = greenContainerLight,
    onGreen: Color = onGreenLight,
    onGreenContainer: Color = onGreenContainerLight,
) = ColorAccents(
    red = red,
    redContainer = redContainer,
    onRed = onRed,
    onRedContainer = onRedContainer,
    orange = orange,
    orangeContainer = orangeContainer,
    onOrange = onOrange,
    onOrangeContainer = onOrangeContainer,
    yellow = yellow,
    yellowContainer = yellowContainer,
    onYellow = onYellow,
    onYellowContainer = onYellowContainer,
    green = green,
    greenContainer = greenContainer,
    onGreen = onGreen,
    onGreenContainer = onGreenContainer,
)

fun darkColorAccents(
    red: Color = redDark,
    redContainer: Color = redContainerDark,
    onRed: Color = onRedDark,
    onRedContainer: Color = onRedContainerDark,
    orange: Color = orangeDark,
    orangeContainer: Color = orangeContainerDark,
    onOrange: Color = onOrangeDark,
    onOrangeContainer: Color = onOrangeContainerDark,
    yellow: Color = yellowDark,
    yellowContainer: Color = yellowContainerDark,
    onYellow: Color = onYellowDark,
    onYellowContainer: Color = onYellowContainerDark,
    green: Color = greenDark,
    greenContainer: Color = greenContainerDark,
    onGreen: Color = onGreenDark,
    onGreenContainer: Color = onGreenContainerDark,
) = ColorAccents(
    red = red,
    redContainer = redContainer,
    onRed = onRed,
    onRedContainer = onRedContainer,
    orange = orange,
    orangeContainer = orangeContainer,
    onOrange = onOrange,
    onOrangeContainer = onOrangeContainer,
    yellow = yellow,
    yellowContainer = yellowContainer,
    onYellow = onYellow,
    onYellowContainer = onYellowContainer,
    green = green,
    greenContainer = greenContainer,
    onGreen = onGreen,
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
        red -> onRed
        orange -> onRed
        yellow -> onYellow
        green -> onGreen
        redContainer -> onRedContainer
        orangeContainer -> onOrangeContainer
        yellowContainer -> onYellowContainer
        greenContainer -> onGreenContainer
        else -> MaterialTheme.colorScheme.contentColorFor(backgroundColor)
    }

val LocalStudyAssistantColors = staticCompositionLocalOf<StudyAssistantColors> {
    error("Colors type is not provided")
}