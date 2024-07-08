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

package ru.aleshin.studyassistant.auth.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
internal data class AuthStrings(
    val otherSignInWayTitle: String,
    val emailLabel: String,
    val emailPlaceholder: String,
    val passwordLabel: String,
    val passwordPlaceholder: String,
    val usernameLabel: String,
    val usernamePlaceholder: String,
    val forgotPasswordLabel: String,
    val loginDesc: String,
    val loginHeadline: String,
    val loginLabel: String,
    val loginViaGoogleLabel: String,
    val notAccountLabelFirst: String,
    val notAccountLabelSecond: String,
    val alreadyHaveAccountLabelFirst: String,
    val alreadyHaveAccountLabelSecond: String,
    val alreadyHavePasswordLabelFirst: String,
    val alreadyHavePasswordLabelSecond: String,
    val registerDesc: String,
    val registerHeadline: String,
    val registerLabel: String,
    val forgotDesc: String,
    val forgotHeadline: String,
    val sendEmailLabel: String,
    val usernameLengthError: String,
    val emailFormatError: String,
    val passwordFormatError: String,
    val hidePasswordDesc: String,
    val showPasswordDesc: String,
    val authErrorMessage: String,
    val userNotFoundErrorMessage: String,
    val credentialsErrorMessage: String,
    val otherErrorMessage: String,
) {

    companion object {
        val RUSSIAN = AuthStrings(
            otherSignInWayTitle = "Или войдите через",
            emailLabel = "Электронная почта",
            emailPlaceholder = "email@example.com",
            passwordLabel = "Пароль",
            passwordPlaceholder = "Мин. 6 символов (a-Z)(0-9)",
            usernameLabel = "Отображаемое имя",
            usernamePlaceholder = "Имя Фамилия",
            forgotPasswordLabel = "Восстановить пароль",
            loginDesc = "Авторизация",
            loginHeadline = "С возвращением,\nв ваш ассистент!",
            loginLabel = "Войти",
            loginViaGoogleLabel = "Google",
            notAccountLabelFirst = "Нет аккаунта? ",
            notAccountLabelSecond = "Зарегистрируйтесь",
            alreadyHaveAccountLabelFirst = "Уже есть аккаунт? ",
            alreadyHaveAccountLabelSecond = "Войдите",
            alreadyHavePasswordLabelFirst = "Уже нашли пароль? ",
            alreadyHavePasswordLabelSecond = "Войдите",
            registerDesc = "Регистрация",
            registerHeadline = "Давайте\nначнём вместе!",
            registerLabel = "Зарегестрироваться",
            forgotDesc = "Восстановление пароля",
            forgotHeadline = "Давайте\nвосстановим\nваш пароль",
            sendEmailLabel = "Отправить письмо",
            emailFormatError = "* Неправильный формат ввода",
            passwordFormatError = "* Пароль должен содержать мин. 6 символов (a-Z)(0-9)",
            usernameLengthError = "* Никнейм должен содержать 2-15 символов (a-Z)",
            hidePasswordDesc = "Скрыть пароль",
            showPasswordDesc = "Показать пароль",
            authErrorMessage = "Неправильный логин или пароль!",
            userNotFoundErrorMessage = "Данные пользователя не найдены!",
            credentialsErrorMessage = "Данные пользователя, по-видимому, неверны или неполны!",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = AuthStrings(
            otherSignInWayTitle = "Or log in via",
            emailLabel = "Email",
            emailPlaceholder = "email@example.com",
            passwordLabel = "Password",
            passwordPlaceholder = "Min. 6 chars (a-Z)(0-9)",
            usernameLabel = "Display name",
            usernamePlaceholder = "First and last name",
            forgotPasswordLabel = "Recover password",
            loginDesc = "Authorization",
            loginHeadline = "Welcome back,\nto your assistant!",
            loginLabel = "Sign in",
            loginViaGoogleLabel = "Google",
            notAccountLabelFirst = "No account? ",
            notAccountLabelSecond = "Create one",
            alreadyHaveAccountLabelFirst = "Already have an account? ",
            alreadyHaveAccountLabelSecond = "Log in",
            alreadyHavePasswordLabelFirst = "Have you found the password yet? ",
            alreadyHavePasswordLabelSecond = "Log in",
            registerDesc = "Registration",
            registerHeadline = "Let's start\ntogether!",
            registerLabel = "Register",
            forgotDesc = "Password Recovery",
            forgotHeadline = "Let's restore\nyour\npassword",
            sendEmailLabel = "Send an email",
            emailFormatError = "* Incorrect input format",
            passwordFormatError = "* The password must contain at least 6 characters (a-Z)(0-9)",
            usernameLengthError = "* The nickname must contain 2-15 characters (a-Z)",
            hidePasswordDesc = "Hide password",
            showPasswordDesc = "Show password",
            authErrorMessage = "Incorrect username or password!",
            userNotFoundErrorMessage = "The user's data could not be found!",
            credentialsErrorMessage = "The user's data seems to be incorrect or incomplete!",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalAuthStrings = staticCompositionLocalOf<AuthStrings> {
    error("Auth Strings is not provided")
}

internal fun fetchAuthStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> AuthStrings.ENGLISH
    StudyAssistantLanguage.RU -> AuthStrings.RUSSIAN
}
