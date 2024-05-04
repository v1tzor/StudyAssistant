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

package remote

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
object StudyAssistantFirestore {

    object Users {
        const val ROOT = "users"
        const val USERNAME = "username"
        const val CODE = "code"
    }

    object UserData {
        const val ROOT = "data"

        const val ORGANIZATIONS = "organizations"
        const val HOMEWORKS = "homeworks"
        const val BASE_SCHEDULES = "baseSchedules"
        const val CUSTOM_SCHEDULES = "customSchedules"
        const val CLASSES = "classes"
        const val SUBJECTS = "subjects"
        const val EMPLOYEE = "employee"

        const val SETTINGS = "settings"
        const val CALENDAR_SETTINGS = "calendar"

        const val UID = "uid"
        const val HOMEWORK_DATE = "date"
        const val ORGANIZATION_ID = "organization_id"
    }

    object LIMITS {
        const val ORGANIZATION_EMPLOYEE = 6L
        const val ORGANIZATION_SUBJECTS = 8L
    }
}