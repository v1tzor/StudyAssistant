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

package ru.aleshin.studyassistant.core.domain.entities.analytics

import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.fetchAllTasks
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkComponents

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
data class DailyWorkload(val value: Float) {

    fun isHigh(threshold: Int): Boolean = value >= threshold

    companion object {

        fun calculate(
            classes: List<Class>,
            homeworks: List<Homework>,
            todos: List<Todo>,
        ): DailyWorkload {
            val movementMap = classes.groupBy { it.organization }.mapValues { classEntry ->
                classEntry.value.map { it.location }.toSet()
            }
            val testsRate = homeworks.count { it.test != null } * DailyAnalysis.TEST_RATE
            val classesRate = classes.sumOf { it.timeRange.periodDuration().minutes } *
                DailyAnalysis.CLASS_MINUTE_DURATION_RATE
            val movementsRate = movementMap.values.sumOf { it.size } * DailyAnalysis.MOVEMENT_RATE
            val theoriesRate = homeworks.sumOf { homework ->
                homework.theoreticalTasks.toHomeworkComponents().fetchAllTasks().size
            } * DailyAnalysis.THEORY_RATE
            val practicesRate = homeworks.sumOf { homework ->
                homework.practicalTasks.toHomeworkComponents().fetchAllTasks().size
            } * DailyAnalysis.PRACTICE_RATE
            val presentationsRate = homeworks.sumOf { homework ->
                homework.presentationTasks.toHomeworkComponents().fetchAllTasks().size
            } * DailyAnalysis.PRESENTATION_RATE
            val todosRate = todos.sumOf { todo ->
                if (todo.priority == TaskPriority.STANDARD) {
                    DailyAnalysis.TODO_RATE
                } else {
                    DailyAnalysis.TODO_PRIORITY_RATE
                }
            }.toFloat()
            val rate = classesRate + testsRate + movementsRate + theoriesRate + practicesRate +
                presentationsRate + todosRate

            return DailyWorkload(value = rate / DailyAnalysis.MAX_RATE * 10f)
        }
    }
}
