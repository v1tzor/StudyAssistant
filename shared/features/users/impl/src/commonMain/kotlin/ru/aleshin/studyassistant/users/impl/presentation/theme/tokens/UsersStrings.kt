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

package ru.aleshin.studyassistant.users.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class UsersStrings(
    val employeeProfileHeader: String,
    val requestsHeader: String,
    val employeePostLabel: String,
    val employeeWorkTimeLabel: String,
    val employeeBirthdayLabel: String,
    val employeeSubjectsHeader: String,
    val employeeContactInfoHeader: String,
    val employeePhoneTitle: String,
    val employeeEmailTitle: String,
    val employeeWebsiteTitle: String,
    val employeeLocationTitle: String,
    val noneSubjectsTitle: String,
    val noneContactInfoTitle: String,
    val friendsSearchBarPlaceholder: String,
    val friendRequestsSectionHeader: String,
    val showAllRequestsTitle: String,
    val receivedFriendRequestLabel: String,
    val sentFriendRequestLabel: String,
    val acceptFriendRequestLabel: String,
    val rejectFriendRequestLabel: String,
    val acceptRequestTitle: String,
    val rejectRequestTitle: String,
    val myFriendsSectionHeader: String,
    val deleteUserFromFriendsTitle: String,
    val notFoundUsersTitle: String,
    val searchUsersRequirementsTitle: String,
    val sendFriendRequestTitle: String,
    val cancelFriendRequestTitle: String,
    val userIsAlreadyFriendTitle: String,
    val noneFriendRequestsTitle: String,
    val noneFriendsTitle: String,
    val receivedRequestsTabTitle: String,
    val sentRequestsTabTitle: String,
    val otherErrorMessage: String,
) {

    companion object {
        val RUSSIAN = UsersStrings(
            employeeProfileHeader = "Сотрудник",
            requestsHeader = "Заявки в друзья",
            employeePostLabel = "Должность",
            employeeWorkTimeLabel = "График работы",
            employeeBirthdayLabel = "День рождения",
            employeeSubjectsHeader = "Предметы",
            employeeContactInfoHeader = "Связь и данные",
            employeePhoneTitle = "Телефон",
            employeeEmailTitle = "Email",
            employeeWebsiteTitle = "Сайт",
            employeeLocationTitle = "Место",
            noneSubjectsTitle = "Предметы отсутствуют",
            noneContactInfoTitle = "Информация отсутствует",
            friendsSearchBarPlaceholder = "Поиск друзей по коду",
            friendRequestsSectionHeader = "Заявки в друзья",
            showAllRequestsTitle = "Смотреть все",
            receivedFriendRequestLabel = "Хочет добавить вас в друзья",
            sentFriendRequestLabel = "Рассматривает вашу заявку в друзья",
            acceptFriendRequestLabel = "Пользователь был добавлен в друзья",
            rejectFriendRequestLabel = "Пользователь отклонил заявку в друзья",
            acceptRequestTitle = "Принять",
            rejectRequestTitle = "Отклонить",
            myFriendsSectionHeader = "Мои друзья",
            deleteUserFromFriendsTitle = "Удалить из друзей",
            notFoundUsersTitle = "Пользователь не найден",
            searchUsersRequirementsTitle = "Введите 7 значный код",
            sendFriendRequestTitle = "Отправить заявку",
            cancelFriendRequestTitle = "Отменить заявку",
            userIsAlreadyFriendTitle = "Пользователь уже у вас друзьях!",
            noneFriendsTitle = "Список друзей пуст",
            noneFriendRequestsTitle = "Заявки отсутствуют",
            receivedRequestsTabTitle = "Получено",
            sentRequestsTabTitle = "Отправлено",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = UsersStrings(
            employeeProfileHeader = "Employee",
            requestsHeader = "Friend requests",
            employeePostLabel = "Post",
            employeeWorkTimeLabel = "Work schedule",
            employeeBirthdayLabel = "Birthday",
            employeeSubjectsHeader = "Subjects",
            employeeContactInfoHeader = "Communication and data",
            employeePhoneTitle = "Phone",
            employeeEmailTitle = "Email",
            employeeWebsiteTitle = "Website",
            employeeLocationTitle = "Location",
            noneSubjectsTitle = "Subjects are missing",
            noneContactInfoTitle = "Information is missing",
            friendsSearchBarPlaceholder = "Search for friends by code",
            friendRequestsSectionHeader = "Friend requests",
            showAllRequestsTitle = "Show all",
            receivedFriendRequestLabel = "Would like to add you as a friend",
            sentFriendRequestLabel = "Considering your friend request",
            acceptFriendRequestLabel = "The user has been added as a friend",
            rejectFriendRequestLabel = "The user rejected the friend request",
            acceptRequestTitle = "Accept",
            rejectRequestTitle = "Reject",
            myFriendsSectionHeader = "My friends",
            deleteUserFromFriendsTitle = "Remove from friends",
            notFoundUsersTitle = "The user was not found",
            searchUsersRequirementsTitle = "Enter the 7 digit code",
            sendFriendRequestTitle = "Send a request",
            cancelFriendRequestTitle = "Cancel a request",
            userIsAlreadyFriendTitle = "The user is already your friend!",
            noneFriendsTitle = "The friends list is empty",
            noneFriendRequestsTitle = "Requests are missing",
            receivedRequestsTabTitle = "Received",
            sentRequestsTabTitle = "Sent",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalUsersStrings = staticCompositionLocalOf<UsersStrings> {
    error("Users Strings is not provided")
}

internal fun fetchUsersStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> UsersStrings.ENGLISH
    StudyAssistantLanguage.RU -> UsersStrings.RUSSIAN
}