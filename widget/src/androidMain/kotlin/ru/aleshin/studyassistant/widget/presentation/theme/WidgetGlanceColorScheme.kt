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

import android.content.Context
import android.os.Build
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.glance.color.ColorProviders
import androidx.glance.material3.ColorProviders
import ru.aleshin.studyassistant.core.domain.entities.settings.ThemeType
import ru.aleshin.studyassistant.core.ui.theme.material.backgroundDark
import ru.aleshin.studyassistant.core.ui.theme.material.backgroundLight
import ru.aleshin.studyassistant.core.ui.theme.material.errorContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.errorContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.errorDark
import ru.aleshin.studyassistant.core.ui.theme.material.errorLight
import ru.aleshin.studyassistant.core.ui.theme.material.onBackgroundDark
import ru.aleshin.studyassistant.core.ui.theme.material.onBackgroundLight
import ru.aleshin.studyassistant.core.ui.theme.material.onErrorContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onErrorContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onErrorDark
import ru.aleshin.studyassistant.core.ui.theme.material.onErrorLight
import ru.aleshin.studyassistant.core.ui.theme.material.onPrimaryContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onPrimaryContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onPrimaryDark
import ru.aleshin.studyassistant.core.ui.theme.material.onPrimaryLight
import ru.aleshin.studyassistant.core.ui.theme.material.onSecondaryContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onSecondaryContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onSecondaryDark
import ru.aleshin.studyassistant.core.ui.theme.material.onSecondaryLight
import ru.aleshin.studyassistant.core.ui.theme.material.onSurfaceDark
import ru.aleshin.studyassistant.core.ui.theme.material.onSurfaceLight
import ru.aleshin.studyassistant.core.ui.theme.material.onSurfaceVariantDark
import ru.aleshin.studyassistant.core.ui.theme.material.onSurfaceVariantLight
import ru.aleshin.studyassistant.core.ui.theme.material.onTertiaryContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.onTertiaryContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.onTertiaryDark
import ru.aleshin.studyassistant.core.ui.theme.material.onTertiaryLight
import ru.aleshin.studyassistant.core.ui.theme.material.outlineDark
import ru.aleshin.studyassistant.core.ui.theme.material.outlineLight
import ru.aleshin.studyassistant.core.ui.theme.material.outlineVariantDark
import ru.aleshin.studyassistant.core.ui.theme.material.outlineVariantLight
import ru.aleshin.studyassistant.core.ui.theme.material.primaryContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.primaryContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.primaryDark
import ru.aleshin.studyassistant.core.ui.theme.material.primaryLight
import ru.aleshin.studyassistant.core.ui.theme.material.secondaryContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.secondaryContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.secondaryDark
import ru.aleshin.studyassistant.core.ui.theme.material.secondaryLight
import ru.aleshin.studyassistant.core.ui.theme.material.surfaceDark
import ru.aleshin.studyassistant.core.ui.theme.material.surfaceLight
import ru.aleshin.studyassistant.core.ui.theme.material.surfaceVariantDark
import ru.aleshin.studyassistant.core.ui.theme.material.surfaceVariantLight
import ru.aleshin.studyassistant.core.ui.theme.material.tertiaryContainerDark
import ru.aleshin.studyassistant.core.ui.theme.material.tertiaryContainerLight
import ru.aleshin.studyassistant.core.ui.theme.material.tertiaryDark
import ru.aleshin.studyassistant.core.ui.theme.material.tertiaryLight

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
object WidgetGlanceColorScheme {

    fun fetch(context: Context, theme: ThemeType): ColorProviders {
        val light = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicLightColorScheme(context)
        } else {
            lightWidgetColorScheme
        }
        val dark = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            dynamicDarkColorScheme(context)
        } else {
            darkWidgetColorScheme
        }
        return when (theme) {
            ThemeType.DEFAULT -> ColorProviders(light = light, dark = dark)
            ThemeType.LIGHT -> ColorProviders(light = light, dark = light)
            ThemeType.DARK -> ColorProviders(light = dark, dark = dark)
        }
    }
}

private val lightWidgetColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
)

private val darkWidgetColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
)
