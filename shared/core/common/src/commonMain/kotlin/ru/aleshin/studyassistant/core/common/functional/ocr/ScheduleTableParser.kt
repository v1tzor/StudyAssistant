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

import kotlin.math.abs

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
class ScheduleTableParser {

    fun parse(result: OcrResult): ScheduleOcrDocument {
        if (result.lines.isEmpty()) {
            return ScheduleOcrDocument(emptyList(), result.text, null)
        }

        val sortedLines = result.lines.sortedBy { it.box.centerY }
        val rows = mutableListOf<MutableList<OcrLine>>()
        
        sortedLines.forEach { line ->
            val lastRow = rows.lastOrNull()
            val lastLineInRow = lastRow?.firstOrNull()
            
            if (lastLineInRow == null || isDifferentRow(lastLineInRow, line)) {
                rows.add(mutableListOf(line))
            } else {
                lastRow.add(line)
            }
        }

        val scheduleRows = rows.map { rowLines ->
            val sortedCells = rowLines.sortedBy { it.box.centerX }.map { line ->
                ScheduleOcrCell(
                    text = line.text,
                    box = line.box,
                    confidence = line.confidence
                )
            }
            ScheduleOcrRow(cells = sortedCells)
        }

        val avgConfidence = result.lines.mapNotNull { it.confidence }.average().toFloat().takeIf { !it.isNaN() }

        return ScheduleOcrDocument(
            rows = scheduleRows,
            rawText = result.text,
            confidence = avgConfidence
        )
    }

    private fun isDifferentRow(line1: OcrLine, line2: OcrLine): Boolean {
        val yDiff = abs(line1.box.centerY - line2.box.centerY)
        val avgHeight = (line1.box.height + line2.box.height) / 2
        // Threshold: 60% of average line height
        return yDiff > avgHeight * 0.6f
    }
}
