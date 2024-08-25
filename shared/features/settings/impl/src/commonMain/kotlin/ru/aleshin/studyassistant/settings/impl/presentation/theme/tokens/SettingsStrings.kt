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

package ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
internal data class SettingsStrings(
    val settingsHeader: String,
    val generalTabHeader: String,
    val notificationsTabHeader: String,
    val calendarTabHeader: String,
    val subscriptionTabHeader: String,
    val languageChooserViewTitle: String,
    val themeChooserViewTitle: String,
    val numberOfRepeatWeekViewTitle: String,
    val beggingOfClassesNotifyTitle: String,
    val beggingOfClassesNotifyDescription: String,
    val endOfClassesNotifyTitle: String,
    val endOfClassesNotifyDescription: String,
    val beforeTimePrefix: String,
    val unfinishedHomeworksNotifyTitle: String,
    val unfinishedHomeworksNotifyDescription: String,
    val reminderTimePrefix: String,
    val reminderTimeDialogHeader: String,
    val highWorkloadWarningNotifyTitle: String,
    val highWorkloadWarningNotifyDescription: String,
    val exceptionOrganizationsDialogHeader: String,
    val exceptionOrganizationsDialogTitle: String,
    val addExceptionChipLabel: String,
    val exceptionsChipLabelPrefix: String,
    val otherErrorMessage: String,
) {

    companion object {
        val RUSSIAN = SettingsStrings(
            settingsHeader = "Настройки",
            generalTabHeader = "Общие",
            notificationsTabHeader = "Уведомления",
            calendarTabHeader = "Каленарь",
            subscriptionTabHeader = "Подписка и данные",
            languageChooserViewTitle = "Язык приложения",
            themeChooserViewTitle = "Тема",
            numberOfRepeatWeekViewTitle = "Недель в расписании",
            beggingOfClassesNotifyTitle = "Уведомлять о начале занятий",
            beggingOfClassesNotifyDescription = "Напоминания о начале занятий для выбранных организаций",
            endOfClassesNotifyTitle = "Уведомлять о конце занятий",
            endOfClassesNotifyDescription = "Оповещение когда закончатся занятия для выбранных организаций",
            beforeTimePrefix = "За ",
            unfinishedHomeworksNotifyTitle = "Напоминание о домашнем задании",
            unfinishedHomeworksNotifyDescription = "Вы будете получать напоминания о невыполненном домашнем задании, а также некоторые рекомендации",
            reminderTimePrefix = "В ",
            reminderTimeDialogHeader = "Время напоминаня",
            highWorkloadWarningNotifyTitle = "Предупреждение о высокой загруженности",
            highWorkloadWarningNotifyDescription = "Отправляется после анализа следующего дня при достижении критической отметки",
            exceptionOrganizationsDialogHeader = "Организации",
            exceptionOrganizationsDialogTitle = "Выберите организации для которых вы не хотите получать уведомления",
            addExceptionChipLabel = "Добавить исключение",
            exceptionsChipLabelPrefix = "Исключения: ",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = SettingsStrings(
            settingsHeader = "Settings",
            generalTabHeader = "General",
            notificationsTabHeader = "Notification",
            calendarTabHeader = "Calendar",
            subscriptionTabHeader = "Subscription and data",
            languageChooserViewTitle = "App language",
            themeChooserViewTitle = "Theme",
            numberOfRepeatWeekViewTitle = "Weeks in the schedule",
            beggingOfClassesNotifyTitle = "Notify about the start of classes",
            beggingOfClassesNotifyDescription = "Reminders about the start of classes for selected organizations",
            endOfClassesNotifyTitle = "Notify about the end of classes",
            endOfClassesNotifyDescription = "Notification when classes end for selected organizations",
            beforeTimePrefix = "In ",
            unfinishedHomeworksNotifyTitle = "Homework reminder",
            unfinishedHomeworksNotifyDescription = "You will receive reminders about homework that you have not finished, as well as some recommendations",
            reminderTimePrefix = "At ",
            reminderTimeDialogHeader = "Reminder time",
            highWorkloadWarningNotifyTitle = "Warning about the high workload",
            highWorkloadWarningNotifyDescription = "It is sent after the analysis of the next day when the critical point is reached",
            exceptionOrganizationsDialogHeader = "Organizations",
            exceptionOrganizationsDialogTitle = "Select the organizations for which you do not want to receive notifications",
            addExceptionChipLabel = "Add an exception",
            exceptionsChipLabelPrefix = "Exceptions: ",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalSettingsStrings = staticCompositionLocalOf<SettingsStrings> {
    error("Settings Strings is not provided")
}

internal fun fetchSettingsStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> SettingsStrings.ENGLISH
    StudyAssistantLanguage.RU -> SettingsStrings.RUSSIAN
}