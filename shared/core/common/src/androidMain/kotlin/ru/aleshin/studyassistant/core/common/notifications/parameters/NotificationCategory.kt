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

package ru.aleshin.studyassistant.core.common.notifications.parameters

import androidx.core.app.NotificationCompat

/**
 * @author Stanislav Aleshin on 25.08.2024.
 */
enum class NotificationCategory(val category: String) {
    CATEGORY_ALARM(NotificationCompat.CATEGORY_ALARM),
    CATEGORY_CALL(NotificationCompat.CATEGORY_CALL),
    CATEGORY_NAVIGATION(NotificationCompat.CATEGORY_NAVIGATION),
    CATEGORY_MESSAGE(NotificationCompat.CATEGORY_MESSAGE),
    CATEGORY_EMAIL(NotificationCompat.CATEGORY_EMAIL),
    CATEGORY_EVENT(NotificationCompat.CATEGORY_EVENT),
    CATEGORY_PROMO(NotificationCompat.CATEGORY_PROMO),
    CATEGORY_PROGRESS(NotificationCompat.CATEGORY_PROGRESS),
    CATEGORY_SOCIAL(NotificationCompat.CATEGORY_SOCIAL),
    CATEGORY_ERROR(NotificationCompat.CATEGORY_ERROR),
    CATEGORY_TRANSPORT(NotificationCompat.CATEGORY_TRANSPORT),
    CATEGORY_SYSTEM(NotificationCompat.CATEGORY_SYSTEM),
    CATEGORY_SERVICE(NotificationCompat.CATEGORY_SERVICE),
    CATEGORY_REMINDER(NotificationCompat.CATEGORY_REMINDER),
    CATEGORY_RECOMMENDATION(NotificationCompat.CATEGORY_RECOMMENDATION),
    CATEGORY_STATUS(NotificationCompat.CATEGORY_STATUS),
    CATEGORY_WORKOUT(NotificationCompat.CATEGORY_WORKOUT),
    CATEGORY_LOCATION_SHARING(NotificationCompat.CATEGORY_LOCATION_SHARING),
    CATEGORY_STOPWATCH(NotificationCompat.CATEGORY_STOPWATCH),
    CATEGORY_MISSED_CALL(NotificationCompat.CATEGORY_MISSED_CALL)
}