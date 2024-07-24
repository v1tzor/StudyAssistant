/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.remote.datasources

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
object StudyAssistantFirestore {

    object Users {
        const val ROOT = "users"
        const val UID = "uid"
        const val CODE = "code"
        const val FRIENDS = "friends"
    }

    object UserData {
        const val ROOT = "data"

        const val ORGANIZATIONS = "organizations"
        const val HOMEWORKS = "homeworks"
        const val TODOS = "todos"
        const val BASE_SCHEDULES = "baseSchedules"
        const val CUSTOM_SCHEDULES = "customSchedules"
        const val SUBJECTS = "subjects"
        const val EMPLOYEE = "employee"

        const val SETTINGS = "settings"
        const val CALENDAR_SETTINGS = "calendar"

        const val UID = "uid"
        const val SCHEDULE_CLASSES = "classes"
        const val HOMEWORK_DEADLINE = "deadline"
        const val HOMEWORK_CLASS_ID = "classId"
        const val HOMEWORK_DONE = "done"
        const val HOMEWORK_COMPLETE_DATE = "completeDate"
        const val SUBJECT_TEACHER_ID = "teacherId"
        const val SUBJECT_NAME = "name"
        const val TODO_DEADLINE = "deadline"
        const val TODO_DONE = "done"
        const val TODO_COMPLETE_DATE = "completeDate"
        const val VERSION_FROM = "dateVersionFrom"
        const val VERSION_TO = "dateVersionTo"
        const val CLASS_SCHEDULE_ID = "scheduleId"
        const val CUSTOM_SCHEDULE_DATE = "date"
        const val WEEK = "week"
        const val DAY_OF_WEEK = "weekDayOfWeek"
        const val ORGANIZATION_ID = "organizationId"
    }

    object Requests {
        const val ROOT = "requests"
    }

    object SharedHomeworks {
        const val ROOT = "sharedHomeworks"
    }

    object LIMITS {
        const val EDITOR_SUBJECTS = 50L
        const val EDITOR_EMPLOYEE = 50L
    }
}