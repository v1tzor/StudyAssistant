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

package ru.aleshin.studyassistant.backend.ai.domain.tools

import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiToolCatalogTest {

    private val catalog = AiToolCatalog()

    @Test
    fun catalogContainsOnlyImplementedClientTools() {
        assertEquals(EXPECTED_TOOL_NAMES, catalog.names)
    }

    @Test
    fun classMutationsRequireDateAndCreateRequiresOrganization() {
        val tools = requireNotNull(
            catalog.resolve(listOf("create_class", "update_class")),
        ).associateBy { it.name }

        assertTrue("date" in tools.getValue("create_class").requiredFields())
        assertTrue("organizationId" in tools.getValue("create_class").requiredFields())
        assertTrue("date" in tools.getValue("update_class").requiredFields())
    }

    @Test
    fun removedToolsAreRejected() {
        assertEquals(
            null,
            catalog.resolve(listOf("get_free_time")),
        )
        assertEquals(
            null,
            catalog.resolve(listOf("delete_todo", "delete_homework", "delete_class", "delete_goal")),
        )
    }

    private fun ru.aleshin.studyassistant.backend.ai.domain.model.AiToolDefinition.requiredFields(): Set<String> {
        return parameters["required"]
            ?.jsonArray
            ?.mapTo(mutableSetOf()) { it.jsonPrimitive.content }
            .orEmpty()
    }

    private companion object {
        val EXPECTED_TOOL_NAMES = setOf(
            "get_profile",
            "get_organizations",
            "get_subjects",
            "get_employees",
            "get_employee",
            "get_todos",
            "get_homeworks",
            "get_overdue_homeworks",
            "get_classes_by_date",
            "get_classes_by_range",
            "get_near_class",
            "get_goals",
            "create_todo",
            "update_todo",
            "complete_todo",
            "create_homework",
            "update_homework",
            "complete_homework",
            "create_class",
            "update_class",
            "create_goal",
            "update_goal",
            "complete_goal",
            "create_subject",
            "update_subject",
            "create_employee",
            "update_employee",
        )
    }
}
