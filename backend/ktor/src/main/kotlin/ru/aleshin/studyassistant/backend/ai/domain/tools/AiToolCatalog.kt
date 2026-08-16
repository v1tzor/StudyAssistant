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
                    properties = listOf("teacherId" to idProperty("Target employee UUID.")),
                ),
            ),
            tool(
                name = "get_todos",
                description = "Read TODO items for an optional date range and completion state.",
                parameters = objectSchema(
                    properties = listOf(
                        "fromDate" to dateProperty("Inclusive start date (YYYY-MM-DD)."),
                        "toDate" to dateProperty("Inclusive end date (YYYY-MM-DD)."),
                        "status" to enumProperty(
                            description = "Completion filter.",
                            values = listOf("ALL", "ACTIVE", "COMPLETED"),
                        ),
                    ),
                ),
            ),
            tool(
                name = "get_homeworks",
                description = "Read homework in an inclusive date range (max 2 weeks).",
                parameters = objectSchema(
                    required = listOf("from", "to"),
                    properties = listOf(
                        "from" to dateProperty("Inclusive start date (YYYY-MM-DD)."),
                        "to" to dateProperty("Inclusive end date (YYYY-MM-DD)."),
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
                    properties = listOf("date" to dateProperty("Local calendar date (YYYY-MM-DD).")),
                ),
            ),
            tool(
                name = "get_classes_by_range",
                description = "Read classes in an inclusive local date range.",
                parameters = objectSchema(
                    required = listOf("fromDate", "toDate"),
                    properties = listOf(
                        "fromDate" to dateProperty("Inclusive start date (YYYY-MM-DD)."),
                        "toDate" to dateProperty("Inclusive end date (YYYY-MM-DD)."),
                    ),
                ),
            ),
            tool(
                name = "get_near_class",
                description = "Read the next scheduled class for one subject.",
                parameters = objectSchema(
                    required = listOf("subjectId"),
                    properties = listOf(
                        "subjectId" to idProperty("Subject UUID."),
                    ),
                ),
            ),
            tool(
                name = "get_goals",
                description = "Read all daily goals for one local calendar date.",
                parameters = objectSchema(
                    required = listOf("date"),
                    properties = listOf("date" to dateProperty("Local calendar date (YYYY-MM-DD).")),
                ),
            ),
            tool(
                name = "get_free_time",
                description = "Find intervals without scheduled classes on one date.",
                parameters = objectSchema(
                    required = listOf("date"),
                    properties = listOf(
                        "date" to dateProperty("Local calendar date (YYYY-MM-DD)."),
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
                description = "Propose a new TODO. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("name"),
                    properties = listOf(
                        "name" to stringProperty("Short actionable title."),
                        "description" to stringProperty("Optional details."),
                        "deadline" to dateTimeProperty("Optional local deadline (YYYY-MM-DDTHH:mm:ss)."),
                        "priority" to enumProperty(
                            description = "Optional priority.",
                            values = listOf("STANDARD", "MEDIUM", "HIGH"),
                        ),
                    ),
                ),
            ),
            tool(
                name = "update_todo",
                description = "Propose changes to an existing TODO. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("todoId"),
                    properties = listOf(
                        "todoId" to idProperty("Target TODO UUID."),
                        "name" to stringProperty("Replacement title."),
                        "description" to stringProperty("Replacement details."),
                        "deadline" to dateTimeProperty("Replacement local deadline (YYYY-MM-DDTHH:mm:ss)."),
                        "priority" to enumProperty(
                            description = "Replacement priority.",
                            values = listOf("STANDARD", "MEDIUM", "HIGH"),
                        ),
                    ),
                ),
            ),
            tool(
                name = "complete_todo",
                description = "Propose changing a TODO completion state. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("todoId", "completed"),
                    properties = listOf(
                        "todoId" to idProperty("Target TODO UUID."),
                        "completed" to booleanProperty("The requested completion state."),
                    ),
                ),
            ),
            tool(
                name = "delete_todo",
                description = "Propose deleting one TODO. Explicit confirmation required.",
                parameters = objectSchema(
                    required = listOf("todoId"),
                    properties = listOf("todoId" to idProperty("Target TODO UUID.")),
                ),
            ),
            tool(
                name = "create_homework",
                description = "Propose new homework. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("organizationId", "subjectId", "deadline"),
                    properties = listOf(
                        "organizationId" to idProperty("Existing organization UUID."),
                        "subjectId" to idProperty("Existing subject UUID."),
                        "classId" to idProperty("Optional related class UUID."),
                        "deadline" to dateProperty("Local deadline (YYYY-MM-DD)."),
                        "theoreticalTasks" to stringProperty("Theory tasks. Format: 'Label: task1, task2; Label: ...'"),
                        "practicalTasks" to stringProperty("Exercises. Format: 'Label: task1, task2; Label: ...'"),
                        "presentationTasks" to stringProperty("Projects. Format: 'Label: task1, task2; Label: ...'"),
                        "testTopic" to stringProperty("Test or exam topic."),
                    ),
                ),
            ),
            tool(
                name = "update_homework",
                description = "Propose changes to existing homework. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("homeworkId"),
                    properties = listOf(
                        "homeworkId" to idProperty("Target homework UUID."),
                        "organizationId" to idProperty("Replacement organization UUID."),
                        "subjectId" to idProperty("Replacement subject UUID."),
                        "classId" to idProperty("Replacement related class UUID."),
                        "deadline" to dateProperty("Replacement local deadline (YYYY-MM-DD)."),
                        "theoreticalTasks" to stringProperty("Replacement theory. Format: 'Label: task1, task2; Label: ...'"),
                        "practicalTasks" to stringProperty("Replacement exercises. Format: 'Label: task1, task2; Label: ...'"),
                        "presentationTasks" to stringProperty("Replacement projects. Format: 'Label: task1, task2; Label: ...'"),
                        "testTopic" to stringProperty("Replacement test topic."),
                        "priority" to enumProperty(
                            description = "Replacement priority.",
                            values = listOf("STANDARD", "MEDIUM", "HIGH"),
                        ),
                    ),
                ),
            ),
            tool(
                name = "complete_homework",
                description = "Propose changing homework completion state. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("homeworkId", "completed"),
                    properties = listOf(
                        "homeworkId" to idProperty("Target homework UUID."),
                        "completed" to booleanProperty("The requested completion state."),
                    ),
                ),
            ),
            tool(
                name = "delete_homework",
                description = "Propose deleting one homework item. Explicit confirmation required.",
                parameters = objectSchema(
                    required = listOf("homeworkId"),
                    properties = listOf("homeworkId" to idProperty("Target homework UUID.")),
                ),
            ),
            tool(
                name = "create_class",
                description = "Propose adding a class to the schedule. Requires confirmation.",
                parameters = classMutationSchema(idField = null),
            ),
            tool(
                name = "update_class",
                description = "Propose changing a scheduled class. Requires confirmation.",
                parameters = classMutationSchema(idField = "classId"),
            ),
            tool(
                name = "delete_class",
                description = "Propose deleting a scheduled class. Explicit confirmation required.",
                parameters = objectSchema(
                    required = listOf("classId", "date"),
                    properties = listOf(
                        "classId" to idProperty("Target class UUID."),
                        "date" to dateProperty("Local date containing the class (YYYY-MM-DD)."),
                    ),
                ),
            ),
            tool(
                name = "create_goal",
                description = "Propose a new daily goal. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("date", "contentType"),
                    properties = listOf(
                        "date" to dateProperty("Local calendar date (YYYY-MM-DD)."),
                        "contentType" to enumProperty(
                            description = "Type of linked content.",
                            values = listOf("HOMEWORK", "TODO"),
                        ),
                        "homeworkId" to idProperty("Linked homework UUID."),
                        "todoId" to idProperty("Linked TODO UUID."),
                        "desiredTime" to integerProperty(
                            description = "Desired duration in minutes.",
                            minimum = 1,
                            maximum = 1_440,
                        ),
                    ),
                ),
            ),
            tool(
                name = "update_goal",
                description = "Propose changes to an existing goal. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("goalId"),
                    properties = listOf(
                        "goalId" to idProperty("Target goal UUID."),
                        "desiredTime" to integerProperty(
                            description = "Replacement duration in minutes.",
                            minimum = 1,
                            maximum = 1_440,
                        ),
                    ),
                ),
            ),
            tool(
                name = "complete_goal",
                description = "Propose changing a goal completion state. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("goalId", "completed"),
                    properties = listOf(
                        "goalId" to idProperty("Target goal UUID."),
                        "completed" to booleanProperty("The requested completion state."),
                    ),
                ),
            ),
            tool(
                name = "delete_goal",
                description = "Propose deleting a daily goal. Explicit confirmation required.",
                parameters = objectSchema(
                    required = listOf("goalId"),
                    properties = listOf("goalId" to idProperty("Target goal UUID.")),
                ),
            ),
            tool(
                name = "create_subject",
                description = "Propose a new subject. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("organizationId", "name", "eventType"),
                    properties = listOf(
                        "organizationId" to idProperty("Existing organization UUID."),
                        "name" to stringProperty("Subject name."),
                        "eventType" to enumProperty(
                            description = "Default event type.",
                            values = listOf(
                                "LESSON", "LECTURE", "PRACTICE", "SEMINAR", "CLASS", "ONLINE_CLASS", "WEBINAR",
                            ),
                        ),
                        "teacherId" to idProperty("Optional teacher UUID."),
                        "office" to stringProperty("Optional default room/office."),
                        "location" to stringProperty("Optional location."),
                        "color" to enumProperty(
                            description = "Optional color accent.",
                            values = listOf(
                                "RED", "ORANGE", "YELLOW", "PISTACHIO", "LIME", "GREEN", "EMERALD",
                                "CYAN", "BLUE", "DARK_BLUE", "INDIGO", "LAVENDER", "LIGHT_PINK", "PINK", "FUCHSIA",
                            ),
                        ),
                    ),
                ),
            ),
            tool(
                name = "update_subject",
                description = "Propose changes to a subject. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("subjectId"),
                    properties = listOf(
                        "subjectId" to idProperty("Target subject UUID."),
                        "organizationId" to idProperty("Replacement organization UUID."),
                        "name" to stringProperty("Replacement name."),
                        "eventType" to enumProperty(
                            description = "Replacement event type.",
                            values = listOf(
                                "LESSON", "LECTURE", "PRACTICE", "SEMINAR", "CLASS", "ONLINE_CLASS", "WEBINAR",
                            ),
                        ),
                        "teacherId" to idProperty("Replacement teacher UUID."),
                        "office" to stringProperty("Replacement room/office."),
                        "location" to stringProperty("Replacement location."),
                        "color" to enumProperty(
                            description = "Replacement color accent.",
                            values = listOf(
                                "RED", "ORANGE", "YELLOW", "PISTACHIO", "LIME", "GREEN", "EMERALD",
                                "CYAN", "BLUE", "DARK_BLUE", "INDIGO", "LAVENDER", "LIGHT_PINK", "PINK", "FUCHSIA",
                            ),
                        ),
                    ),
                ),
            ),
            tool(
                name = "create_employee",
                description = "Propose a new employee. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("organizationId", "firstName", "post"),
                    properties = listOf(
                        "organizationId" to idProperty("Existing organization UUID."),
                        "firstName" to stringProperty("First name."),
                        "secondName" to stringProperty("Optional second name."),
                        "patronymic" to stringProperty("Optional patronymic."),
                        "post" to enumProperty(
                            description = "Position.",
                            values = listOf("EMPLOYEE", "TEACHER", "DIRECTOR", "MENTOR", "TUTOR", "MANAGER"),
                        ),
                        "birthday" to dateProperty("Optional birthday (YYYY-MM-DD)."),
                        "emails" to arrayProperty(stringProperty("Email.")),
                        "phones" to arrayProperty(stringProperty("Phone.")),
                        "locations" to arrayProperty(stringProperty("Address.")),
                        "webs" to arrayProperty(stringProperty("Website.")),
                    ),
                ),
            ),
            tool(
                name = "update_employee",
                description = "Propose changes to an employee. Requires confirmation.",
                parameters = objectSchema(
                    required = listOf("teacherId"),
                    properties = listOf(
                        "teacherId" to idProperty("Target employee UUID."),
                        "organizationId" to idProperty("Replacement organization UUID."),
                        "firstName" to stringProperty("Replacement first name."),
                        "secondName" to stringProperty("Replacement second name."),
                        "patronymic" to stringProperty("Replacement patronymic."),
                        "post" to enumProperty(
                            description = "Replacement position.",
                            values = listOf("EMPLOYEE", "TEACHER", "DIRECTOR", "MENTOR", "TUTOR", "MANAGER"),
                        ),
                        "birthday" to dateProperty("Replacement birthday (YYYY-MM-DD)."),
                        "emails" to arrayProperty(stringProperty("Email.")),
                        "phones" to arrayProperty(stringProperty("Phone.")),
                        "locations" to arrayProperty(stringProperty("Address.")),
                        "webs" to arrayProperty(stringProperty("Website.")),
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
                idField?.let { field -> add(field to idProperty("Target class UUID.")) }
                add("date" to dateProperty("Local date (YYYY-MM-DD)."))
                add("startTime" to timeProperty("Start time (HH:mm:ss)."))
                add("endTime" to timeProperty("End time (HH:mm:ss)."))
                add("subjectId" to idProperty("Optional subject UUID."))
                add("organizationId" to idProperty("Existing organization UUID."))
                add("employeeId" to idProperty("Optional teacher UUID."))
                add("customData" to stringProperty("Class name (if no subject selected)."))
                add("office" to stringProperty("Room/office."))
                add("location" to stringProperty("Physical/online location."))
                add(
                    "eventType" to enumProperty(
                        description = "Class type.",
                        values = listOf(
                            "LESSON", "LECTURE", "PRACTICE", "SEMINAR", "CLASS", "ONLINE_CLASS", "WEBINAR",
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

        fun arrayProperty(items: JsonObject): JsonObject {
            return buildJsonObject {
                put("type", "array")
                put("items", items)
            }
        }
    }
}
