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

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import ru.aleshin.studyassistant.backend.ai.domain.model.AiToolDefinition

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiToolCatalog {

    val names: Set<String> = TOOLS.keys

    fun resolve(names: List<String>): List<AiToolDefinition>? {
        return names.map { name -> TOOLS[name] ?: return null }
    }

    private companion object {

        val TOOLS = listOf(
            tool(
                name = "get_profile",
                description = "Read the local user's basic profile and current study context.",
                parameters = objectSchema(),
            ),
            tool(
                name = "get_organizations",
                description = "List the user's local educational organizations and their identifiers.",
                parameters = objectSchema(),
            ),
            tool(
                name = "get_subjects",
                description = "List local subjects for one organization.",
                parameters = objectSchema(
                    required = listOf("organizationId"),
                    properties = listOf(
                        "organizationId" to idProperty("Organization identifier."),
                    ),
                ),
            ),
            tool(
                name = "get_employees",
                description = "List local teachers or employees, optionally limited to one organization.",
                parameters = objectSchema(
                    properties = listOf(
                        "organizationId" to idProperty("Organization identifier."),
                        "query" to stringProperty("Optional case-insensitive name search."),
                    ),
                ),
            ),
            tool(
                name = "get_employee",
                description = "Read full details for one local teacher or employee.",
                parameters = objectSchema(
                    required = listOf("teacherId"),
                    properties = listOf("teacherId" to idProperty("Teacher or employee identifier.")),
                ),
            ),
            tool(
                name = "get_todos",
                description = "Read TODO items for an optional date range and completion state.",
                parameters = objectSchema(
                    properties = listOf(
                        "fromDate" to dateProperty("Inclusive start date."),
                        "toDate" to dateProperty("Inclusive end date."),
                        "status" to enumProperty(
                            description = "Completion filter.",
                            values = listOf("ALL", "ACTIVE", "COMPLETED"),
                        ),
                    ),
                ),
            ),
            tool(
                name = "get_homeworks",
                description = "Read homework in an inclusive date range of no more than two weeks.",
                parameters = objectSchema(
                    required = listOf("from", "to"),
                    properties = listOf(
                        "from" to dateProperty("Inclusive start date."),
                        "to" to dateProperty("Inclusive end date."),
                    ),
                ),
            ),
            tool(
                name = "get_overdue_homeworks",
                description = "Read all incomplete homework whose deadline has passed.",
                parameters = objectSchema(),
            ),
            tool(
                name = "get_classes_by_date",
                description = "Read all classes scheduled on one local calendar date.",
                parameters = objectSchema(
                    required = listOf("date"),
                    properties = listOf("date" to dateProperty("Local calendar date.")),
                ),
            ),
            tool(
                name = "get_classes_by_range",
                description = "Read classes in an inclusive local date range.",
                parameters = objectSchema(
                    required = listOf("fromDate", "toDate"),
                    properties = listOf(
                        "fromDate" to dateProperty("Inclusive start date."),
                        "toDate" to dateProperty("Inclusive end date."),
                    ),
                ),
            ),
            tool(
                name = "get_near_class",
                description = "Read the next scheduled class for one subject.",
                parameters = objectSchema(
                    required = listOf("subjectId"),
                    properties = listOf(
                        "subjectId" to idProperty("Subject identifier."),
                    ),
                ),
            ),
            tool(
                name = "get_free_time",
                description = "Find free intervals between classes on one date.",
                parameters = objectSchema(
                    required = listOf("date"),
                    properties = listOf(
                        "date" to dateProperty("Local calendar date."),
                        "minimumMinutes" to integerProperty(
                            description = "Minimum interval duration in minutes.",
                            minimum = 1,
                            maximum = 1_440,
                        ),
                    ),
                ),
            ),
            tool(
                name = "create_todo",
                description = "Propose a new TODO. The app must ask for confirmation before saving it.",
                parameters = objectSchema(
                    required = listOf("name"),
                    properties = listOf(
                        "name" to stringProperty("Short actionable title."),
                        "description" to stringProperty("Optional details."),
                        "deadline" to dateTimeProperty("Optional local deadline."),
                        "priority" to enumProperty(
                            description = "Optional priority.",
                            values = listOf("STANDARD", "MEDIUM", "HIGH"),
                        ),
                    ),
                ),
            ),
            tool(
                name = "update_todo",
                description = "Propose changes to an existing TODO. Confirmation is required before saving.",
                parameters = objectSchema(
                    required = listOf("todoId"),
                    properties = listOf(
                        "todoId" to idProperty("TODO identifier."),
                        "name" to stringProperty("Replacement title."),
                        "description" to stringProperty("Replacement details."),
                        "deadline" to dateTimeProperty("Replacement local deadline."),
                        "priority" to enumProperty(
                            description = "Replacement priority.",
                            values = listOf("STANDARD", "MEDIUM", "HIGH"),
                        ),
                    ),
                ),
            ),
            tool(
                name = "complete_todo",
                description = "Propose changing a TODO completion state. Confirmation is required.",
                parameters = objectSchema(
                    required = listOf("todoId", "completed"),
                    properties = listOf(
                        "todoId" to idProperty("TODO identifier."),
                        "completed" to booleanProperty("The requested completion state."),
                    ),
                ),
            ),
            tool(
                name = "delete_todo",
                description = "Propose deleting one TODO. Explicit confirmation is required.",
                parameters = objectSchema(
                    required = listOf("todoId"),
                    properties = listOf("todoId" to idProperty("TODO identifier.")),
                ),
            ),
            tool(
                name = "create_homework",
                description = "Propose new homework. The app must ask for confirmation before saving it.",
                parameters = objectSchema(
                    required = listOf("organizationId", "subjectId", "deadline"),
                    properties = listOf(
                        "organizationId" to idProperty("Existing organization identifier."),
                        "subjectId" to idProperty("Existing subject identifier."),
                        "classId" to idProperty("Related class identifier when known."),
                        "deadline" to dateProperty("Local deadline."),
                        "theoreticalTasks" to stringProperty("Reading or theory tasks."),
                        "practicalTasks" to stringProperty("Exercises or practical tasks."),
                        "presentationTasks" to stringProperty("Presentation or project tasks."),
                        "testTopic" to stringProperty("Test, exam, or quiz topic."),
                    ),
                ),
            ),
            tool(
                name = "update_homework",
                description = "Propose changes to existing homework. Confirmation is required before saving.",
                parameters = objectSchema(
                    required = listOf("homeworkId"),
                    properties = listOf(
                        "homeworkId" to idProperty("Homework identifier."),
                        "organizationId" to idProperty("Replacement organization identifier."),
                        "subjectId" to idProperty("Replacement subject identifier."),
                        "classId" to idProperty("Replacement related class identifier."),
                        "deadline" to dateProperty("Replacement local deadline."),
                        "theoreticalTasks" to stringProperty("Replacement reading or theory tasks."),
                        "practicalTasks" to stringProperty("Replacement exercises or practical tasks."),
                        "presentationTasks" to stringProperty("Replacement presentation or project tasks."),
                        "testTopic" to stringProperty("Replacement test, exam, or quiz topic."),
                        "priority" to enumProperty(
                            description = "Replacement priority.",
                            values = listOf("STANDARD", "MEDIUM", "HIGH"),
                        ),
                    ),
                ),
            ),
            tool(
                name = "complete_homework",
                description = "Propose changing homework completion state. Confirmation is required.",
                parameters = objectSchema(
                    required = listOf("homeworkId", "completed"),
                    properties = listOf(
                        "homeworkId" to idProperty("Homework identifier."),
                        "completed" to booleanProperty("The requested completion state."),
                    ),
                ),
            ),
            tool(
                name = "delete_homework",
                description = "Propose deleting one homework item. Explicit confirmation is required.",
                parameters = objectSchema(
                    required = listOf("homeworkId"),
                    properties = listOf("homeworkId" to idProperty("Homework identifier.")),
                ),
            ),
            tool(
                name = "create_class",
                description = "Propose adding a class to the local schedule. Confirmation is required.",
                parameters = classMutationSchema(idField = null),
            ),
            tool(
                name = "update_class",
                description = "Propose changing an existing scheduled class. Confirmation is required.",
                parameters = classMutationSchema(idField = "classId"),
            ),
            tool(
                name = "delete_class",
                description = "Propose deleting one scheduled class. Explicit confirmation is required.",
                parameters = objectSchema(
                    required = listOf("classId", "date"),
                    properties = listOf(
                        "classId" to idProperty("Class identifier."),
                        "date" to dateProperty("Local calendar date containing the class."),
                    ),
                ),
            ),
        ).associateBy(AiToolDefinition::name)

        fun tool(
            name: String,
            description: String,
            parameters: JsonObject,
        ): AiToolDefinition {
            return AiToolDefinition(
                name = name,
                description = description,
                parameters = parameters,
            )
        }

        fun classMutationSchema(idField: String?): JsonObject {
            val required = buildList {
                idField?.let(::add)
                add("date")
                if (idField == null) {
                    add("startTime")
                    add("endTime")
                    add("organizationId")
                }
            }
            val properties = buildList {
                idField?.let { field -> add(field to idProperty("Class identifier.")) }
                add("date" to dateProperty("Local calendar date."))
                add("startTime" to timeProperty("Local start time."))
                add("endTime" to timeProperty("Local end time."))
                add("subjectId" to idProperty("Existing subject identifier when known."))
                add("organizationId" to idProperty("Existing organization identifier when known."))
                add("employeeId" to idProperty("Existing teacher identifier when known."))
                add("customData" to stringProperty("Class name when no existing subject is selected."))
                add("office" to stringProperty("Room or office."))
                add("location" to stringProperty("Physical or online location."))
                add(
                    "eventType" to enumProperty(
                        description = "Class type.",
                        values = listOf(
                            "LESSON",
                            "LECTURE",
                            "PRACTICE",
                            "SEMINAR",
                            "CLASS",
                            "ONLINE_CLASS",
                            "WEBINAR",
                        ),
                    ),
                )
            }
            return objectSchema(required = required, properties = properties)
        }

        fun objectSchema(
            required: List<String> = emptyList(),
            properties: List<Pair<String, JsonObject>> = emptyList(),
        ): JsonObject {
            return buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    properties.forEach { (name, schema) -> put(name, schema) }
                }
                if (required.isNotEmpty()) {
                    putJsonArray("required") {
                        required.forEach { value -> add(JsonPrimitive(value)) }
                    }
                }
                put("additionalProperties", false)
            }
        }

        fun idProperty(description: String): JsonObject {
            return stringProperty(description = description, format = "uuid")
        }

        fun dateProperty(description: String): JsonObject {
            return stringProperty(description = description, format = "date")
        }

        fun dateTimeProperty(description: String): JsonObject {
            return stringProperty(description = description, format = "date-time")
        }

        fun timeProperty(description: String): JsonObject {
            return stringProperty(description = description, format = "time")
        }

        fun stringProperty(
            description: String,
            format: String? = null,
        ): JsonObject {
            return buildJsonObject {
                put("type", "string")
                put("description", description)
                put("minLength", 1)
                put("maxLength", 512)
                format?.let { value -> put("format", value) }
            }
        }

        fun enumProperty(
            description: String,
            values: List<String>,
        ): JsonObject {
            return buildJsonObject {
                put("type", "string")
                put("description", description)
                put("enum", buildJsonArray {
                    values.forEach { value -> add(JsonPrimitive(value)) }
                })
            }
        }

        fun integerProperty(
            description: String,
            minimum: Int,
            maximum: Int,
        ): JsonObject {
            return buildJsonObject {
                put("type", "integer")
                put("description", description)
                put("minimum", minimum)
                put("maximum", maximum)
            }
        }

        fun booleanProperty(description: String): JsonObject {
            return buildJsonObject {
                put("type", "boolean")
                put("description", description)
            }
        }
    }
}
