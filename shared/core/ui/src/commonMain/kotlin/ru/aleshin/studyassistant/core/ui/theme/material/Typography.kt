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
package ru.aleshin.studyassistant.core.ui.theme.material

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import ru.aleshin.studyassistant.core.ui.resources.Res
import ru.aleshin.studyassistant.core.ui.resources.Roboto

/**
 * @author Stanislav Aleshin on 11.09.2025.
 */
@Composable
fun StudyAssistantTypography() = Typography().run {
    val robotoFontFamily = robotoFontFamily()

    return@run copy(
        displayLarge = displayLarge.copy(
            fontFamily = robotoFontFamily
        ),
        displayMedium = displayMedium.copy(
            fontFamily = robotoFontFamily
        ),
        displaySmall = displaySmall.copy(
            fontFamily = robotoFontFamily
        ),
        headlineLarge = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.1.sp
        ),
        headlineMedium = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.1.sp
        ),
        headlineSmall = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.1.sp
        ),
        titleLarge = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.15.sp
        ),
        titleMedium = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.15.sp
        ),
        bodyLarge = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.3.sp
        ),
        bodyMedium = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.3.sp
        ),
        bodySmall = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.3.sp
        ),
        labelLarge = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp
        ),
        labelSmall = TextStyle.Default.copy(
            fontFamily = robotoFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.1.sp
        )
    )
}

@Composable
fun robotoFontFamily() = FontFamily(
    Font(Res.font.Roboto, FontWeight.ExtraLight),
    Font(Res.font.Roboto, FontWeight.Light),
    Font(Res.font.Roboto, FontWeight.Normal),
    Font(Res.font.Roboto, FontWeight.Medium),
    Font(Res.font.Roboto, FontWeight.SemiBold),
    Font(Res.font.Roboto, FontWeight.Bold),
    Font(Res.font.Roboto, FontWeight.ExtraBold),
    Font(Res.font.Roboto, FontWeight.Black),
)

val baseTypography = Typography(
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
    ),
)
