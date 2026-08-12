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

package ru.aleshin.studyassistant.widget

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.widget.domain.entities.todo.WidgetTodoItem
import ru.aleshin.studyassistant.widget.domain.entities.todo.WidgetTodoStatus
import ru.aleshin.studyassistant.widget.domain.entities.todo.WidgetTodos
import ru.aleshin.studyassistant.widget.presentation.mappers.WidgetStateUiMapper
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class WidgetStateUiMapperTest {

    private val mapper = WidgetStateUiMapper()

    @Test
    fun shouldCreateGlanceSafeStableItemIds() {
        val todos = WidgetTodos(
            generatedAt = Instant.fromEpochMilliseconds(1L),
            items = listOf(
                WidgetTodoItem(
                    uid = "todo-with-negative-fnv-hash",
                    name = "Task",
                    description = null,
                    deadline = null,
                    priority = TaskPriority.STANDARD,
                    status = WidgetTodoStatus.ACTIVE,
                ),
            ),
            nextUpdateAt = Instant.fromEpochMilliseconds(2L),
        )

        val firstId = mapper.mapTodos(todos).items.single().id
        val secondId = mapper.mapTodos(todos).items.single().id

        assertTrue(firstId >= 0L)
        assertTrue(firstId <= GLANCE_ITEM_ID_MAX_VALUE)
        assertEquals(firstId, secondId)
    }
}

private const val GLANCE_ITEM_ID_MAX_VALUE = 0x3FFF_FFFF_FFFF_FFFFL
