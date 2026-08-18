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
import android.content.res.Configuration
import android.os.Build
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.ContentScale
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import ru.aleshin.studyassistant.core.domain.entities.settings.LanguageType
import ru.aleshin.studyassistant.core.domain.entities.settings.ThemeType
import ru.aleshin.studyassistant.widget.R
import ru.aleshin.studyassistant.widget.presentation.state.WidgetStateKeys
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetShapes
import ru.aleshin.studyassistant.widget.presentation.theme.tokens.WidgetTypography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
fun WidgetTheme(
    context: Context,
    content: @Composable () -> Unit,
) {
    val theme = currentState(WidgetStateKeys.theme)
        ?.let { value -> ThemeType.entries.firstOrNull { it.name == value } }
        ?: ThemeType.DEFAULT
    val language = currentState(WidgetStateKeys.language)
        ?.let { value -> LanguageType.entries.firstOrNull { it.name == value } }
        ?: LanguageType.DEFAULT

    GlanceTheme(colors = WidgetGlanceColorScheme.fetch(context, theme)) {
        CompositionLocalProvider(
            LocalWidgetTypography provides WidgetTypography(),
            LocalWidgetLanguage provides language,
            LocalWidgetAccents provides widgetAccentColors(isWidgetDark(context, theme)),
            content = content,
        )
    }
}

private fun isWidgetDark(context: Context, theme: ThemeType): Boolean {
    return when (theme) {
        ThemeType.LIGHT -> false
        ThemeType.DARK -> true
        ThemeType.DEFAULT -> {
            val nightMask = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            nightMask == Configuration.UI_MODE_NIGHT_YES
        }
    }
}

@Composable
fun widgetString(@StringRes resource: Int): String {
    val context = LocalContext.current
    val locale = widgetLocale(LocalWidgetLanguage.current)
    val configuration = Configuration(context.resources.configuration).apply { setLocale(locale) }
    return context.createConfigurationContext(configuration).getString(resource)
}

@Composable
fun formatWidgetTime(time: Long): String {
    val context = LocalContext.current
    val pattern = if (android.text.format.DateFormat.is24HourFormat(context)) "HH:mm" else "h:mm"
    return SimpleDateFormat(pattern, widgetLocale(LocalWidgetLanguage.current)).format(Date(time))
}

@Composable
fun formatWidgetDate(date: Long): String {
    val context = LocalContext.current
    val locale = widgetLocale(LocalWidgetLanguage.current)
    val targetDate = Date(date)
    val today = java.util.Calendar.getInstance().apply { time = Date() }
    val target = java.util.Calendar.getInstance().apply { time = targetDate }
    val isToday = today.get(java.util.Calendar.ERA) == target.get(java.util.Calendar.ERA) &&
        today.get(java.util.Calendar.YEAR) == target.get(java.util.Calendar.YEAR) &&
        today.get(java.util.Calendar.DAY_OF_YEAR) == target.get(java.util.Calendar.DAY_OF_YEAR)
    return if (isToday) {
        val configuration = Configuration(context.resources.configuration).apply { setLocale(locale) }
        context.createConfigurationContext(configuration).getString(R.string.widget_today)
    } else {
        SimpleDateFormat("EEE, d MMM", locale).format(targetDate)
    }
}

fun formatWidgetDuration(duration: Long): String {
    val totalMinutes = (duration.coerceAtLeast(0L) / 60_000L)
    val hours = totalMinutes / 60L
    val minutes = totalMinutes % 60L
    return "$hours:${minutes.toString().padStart(2, '0')}"
}

fun GlanceModifier.compatCornerBackground(
    color: ColorProvider,
    cornerRadius: Int,
): GlanceModifier {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        cornerRadius(cornerRadius.dp).background(color)
    } else {
        background(
            imageProvider = when (cornerRadius) {
                in 0..WidgetShapes.SMALL -> ImageProvider(R.drawable.widget_background_8)
                in (WidgetShapes.SMALL + 1)..WidgetShapes.LARGE -> {
                    ImageProvider(R.drawable.widget_background_16)
                }
                in (WidgetShapes.LARGE + 1)..WidgetShapes.EXTRA_LARGE -> {
                    ImageProvider(R.drawable.widget_background_24)
                }
                else -> ImageProvider(R.drawable.widget_background_circle)
            },
            contentScale = ContentScale.FillBounds,
            colorFilter = ColorFilter.tint(color),
        )
    }
}

@Composable
fun GlanceTheme.widgetTypography(): WidgetTypography = LocalWidgetTypography.current

@Composable
fun TextStyle.withColor(color: ColorProvider): TextStyle = copy(color = color)

private fun widgetLocale(language: LanguageType): Locale = when (language) {
    LanguageType.EN -> Locale.ENGLISH
    LanguageType.RU -> Locale.forLanguageTag("ru")
    LanguageType.DEFAULT -> Locale.getDefault()
}

private val LocalWidgetTypography = compositionLocalOf { WidgetTypography() }
private val LocalWidgetLanguage = compositionLocalOf { LanguageType.DEFAULT }
