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

package ru.aleshin.studyassistant.widget.presentation.mappers

import ru.aleshin.studyassistant.widget.domain.entities.goal.WidgetGoals
import ru.aleshin.studyassistant.widget.domain.entities.homework.WidgetHomeworks
import ru.aleshin.studyassistant.widget.domain.entities.schedule.WidgetSchedule
import ru.aleshin.studyassistant.widget.domain.entities.todo.WidgetTodos
import ru.aleshin.studyassistant.widget.presentation.models.GoalWidgetItemUi
import ru.aleshin.studyassistant.widget.presentation.models.GoalsWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.models.HomeworkWidgetGroupUi
import ru.aleshin.studyassistant.widget.presentation.models.HomeworkWidgetItemUi
import ru.aleshin.studyassistant.widget.presentation.models.HomeworksWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.models.ScheduleWidgetItemUi
import ru.aleshin.studyassistant.widget.presentation.models.ScheduleWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.models.TodoWidgetItemUi
import ru.aleshin.studyassistant.widget.presentation.models.TodoWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.state.WidgetContentStatusUi

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
class WidgetStateUiMapper {

    fun mapSchedule(schedule: WidgetSchedule): ScheduleWidgetStateUi {
        return ScheduleWidgetStateUi(
            status = schedule.items.contentStatus(),
            generatedAt = schedule.generatedAt.toEpochMilliseconds(),
            date = schedule.date.toEpochMilliseconds(),
            customScheduleId = schedule.customScheduleId,
            baseScheduleId = schedule.baseScheduleId,
            items = schedule.items.map { item ->
                ScheduleWidgetItemUi(
                    id = item.uid.stableWidgetId(SCHEDULE_ID_NAMESPACE),
                    eventType = item.eventType,
                    title = item.title,
                    office = item.office,
                    color = item.color,
                    start = item.start.toEpochMilliseconds(),
                    end = item.end.toEpochMilliseconds(),
                    status = item.status,
                )
            },
        )
    }

    fun mapHomeworks(homeworks: WidgetHomeworks): HomeworksWidgetStateUi {
        return HomeworksWidgetStateUi(
            status = homeworks.groups.contentStatus(),
            generatedAt = homeworks.generatedAt.toEpochMilliseconds(),
            groups = homeworks.groups.map { group ->
                val date = group.date.toEpochMilliseconds()
                HomeworkWidgetGroupUi(
                    id = date.stableWidgetId(HOMEWORK_HEADER_ID_NAMESPACE),
                    date = date,
                    items = group.items.map { item ->
                        HomeworkWidgetItemUi(
                            id = item.uid.stableWidgetId(HOMEWORK_ID_NAMESPACE),
                            homeworkId = item.uid,
                            subjectId = item.subjectId,
                            organizationId = item.organizationId,
                            subjectName = item.subjectName,
                            subjectColor = item.subjectColor,
                            deadline = item.deadline.toEpochMilliseconds(),
                            status = item.status,
                            theoreticalTasksCount = item.theoreticalTasksCount,
                            practicalTasksCount = item.practicalTasksCount,
                            presentationTasksCount = item.presentationTasksCount,
                        )
                    },
                )
            },
        )
    }

    fun mapTodos(todos: WidgetTodos): TodoWidgetStateUi {
        return TodoWidgetStateUi(
            status = todos.items.contentStatus(),
            generatedAt = todos.generatedAt.toEpochMilliseconds(),
            items = todos.items.map { item ->
                TodoWidgetItemUi(
                    id = item.uid.stableWidgetId(TODO_ID_NAMESPACE),
                    todoId = item.uid,
                    name = item.name,
                    description = item.description,
                    deadline = item.deadline?.toEpochMilliseconds(),
                    priority = item.priority,
                    status = item.status,
                )
            },
        )
    }

    fun mapGoals(goals: WidgetGoals): GoalsWidgetStateUi {
        return GoalsWidgetStateUi(
            status = goals.items.contentStatus(),
            generatedAt = goals.generatedAt.toEpochMilliseconds(),
            items = goals.items.map { item ->
                GoalWidgetItemUi(
                    id = item.uid.stableWidgetId(GOAL_ID_NAMESPACE),
                    number = item.number,
                    contentType = item.contentType,
                    title = item.title,
                    color = item.color,
                    timeType = item.timeType,
                    elapsedTime = item.elapsedTime,
                    targetTime = item.targetTime,
                    progress = item.progress,
                    status = item.status,
                )
            },
        )
    }

    private fun Collection<*>.contentStatus(): WidgetContentStatusUi {
        return if (isEmpty()) WidgetContentStatusUi.EMPTY else WidgetContentStatusUi.CONTENT
    }

    private fun String.stableWidgetId(namespace: Long): Long {
        val hash = fold(FNV_OFFSET_BASIS) { hash, char ->
            (hash xor char.code.toLong()) * FNV_PRIME
        }
        return namespace or (hash and WIDGET_ID_PAYLOAD_MASK)
    }

    private fun Long.stableWidgetId(namespace: Long): Long {
        return namespace or (this and WIDGET_ID_PAYLOAD_MASK)
    }
}

private const val FNV_OFFSET_BASIS = -3750763034362895579L
private const val FNV_PRIME = 1099511628211L
private const val WIDGET_ID_PAYLOAD_MASK = 0x07FF_FFFF_FFFF_FFFFL
private const val SCHEDULE_ID_NAMESPACE = 0x0800_0000_0000_0000L
private const val HOMEWORK_HEADER_ID_NAMESPACE = 0x1000_0000_0000_0000L
private const val HOMEWORK_ID_NAMESPACE = 0x1800_0000_0000_0000L
private const val TODO_ID_NAMESPACE = 0x2000_0000_0000_0000L
private const val GOAL_ID_NAMESPACE = 0x2800_0000_0000_0000L
