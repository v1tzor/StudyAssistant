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
        const val COLLECTION_ID = "687a169b0033e3bb56fd"

        const val FRIENDS = "friends"
        const val CODE = "code"
        const val UID = "uid"
    }

    object Organizations {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "68644e7f001f9d9f8bd2"

        const val USER_ID = "userId"
        const val UID = "uid"
        const val HIDE = "hide"
        const val AVATAR_URL = "avatar"
    }

    object Products {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "6868f3600031044044e4"
    }

    object Requests {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "6868f6f6003c5a582e29"
    }

    object Todo {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a6c1200117f6ac3e4"

        const val UID = "uid"
        const val USER_ID = "userId"
        const val DEADLINE = "deadline"
        const val DONE = "done"
        const val COMPLETE_DATE = "completeDate"
    }

    object Homeworks {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a6db40026eb4d93e1"

        const val UID = "uid"
        const val USER_ID = "userId"
        const val CLASS_ID = "classId"
        const val DEADLINE = "deadline"
        const val DONE = "done"
        const val COMPLETE_DATE = "completeDate"
    }

    object Subjects {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a77e3001569389b53"

        const val ORGANIZATION_ID = "organizationId"
        const val USER_ID = "userId"
        const val TEACHER_ID = "teacherId"
        const val SUBJECT_NAME = "name"
    }

    object Employee {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a76030033a69f0c63"

        const val ORGANIZATION_ID = "organizationId"
        const val USER_ID = "userId"
        const val AVATAR_URL = "avatar"
    }

    object Goals {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a72270018a02b7f87"

        const val ORGANIZATION_ID = "organizationId"
        const val CONTENT_ID = "contentId"
        const val USER_ID = "userId"
        const val TARGET_DATE = "targetDate"
        const val DONE = "done"
        const val COMPLETE_DATE = "completeDate"
    }

    object CalendarSettings {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686bba840027eb8155c1"
    }

    object BaseSchedules {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a7916003790edf45b"

        const val USER_ID = "userId"
        const val VERSION_FROM = "dateVersionFrom"
        const val VERSION_TO = "dateVersionTo"
        const val WEEK = "week"
        const val DAY_OF_WEEK = "weekDayOfWeek"
    }

    object CustomSchedules {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a7a24002578efe97f"

        const val USER_ID = "userId"
        const val DATE = "date"
    }

    object SharedHomeworks {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a7b8c0027dd5f1cad"
    }

    object SharedSchedules {
        const val DATABASE_ID = "686052b2001b25f5a09f"
        const val COLLECTION_ID = "686a7c54002949d7a815"
    }

    object Storage {
        const val BUCKET = "68640fd3001e968f42d1"
    }

    object LIMITS {
        const val EDITOR_SUBJECTS = 50L
        const val EDITOR_EMPLOYEE = 50L
    }
}