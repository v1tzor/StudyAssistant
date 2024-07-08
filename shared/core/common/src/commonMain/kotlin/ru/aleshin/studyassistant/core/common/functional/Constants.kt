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
        const val LOG = "study_assistant"
        const val DEVELOPER = "Stanislav Aleshin"
        const val LICENCE = "Apache Licence v2.0"
        const val GITHUB_URI = "https://github.com/v1tzor/StudyAssistant"
        const val ISSUES_URI = "https://github.com/v1tzor/StudyAssistant/issues"
        const val PERMISSION_TAG = "Notificatio_Permission"
        const val WEB_CLIENT_ID =
            "190379302885-gnrh3tua2lftfs7ke2uhnfj1vinbc8vm.apps.googleusercontent.com"
    }

    object Database {
        const val DATABASE_NAME = "study_assistant_main.db"
    }

    object Class {
        const val MAX_NUMBER = 15
    }

    object Regex {
        const val TEXT_AND_NUMBERS = "^[a-zA-Z0-9]+$"
        const val ONLY_TEXT = "^[a-zA-Z ]+$"
        const val NUMBERS = "\\d+"
        const val PASSWORD = "^[A-Za-z0-9\\!\\\"\\#\\$\\%\\&\\'\\(\\)\\*\\+\\,\\-\\.\\/\\:\\;\\<\\>\\=\\?\\@\\[\\]\\{\\}\\\\\\^\\_\\`\\~]+$"
    }

    object Text {
        const val MAX_PROFILE_DESC_LENGTH = 120
        const val SUBJECT_TEXT_LENGTH = 56
        const val DEFAULT_MAX_TEXT_LENGTH = 32
        const val USERNAME_LENGTH = 30
        const val TASK_MAX_LENGTH = 120
        const val TEST_TOPIC_MAX_LENGTH = 70
        const val MIN_PASSWORD_LENGTH = 6
        const val MIN_EMAIL_LENGTH = 5
    }

    object Notification {
        const val CHANNEL_ID = "studyAlarmChannel"
    }

    object Alarm {
        const val ALARM_NOTIFICATION_ACTION = "ru.aleshin.ALARM_NOTIFICATION_ACTION"
        const val NOTIFICATION_TIME_TYPE = "ALARM_DATA_TIME_TYPE"
        const val NOTIFICATION_CATEGORY = "ALARM_DATA_CATEGORY"
        const val NOTIFICATION_SUBCATEGORY = "ALARM_DATA_SUBCATEGORY"
        const val NOTIFICATION_ICON = "ALARM_DATA_ICON"
        const val APP_ICON = "ALARM_DATA_APP_ICON"
        const val REPEAT_TIME = "REPEAT_TIME"
        const val REPEAT_TYPE = "REPEAT_TYPE"
        const val TEMPLATE_ID = "REPEAT_TEMPLATE_ID"
        const val DAY_OF_MONTH = "REPEAT_DAY_OF_MONTH"
        const val WEEK_DAY = "REPEAT_WEEK_DAY"
        const val WEEK_NUMBER = "REPEAT_WEEK_NUMBER"
        const val MONTH = "REPEAT_MONTH"
    }

    object Placeholder {
        const val OVERVIEW_ITEMS = 8
        const val SCHEDULE_CLASSES_ITEMS = 3
        const val CONTACT_INFO = 2
        const val SHORT_EMPLOYEES = 9
        const val SHORT_SUBJECTS = 9
        const val EMPLOYEES_OR_SUBJECTS = 12
        const val HOMEWORKS = 7
        const val TODOS = 6
    }

    object Delay {
        const val SPLASH_NAV = 1800L
        const val SPLASH_LOGO = 300L
        const val SPLASH_TEXT = 600L
        const val UPDATE_ACTIVE_CLASS = 5000L
        const val UPDATE_TASK_STATUS = 5000L
        const val PULL_REFRESH = 180L
    }

    object Animations {
        const val FADE_FAST = 500
        const val FADE_SLOW = 800
        const val FADE_DELAY = 200
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
        const val MINUTES_IN_MILLIS = 60000L
        const val MINUTES_IN_HOUR = 60L
        const val HOURS_IN_DAY = 24L

        const val OVERVIEW_FIRST_ITEM = 14
        const val OVERVIEW_NEXT_DAYS = 15
        const val OVERVIEW_PREVIOUS_DAYS = 14

        const val MINUTES_FORMAT = "%s%s"
        const val HOURS_FORMAT = "%s%s"
        const val HOURS_AND_MINUTES_FORMAT = "%s%s %s%s"

        const val SHIFT_MINUTE_VALUE = 5
    }

    object Window {
        const val SAFE_AREA_PX = 32
    }
}