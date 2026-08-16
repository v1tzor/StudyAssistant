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

package ru.aleshin.studyassistant.core.common.functional.ocr

import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Serializable
enum class OcrLanguage {
    RUSSIAN, ENGLISH
}

@Serializable
data class OcrRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
) {
    val centerX get() = (left + right) / 2
    val centerY get() = (top + bottom) / 2
    val height get() = bottom - top
}

@Serializable
data class OcrWord(
    val text: String,
    val confidence: Float?,
    val box: OcrRect
)

@Serializable
data class OcrLine(
    val text: String,
    val confidence: Float?,
    val box: OcrRect,
    val words: List<OcrWord>
)

@Serializable
data class OcrResult(
    val text: String,
    val lines: List<OcrLine>
)

@Serializable
data class ScheduleOcrDocument(
    val rows: List<ScheduleOcrRow>,
    val rawText: String,
    val confidence: Float?
)

@Serializable
data class ScheduleOcrRow(
    val cells: List<ScheduleOcrCell>
)

@Serializable
data class ScheduleOcrCell(
    val text: String,
    val box: OcrRect,
    val confidence: Float?
)
