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
object StudyAssistantAppwrite {

    object Client {
        const val ENDPOINT = "https://fra.cloud.appwrite.io/v1"
        const val ENDPOINT_REALTIME = "wss://fra.cloud.appwrite.io/v1/realtime"
        const val PROJECT_ID = "685aefd7003bf3aab9fc"
    }

    object Users {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "xxAGxrwB36eIWMKkUdElDAvWQzS2"

        const val FRIENDS = "friends"
        const val CODE = "code"

        const val ROOT = "users"
        const val UID = "uid"
    }

    object Organizations {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "68644e7f001f9d9f8bd2"
        const val USER_ID = "userId"
    }

    // NOT UPDATED

    object UserData {
        const val ROOT = "data"

        const val ORGANIZATIONS = "organizations"
        const val HOMEWORKS = "homeworks"
        const val GOALS = "goals"
        const val TODOS = "todos"
        const val BASE_SCHEDULES = "baseSchedules"
        const val CUSTOM_SCHEDULES = "customSchedules"
        const val SUBJECTS = "subjects"
        const val EMPLOYEE = "employee"

        const val SETTINGS = "settings"
        const val CALENDAR_SETTINGS = "calendar"
        const val NOTIFICATION_SETTINGS = "notification"

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
        const val GOAL_TARGET_DATA = "targetDate"
        const val GOAL_CONTENT_ID = "contentId"
        const val GOAL_DONE = "done"
        const val GOAL_COMPLETE_DATE = "completeDate"
        const val CLASS_SCHEDULE_ID = "scheduleId"
        const val CUSTOM_SCHEDULE_DATE = "date"
        const val WEEK = "week"
        const val DAY_OF_WEEK = "weekDayOfWeek"
        const val ORGANIZATION_ID = "organizationId"
        const val ORGANIZATION_AVATAR = "avatar"
        const val ORGANIZATION_HIDE = "hide"
        const val USER_AVATAR = "avatar"
    }

    object Requests {
        const val ROOT = "requests"
    }

    object SharedHomeworks {
        const val ROOT = "sharedHomeworks"
    }

    object SharedSchedules {
        const val ROOT = "sharedSchedules"
    }

    object Products {
        const val ROOT = "products"
    }

    object Storage {
        const val BUCKET = "68640fd3001e968f42d1"
    }

    object LIMITS {
        const val EDITOR_SUBJECTS = 50L
        const val EDITOR_EMPLOYEE = 50L
    }
}