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

package ru.aleshin.studyassistant.chat.impl.domain.tools

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal enum class AiToolName(
    val wireName: String,
    val mutatesData: Boolean,
) {
    CREATE_TODO("create_todo", true),
    UPDATE_TODO("update_todo", true),
    COMPLETE_TODO("complete_todo", true),
    DELETE_TODO("delete_todo", true),
    CREATE_HOMEWORK("create_homework", true),
    UPDATE_HOMEWORK("update_homework", true),
    COMPLETE_HOMEWORK("complete_homework", true),
    DELETE_HOMEWORK("delete_homework", true),
    CREATE_CLASS("create_class", true),
    UPDATE_CLASS("update_class", true),
    DELETE_CLASS("delete_class", true),
    CREATE_GOAL("create_goal", true),
    UPDATE_GOAL("update_goal", true),
    COMPLETE_GOAL("complete_goal", true),
    DELETE_GOAL("delete_goal", true),
    CREATE_SUBJECT("create_subject", true),
    UPDATE_SUBJECT("update_subject", true),
    CREATE_EMPLOYEE("create_employee", true),
    UPDATE_EMPLOYEE("update_employee", true),
    GET_PROFILE("get_profile", false),
    GET_HOMEWORKS("get_homeworks", false),
    GET_OVERDUE_HOMEWORKS("get_overdue_homeworks", false),
    GET_TODOS("get_todos", false),
    GET_SUBJECTS("get_subjects", false),
    GET_EMPLOYEES("get_employees", false),
    GET_EMPLOYEE("get_employee", false),
    GET_ORGANIZATIONS("get_organizations", false),
    GET_CLASSES_BY_DATE("get_classes_by_date", false),
    GET_CLASSES_BY_RANGE("get_classes_by_range", false),
    GET_NEAR_CLASS("get_near_class", false),
    GET_FREE_TIME("get_free_time", false),
    GET_GOALS("get_goals", false);

    companion object {
        fun fromWireName(name: String): AiToolName? = entries.find { it.wireName == name }
        val supportedWireNames: List<String> = entries.map(AiToolName::wireName)
    }
}
