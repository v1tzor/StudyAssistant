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

package ru.aleshin.studyassistant.core.common.functional

/**
 * @author Stanislav Aleshin on 09.02.2024.
 */
object Constants {

    object App {
        const val SPLASH_NAME = "STUDY\nASSISTANT"
        const val NAME = "Study Assistant"
        const val LOGGER_TAG = "study_assistant"
        const val DEVELOPER = "Stanislav Aleshin"
        const val LICENCE = "Apache Licence v2.0"
        const val PRIVACY_POLICY = "https://www.termsfeed.com/live/32b56f45-28c2-4942-89b8-f900516d2129"
        const val GITHUB_URI = "https://github.com/v1tzor/StudyAssistant"
        const val ISSUES_URI = "https://github.com/v1tzor/StudyAssistant/issues"
        const val PERMISSION_TAG = "Notification_Permission"
        const val PAY_DEEPLINK_SCHEME = "ru.aleshin.studyassistant.pay"
        const val OPEN_APP_DEEPLINK = "https://studyassistant-app.ru/"
        const val RECOVERY_PASSWORD_URL = "https://studyassistant-app.ru/recovery"
        const val VERIFY_EMAIL_URL = "https://studyassistant-app.ru/verify"
    }

    object Database {
        const val DATABASE_NAME = "study_assistant_main.db"
    }

    object Class {
        const val MAX_NUMBER = 15
    }

    object Regex {
        const val TEXT_AND_NUMBERS = "^[a-zA-Z0-9]+$"
        const val ONLY_TEXT = "^[\\p{L}\\p{M}\\p{Pd}.'’\\s]{1,50}$"
        const val NUMBERS = "\\d+"
        const val PASSWORD = "^[A-Za-z0-9\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\>\\=\\?\\@\\[\\]\\{\\}\\\\\\^\\_\\`\\~]+$"
    }

    object Text {
        const val MAX_PROFILE_DESC_LENGTH = 120
        const val SUBJECT_TEXT_LENGTH = 56
        const val DEFAULT_MAX_TEXT_LENGTH = 32
        const val FULL_ORG_NAME_LENGTH = 80
        const val USERNAME_LENGTH = 30
        const val TASK_MAX_LENGTH = 120
        const val TODO_MAX_LENGTH = 150
        const val TEST_TOPIC_MAX_LENGTH = 70
        const val MIN_PASSWORD_LENGTH = 6
        const val MIN_EMAIL_LENGTH = 5
        const val USER_CODE_LENGTH = 7
    }

    object Image {
        const val BYTE_TO_MB = 1024 * 1024
        const val AVATAR_MAX_SIZE_IN_BYTES = 1024 * 1024 * 5
    }

    object Notification {
        const val CHANNEL_ID = "studyAssistantAlarmChannel"
        const val CHANNEL_NAME = "Common"
    }

    object Alarm {
        const val ALARM_NOTIFICATION_ACTION = "ru.aleshin.ALARM_NOTIFICATION_ACTION"
    }

    object Placeholder {
        const val OVERVIEW_ITEMS = 8
        const val SCHEDULE_CLASSES_ITEMS = 3
        const val CONTACT_INFO = 2
        const val CHAT_MESSAGES = 5
        const val SHORT_EMPLOYEES = 9
        const val SHORT_SUBJECTS = 9
        const val LINK_SUBJECTS = 6
        const val LINK_EMPLOYEES = 5
        const val GOALS = 5
        const val EMPLOYEES_OR_SUBJECTS = 12
        const val HOMEWORKS = 5
        const val USER_CONTACT_INFO = 4
        const val OVERVIEW_TODOS = 6
        const val TODOS = 15
        const val OVERVIEW_FRIEND_REQUESTS = 3
        const val FULL_FRIEND_REQUESTS = 10
        const val FRIENDS = 10
        const val SOCIAL_NETWORKS = 4
        const val SHARED_HOMEWORKS = 4
        const val SHARED_HOMEWORK_SUBJECTS = 4
        const val PRODUCT = 2
    }

    object Delay {
        const val SPLASH_NAV = 1400L
        const val SPLASH_LOGO = 300L
        const val SPLASH_TEXT = 600L
        const val UPDATE_ACTIVE_CLASS = 5000L
        const val UPDATE_TASK_STATUS = 5000L
        const val UPDATE_EMAIL_VERIFICATION = 1000L
        const val PULL_REFRESH = 180L
    }

    object Animations {
        const val FADE_FAST = 500
        const val FADE_SLOW = 800
        const val FADE_DELAY = 200
        const val STANDARD_TWEEN = 300
    }

    object Date {
        const val DAY = 1
        const val DAYS_IN_WEEK = 7
        const val DAYS_IN_MONTH = 31
        const val DAYS_IN_HALF_YEAR = 183
        const val DAYS_IN_YEAR = 365
        const val MAX_DAYS_SHIFT = 730

        const val MAX_HOUR = 23
        const val MAX_MINUTE = 59

        const val EMPTY_DURATION = 0L
        const val MILLIS_IN_SECONDS = 1000L
        const val MILLIS_IN_MINUTE = 60000L
        const val MILLIS_IN_HOUR = 3600000L
        const val MILLIS_IN_DAY = 86400000L
        const val SECONDS_IN_MINUTE = 60L
        const val SECONDS_IN_YEAR = 31_536_000
        const val MINUTES_IN_MILLIS = 60000L
        const val MINUTES_IN_HOUR = 60L
        const val HOURS_IN_DAY = 24L

        const val OVERVIEW_FIRST_ITEM = 14
        const val OVERVIEW_NEXT_DAYS = 15
        const val OVERVIEW_PREVIOUS_DAYS = 14
    }

    object Window {
        const val SAFE_AREA_PX = 32
    }
}