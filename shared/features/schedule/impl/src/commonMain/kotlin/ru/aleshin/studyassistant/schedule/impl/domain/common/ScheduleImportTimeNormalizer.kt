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

package ru.aleshin.studyassistant.schedule.impl.domain.common

import kotlinx.datetime.LocalTime
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportClass
import ru.aleshin.studyassistant.schedule.impl.domain.validation.ScheduleImportValidator

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
internal class ScheduleImportTimeNormalizer(
    private val validator: ScheduleImportValidator,
) {

    fun normalize(classes: List<ScheduleImportClass>): List<ScheduleImportClass> {
        val byDay = classes.groupBy { classModel -> classModel.repeatWeek to classModel.dayOfWeek }
        val gridsByWeek = buildWeekGrids(byDay)
        val normalizedById = byDay.flatMap { (key, dayClasses) ->
            normalizeDay(
                dayClasses = dayClasses,
                grid = gridsByWeek[key.first].orEmpty(),
            )
        }.associateBy(ScheduleImportClass::uid)
        return classes.map { classModel ->
            normalizedById[classModel.uid] ?: classModel
        }.sortedWith(
            compareBy(
                ScheduleImportClass::repeatWeek,
                ScheduleImportClass::dayOfWeek,
                { classModel -> classModel.number ?: Int.MAX_VALUE },
                ScheduleImportClass::startTime,
            )
        )
    }

    private fun buildWeekGrids(
        byDay: Map<Pair<Int, Int>, List<ScheduleImportClass>>,
    ): Map<Int, Map<Int, TimeSlot>> {
        val grids = mutableMapOf<Int, MutableMap<Int, MutableList<TimeSlot>>>()
        byDay.forEach { (key, dayClasses) ->
            val parsed = dayClasses.map(::parsedClass).sortedBy(ParsedClass::sortMinutes)
            if (!hasDistinctStarts(parsed)) return@forEach
            val uniqueNumbers = parsed.mapNotNull(ParsedClass::number).toSet()
            if (uniqueNumbers.size != parsed.size) return@forEach
            val weekSlots = grids.getOrPut(key.first) { mutableMapOf() }
            parsed.forEach { item ->
                val number = item.number ?: return@forEach
                val start = item.start ?: return@forEach
                val end = item.end ?: return@forEach
                weekSlots.getOrPut(number) { mutableListOf() }.add(TimeSlot(start, end))
            }
        }
        return grids.mapValues { (_, numberedSlots) ->
            numberedSlots.mapNotNull { (number, slots) ->
                val slot = slots.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key
                slot?.let { number to it }
            }.toMap()
        }
    }

    private fun normalizeDay(
        dayClasses: List<ScheduleImportClass>,
        grid: Map<Int, TimeSlot>,
    ): List<ScheduleImportClass> {
        val parsed = dayClasses.map(::parsedClass).sortedWith(
            compareBy<ParsedClass> { item -> item.start?.toMinutes() ?: Int.MAX_VALUE }
                .thenBy { item -> item.number ?: Int.MAX_VALUE },
        )
        val withTimes = repairTimes(parsed, grid)
        val withNumbers = repairNumbers(withTimes, grid)
        return withNumbers.map(ParsedClass::toClass)
    }

    private fun repairTimes(
        parsed: List<ParsedClass>,
        grid: Map<Int, TimeSlot>,
    ): List<ParsedClass> {
        if (parsed.size <= 1) {
            return parsed.map { item -> applyGridSlot(item, grid[item.number]) }
        }
        if (hasClonedTimes(parsed)) {
            val byNumber = parsed.map { item ->
                grid[item.number]?.let { slot -> item.withTimes(slot) } ?: item
            }
            if (hasDistinctStarts(byNumber)) return byNumber
            val byOrder = applyGridByOrder(parsed, grid)
            if (hasDistinctStarts(byOrder)) return byOrder
            return splitClonedSpan(parsed)
        }
        val matched = parsed.map { item ->
            val slot = grid.entries.firstOrNull { entry ->
                entry.value.start == item.start
            }?.value
            if (item.end == null && slot != null) item.withTimes(slot) else item
        }
        return repairClonedEnds(matched)
    }

    private fun repairNumbers(
        parsed: List<ParsedClass>,
        grid: Map<Int, TimeSlot>,
    ): List<ParsedClass> {
        val fromGrid = parsed.map { item ->
            val matchedNumber = grid.entries.firstOrNull { entry ->
                entry.value.start == item.start
            }?.key
            if (matchedNumber != null) item.copy(number = matchedNumber) else item
        }
        val numbers = fromGrid.map(ParsedClass::number)
        val uniquePresent = numbers.filterNotNull().toSet()
        val shouldFill = numbers.any { number -> number == null }
        val shouldRenumberIdentical = fromGrid.size > 1 && uniquePresent.size <= 1 && uniquePresent.isNotEmpty()
        if (!shouldFill && !shouldRenumberIdentical) {
            return fromGrid
        }
        val used = uniquePresent.toMutableSet()
        var nextFallback = 1
        return fromGrid.mapIndexed { index, item ->
            val existing = item.number.takeUnless { shouldRenumberIdentical }
            if (existing != null) {
                item
            } else {
                val gridNumber = grid.entries.firstOrNull { entry ->
                    entry.value.start == item.start && entry.key !in used
                }?.key
                val assigned = gridNumber ?: run {
                    while (nextFallback in used) nextFallback += 1
                    nextFallback
                }
                used += assigned
                item.copy(number = assigned)
            }.let { updated ->
                if (updated.number == null) {
                    updated.copy(number = index + 1)
                } else {
                    updated
                }
            }
        }
    }

    private fun applyGridByOrder(
        parsed: List<ParsedClass>,
        grid: Map<Int, TimeSlot>,
    ): List<ParsedClass> {
        val orderedSlots = grid.entries.sortedBy { entry -> entry.key }.map { it.value }
        if (orderedSlots.size < parsed.size) return parsed
        return parsed.mapIndexed { index, item ->
            item.withTimes(orderedSlots[index]).copy(number = item.number ?: orderedSlots.getOrNull(index)?.let { slot ->
                grid.entries.firstOrNull { entry -> entry.value == slot }?.key
            })
        }
    }

    private fun splitClonedSpan(parsed: List<ParsedClass>): List<ParsedClass> {
        val start = parsed.first().start ?: return parsed
        val end = parsed.first().end ?: return parsed
        val span = end.toMinutes() - start.toMinutes()
        if (span <= 0) return parsed
        val duration = ((span - DEFAULT_BREAK_MINUTES * (parsed.size - 1)) / parsed.size)
            .coerceAtLeast(MIN_CLASS_MINUTES)
        var cursor = start.toMinutes()
        return parsed.map { item ->
            val classEnd = (cursor + duration).coerceAtMost(MINUTES_IN_DAY - 1)
            val updated = item.withTimes(
                TimeSlot(
                    start = minutesToTime(cursor) ?: start,
                    end = minutesToTime(classEnd) ?: end,
                )
            )
            cursor = (classEnd + DEFAULT_BREAK_MINUTES).coerceAtMost(MINUTES_IN_DAY - 1)
            updated
        }
    }

    private fun repairClonedEnds(parsed: List<ParsedClass>): List<ParsedClass> {
        val ends = parsed.mapNotNull(ParsedClass::end)
        if (ends.toSet().size != 1 || parsed.size <= 1) return parsed
        return parsed.mapIndexed { index, item ->
            val start = item.start ?: return@mapIndexed item
            val nextStart = parsed.getOrNull(index + 1)?.start
            val end = if (nextStart != null && nextStart > start) {
                val gap = nextStart.toMinutes() - start.toMinutes()
                if (gap > MAX_CLASS_MINUTES) {
                    minutesToTime(start.toMinutes() + DEFAULT_CLASS_MINUTES)?.takeIf { it <= nextStart }
                        ?: nextStart
                } else {
                    nextStart
                }
            } else {
                item.end
            }
            item.copy(end = end)
        }
    }

    private fun applyGridSlot(item: ParsedClass, slot: TimeSlot?): ParsedClass {
        if (slot == null || item.start != null && item.end != null) return item
        return item.withTimes(slot)
    }

    private fun hasDistinctStarts(parsed: List<ParsedClass>): Boolean {
        return parsed.mapNotNull(ParsedClass::start).toSet().size >= 2
    }

    private fun hasClonedTimes(parsed: List<ParsedClass>): Boolean {
        val starts = parsed.mapNotNull(ParsedClass::start).toSet()
        val ends = parsed.mapNotNull(ParsedClass::end).toSet()
        if (starts.size != 1 || ends.size != 1) return false
        val start = starts.single()
        val end = ends.single()
        return end.toMinutes() - start.toMinutes() >= MEGA_SPAN_MINUTES
    }

    private fun parsedClass(classModel: ScheduleImportClass): ParsedClass {
        return ParsedClass(
            source = classModel,
            number = classModel.number,
            start = validator.parseTime(classModel.startTime),
            end = validator.parseTime(classModel.endTime),
        )
    }

    private data class TimeSlot(
        val start: LocalTime,
        val end: LocalTime,
    )

    private data class ParsedClass(
        val source: ScheduleImportClass,
        val number: Int?,
        val start: LocalTime?,
        val end: LocalTime?,
    ) {
        val sortMinutes: Int
            get() = start?.toMinutes() ?: Int.MAX_VALUE

        fun withTimes(slot: TimeSlot) = copy(start = slot.start, end = slot.end)

        fun toClass(): ScheduleImportClass {
            return source.copy(
                number = number,
                startTime = start?.formatClock() ?: source.startTime,
                endTime = end?.formatClock() ?: source.endTime,
            )
        }
    }

    private companion object {
        const val MEGA_SPAN_MINUTES = 90
        const val DEFAULT_CLASS_MINUTES = 45
        const val DEFAULT_BREAK_MINUTES = 10
        const val MIN_CLASS_MINUTES = 15
        const val MAX_CLASS_MINUTES = 90
        const val MINUTES_IN_DAY = 24 * 60
    }
}

private fun LocalTime.toMinutes(): Int = hour * 60 + minute

private fun minutesToTime(total: Int): LocalTime? {
    if (total !in 0 until 24 * 60) return null
    return LocalTime(hour = total / 60, minute = total % 60)
}

private fun LocalTime.formatClock(): String {
    val hours = hour.toString().padStart(2, '0')
    val minutes = minute.toString().padStart(2, '0')
    return "$hours:$minutes"
}
