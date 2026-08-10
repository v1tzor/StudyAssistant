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

package ru.aleshin.studyassistant.core.common.navigation

import ru.aleshin.studyassistant.core.common.functional.UID

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
sealed class WidgetDeepLinkDestination {

    data object Schedule : WidgetDeepLinkDestination()
    data object Homeworks : WidgetDeepLinkDestination()
    data object Todos : WidgetDeepLinkDestination()
    data object Goals : WidgetDeepLinkDestination()

    data class ScheduleEditor(
        val date: Long,
        val customScheduleId: UID?,
        val baseScheduleId: UID?,
    ) : WidgetDeepLinkDestination()

    data class HomeworkEditor(
        val homeworkId: UID?,
        val date: Long?,
        val subjectId: UID?,
        val organizationId: UID?,
    ) : WidgetDeepLinkDestination()

    data class TodoEditor(
        val todoId: UID?,
    ) : WidgetDeepLinkDestination()

    fun toUrl(): String = when (this) {
        Schedule -> "$WIDGET_DEEP_LINK_PREFIX/schedule"
        Homeworks -> "$WIDGET_DEEP_LINK_PREFIX/homeworks"
        Todos -> "$WIDGET_DEEP_LINK_PREFIX/todos"
        Goals -> "$WIDGET_DEEP_LINK_PREFIX/goals"
        is ScheduleEditor -> "$WIDGET_DEEP_LINK_PREFIX/editor/schedule" + queryOf(
            DATE_PARAM to date.toString(),
            CUSTOM_SCHEDULE_ID_PARAM to customScheduleId,
            BASE_SCHEDULE_ID_PARAM to baseScheduleId,
        )
        is HomeworkEditor -> "$WIDGET_DEEP_LINK_PREFIX/editor/homework" + queryOf(
            HOMEWORK_ID_PARAM to homeworkId,
            DATE_PARAM to date?.toString(),
            SUBJECT_ID_PARAM to subjectId,
            ORGANIZATION_ID_PARAM to organizationId,
        )
        is TodoEditor -> "$WIDGET_DEEP_LINK_PREFIX/editor/todo" + queryOf(
            TODO_ID_PARAM to todoId,
        )
    }

    companion object {

        fun fromDeepLinkUrl(url: DeepLinkUrl): WidgetDeepLinkDestination? {
            if (url.pathSegments.firstOrNull() != WIDGET_SEGMENT) return null
            return when (url.pathSegments.drop(1)) {
                listOf("schedule") -> Schedule
                listOf("homeworks") -> Homeworks
                listOf("todos") -> Todos
                listOf("goals") -> Goals
                listOf("editor", "schedule") -> {
                    val date = url.params[DATE_PARAM]?.toLongOrNull() ?: return null
                    ScheduleEditor(
                        date = date,
                        customScheduleId = url.params[CUSTOM_SCHEDULE_ID_PARAM],
                        baseScheduleId = url.params[BASE_SCHEDULE_ID_PARAM],
                    )
                }
                listOf("editor", "homework") -> HomeworkEditor(
                    homeworkId = url.params[HOMEWORK_ID_PARAM],
                    date = url.params[DATE_PARAM]?.toLongOrNull(),
                    subjectId = url.params[SUBJECT_ID_PARAM],
                    organizationId = url.params[ORGANIZATION_ID_PARAM],
                )
                listOf("editor", "todo") -> TodoEditor(
                    todoId = url.params[TODO_ID_PARAM],
                )
                else -> null
            }
        }
    }
}

private fun queryOf(vararg values: Pair<String, String?>): String {
    val query = values.mapNotNull { (key, value) -> value?.let { "$key=$it" } }
    return query.takeIf { it.isNotEmpty() }?.joinToString(prefix = "?", separator = "&") ?: ""
}

private const val WIDGET_DEEP_LINK_PREFIX = "studyassistant://widget"
private const val WIDGET_SEGMENT = "widget"
private const val DATE_PARAM = "date"
private const val CUSTOM_SCHEDULE_ID_PARAM = "customScheduleId"
private const val BASE_SCHEDULE_ID_PARAM = "baseScheduleId"
private const val HOMEWORK_ID_PARAM = "homeworkId"
private const val SUBJECT_ID_PARAM = "subjectId"
private const val ORGANIZATION_ID_PARAM = "organizationId"
private const val TODO_ID_PARAM = "todoId"
