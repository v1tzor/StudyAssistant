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

package ru.aleshin.studyassistant.widget.presentation.theme.tokens

import androidx.compose.ui.unit.sp
import androidx.glance.text.FontWeight
import androidx.glance.text.TextDefaults
import androidx.glance.text.TextStyle

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
data class WidgetTypography(
    val title: TextStyle = TextDefaults.defaultTextStyle.copy(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
    val label: TextStyle = TextDefaults.defaultTextStyle.copy(
        fontSize = 13.sp,
        fontWeight = FontWeight.Medium,
    ),
    val body: TextStyle = TextDefaults.defaultTextStyle.copy(fontSize = 12.sp),
    val caption: TextStyle = TextDefaults.defaultTextStyle.copy(fontSize = 10.sp),
)
