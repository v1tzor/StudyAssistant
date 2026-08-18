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

package ru.aleshin.studyassistant.widget.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.glance.unit.ColorProvider
import ru.aleshin.studyassistant.core.ui.theme.material.greenContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.greenContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.greenDark
import ru.aleshin.studyassistant.core.ui.theme.material.greenLight
import ru.aleshin.studyassistant.core.ui.theme.material.onGreenContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onGreenContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onOrangeContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onOrangeContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onRedContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onRedContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onYellowContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onYellowContainerLight
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
 * @author Stanislav Aleshin on 18.08.2026.
 */
data class WidgetAccentColors(
    val red: ColorProvider,
    val redContainer: ColorProvider,
    val onRedContainer: ColorProvider,
    val orange: ColorProvider,
    val orangeContainer: ColorProvider,
    val onOrangeContainer: ColorProvider,
    val yellow: ColorProvider,
    val yellowContainer: ColorProvider,
    val onYellowContainer: ColorProvider,
    val green: ColorProvider,
    val greenContainer: ColorProvider,
    val onGreenContainer: ColorProvider,
)

fun widgetAccentColors(isDark: Boolean): WidgetAccentColors {
    return if (isDark) {
        WidgetAccentColors(
            red = ColorProvider(redDark),
            redContainer = ColorProvider(redContainerDark),
            onRedContainer = ColorProvider(onRedContainerDark),
            orange = ColorProvider(orangeDark),
            orangeContainer = ColorProvider(orangeContainerDark),
            onOrangeContainer = ColorProvider(onOrangeContainerDark),
            yellow = ColorProvider(yellowDark),
            yellowContainer = ColorProvider(yellowContainerDark),
            onYellowContainer = ColorProvider(onYellowContainerDark),
            green = ColorProvider(greenDark),
            greenContainer = ColorProvider(greenContainerDark),
            onGreenContainer = ColorProvider(onGreenContainerDark),
        )
    } else {
        WidgetAccentColors(
            red = ColorProvider(redLight),
            redContainer = ColorProvider(redContainerLight),
            onRedContainer = ColorProvider(onRedContainerLight),
            orange = ColorProvider(orangeLight),
            orangeContainer = ColorProvider(orangeContainerLight),
            onOrangeContainer = ColorProvider(onOrangeContainerLight),
            yellow = ColorProvider(yellowLight),
            yellowContainer = ColorProvider(yellowContainerLight),
            onYellowContainer = ColorProvider(onYellowContainerLight),
            green = ColorProvider(greenLight),
            greenContainer = ColorProvider(greenContainerLight),
            onGreenContainer = ColorProvider(onGreenContainerLight),
        )
    }
}

fun tintedSubjectColor(color: Int, alpha: Float = 0.18f): ColorProvider {
    return ColorProvider(Color(color).copy(alpha = alpha))
}

internal val LocalWidgetAccents = compositionLocalOf { widgetAccentColors(isDark = false) }

@Composable
fun widgetAccents(): WidgetAccentColors = LocalWidgetAccents.current
