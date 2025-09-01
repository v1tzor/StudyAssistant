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

package ru.aleshin.studyassistant.profile.impl.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
@Immutable
internal data class ProfileStrings(
    val profileHeader: String,
    val editProfileDesc: String,
    val signOutDesc: String,
    val emailIsNotConfirmed: String,
    val friendsTitle: String,
    val aboutAppTitle: String,
    val generalSettingsTitle: String,
    val notifySettingsTitle: String,
    val calendarSettingsTitle: String,
    val paymentsSettingsTitle: String,
    val sharedSchedulesViewTitle: String,
    val openSentSchedulesButtonTitle: String,
    val sendScheduleButtonTitle: String,
    val receivedSchedulesSheetHeader: String,
    val sentSchedulesSheetHeader: String,
    val noneSharedSchedulesSheetTitle: String,
    val showSharedSchedulesButton: String,
    val cancelSentSharedSchedulesButton: String,
    val fromPrefix: String,
    val toPrefix: String,
    val agoSuffix: String,
    val choosingOrganizationsStepHeader: String,
    val choosingOrganizationsStepLabel: String,
    val selectOptionsStepHeader: String,
    val selectOptionsStepLabel: String,
    val sendAllSubjectsOptionTitle: String,
    val sendAllSubjectsOptionDescription: String,
    val sendAllEmployeesOptionTitle: String,
    val sendAllEmployeesOptionDescription: String,
    val specifyRecipientStepHeader: String,
    val specifyRecipientStepLabel: String,
    val nextStepButtonTitle: String,
    val previousStepButtonTitle: String,
    val otherErrorMessage: String,
) {

    companion object {
        val RUSSIAN = ProfileStrings(
            profileHeader = "Профиль",
            editProfileDesc = "Редактировать профиль",
            signOutDesc = "Выйти из аккаунта",
            emailIsNotConfirmed = "Email не подтверждён",
            friendsTitle = "Друзья",
            aboutAppTitle = "Информация\nо приложении",
            generalSettingsTitle = "Общие\nнастройки",
            notifySettingsTitle = "Уведомления и\nоповещения",
            calendarSettingsTitle = "Конфигурация\nкалендаря",
            paymentsSettingsTitle = "Подписка и \nплатные услуги",
            sharedSchedulesViewTitle = "Обмен расписанием",
            openSentSchedulesButtonTitle = "Открыть",
            sendScheduleButtonTitle = "Отправить",
            receivedSchedulesSheetHeader = "Полученные расписания",
            sentSchedulesSheetHeader = "Отправленные расписания",
            noneSharedSchedulesSheetTitle = "Расписания отсутствуют",
            showSharedSchedulesButton = "Показать",
            cancelSentSharedSchedulesButton = "Отменить",
            fromPrefix = "От:",
            toPrefix = "Кому:",
            agoSuffix = "назад",
            choosingOrganizationsStepHeader = "1. Выбор организаций",
            choosingOrganizationsStepLabel = "Выберите организации данные которых будут отправлены",
            selectOptionsStepHeader = "2. Параметры отправки",
            selectOptionsStepLabel = "Выберите необходимый вам вариант отправки",
            sendAllSubjectsOptionTitle = "Отправить все предметы",
            sendAllSubjectsOptionDescription = "Включите, если хотите отправить не только задействованные предметы, но и остальные",
            sendAllEmployeesOptionTitle = "Отправить всех сотрудников",
            sendAllEmployeesOptionDescription = "Включите эту опцию, если хотите поделиться всеми сотрудниками, независимо от содержания расписания",
            specifyRecipientStepHeader = "3. Укажите пользователей",
            specifyRecipientStepLabel = "Укажите с кем вы хотите поделиться расписанием",
            nextStepButtonTitle = "Продолжить",
            previousStepButtonTitle = "Назад",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = ProfileStrings(
            profileHeader = "Profile",
            editProfileDesc = "Edit profile",
            signOutDesc = "Sign out",
            emailIsNotConfirmed = "Email not confirmed",
            friendsTitle = "Friends",
            aboutAppTitle = "Application\ninformation",
            generalSettingsTitle = "General\nsettings",
            notifySettingsTitle = "Notifications and\nalerts",
            calendarSettingsTitle = "Calendar\nconfiguration",
            paymentsSettingsTitle = "Subscription and\npaid services",
            sharedSchedulesViewTitle = "Schedule sharing",
            openSentSchedulesButtonTitle = "Open",
            sendScheduleButtonTitle = "Send",
            receivedSchedulesSheetHeader = "Received schedules",
            sentSchedulesSheetHeader = "Sent schedules",
            noneSharedSchedulesSheetTitle = "Schedules is missing",
            showSharedSchedulesButton = "Show",
            cancelSentSharedSchedulesButton = "Cancel",
            fromPrefix = "From:",
            toPrefix = "To:",
            agoSuffix = "ago",
            choosingOrganizationsStepHeader = "1. Choosing organizations",
            choosingOrganizationsStepLabel = "Select the organizations whose data will be sent",
            selectOptionsStepHeader = "2. Sending Parameters",
            selectOptionsStepLabel = "Select the sending option you need",
            sendAllSubjectsOptionTitle = "Send all subjects",
            sendAllSubjectsOptionDescription = "Enable it If you want to send not only the subjects that are being used, but also other subjects as well",
            sendAllEmployeesOptionTitle = "Send all employees",
            sendAllEmployeesOptionDescription = "Enable this option if you want to share with all employees, regardless of the content of the schedule",
            specifyRecipientStepHeader = "3. Specify the users",
            specifyRecipientStepLabel = "Specify who you want to share the schedule with",
            nextStepButtonTitle = "Continue",
            previousStepButtonTitle = "Back",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalProfileStrings = staticCompositionLocalOf<ProfileStrings> {
    error("Profile Strings is not provided")
}

internal fun fetchProfileStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> ProfileStrings.ENGLISH
    StudyAssistantLanguage.RU -> ProfileStrings.RUSSIAN
}