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

package ru.aleshin.studyassistant.preview.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
internal data class PreviewStrings(
    val continueLabel: String,
    val backLabel: String,
    val studyIntroTitle: String,
    val studyIntroBody: String,
    val analyticsIntroTitle: String,
    val analyticsIntroBody: String,
    val organizationIntroTitle: String,
    val organizationIntroBody: String,
    val friendsIntroTitle: String,
    val friendsIntroBody: String,
    val loginLabel: String,
    val registerLabel: String,
    val otherErrorMessage: String,
) {
    companion object {
        val RUSSIAN = PreviewStrings(
            continueLabel = "Дальше",
            backLabel = "Назад",
            studyIntroTitle = "Быстро и просто отслеживайте ваше расписание",
            studyIntroBody = "На главном экране вы сможете увидеть расписание на текущий день, его ход выполнения, недельную загруженность, а также все привязанные задания к занятиям",
            analyticsIntroTitle = "Более эффективно выполняйте заданные задания",
            analyticsIntroBody = "Удобная система управления заданиями поможет вам более лучше визуализировать заданные задания, распределить всю нагрузку по дням недели и многое другое",
            organizationIntroTitle = "Создавайте разные организациями и вносите дневные изменения",
            organizationIntroBody = "Приложение поддерживает работу с несколькими учебными заведениями, а также возможность быстрого внесения дневных корректировок в постоянное расписание",
            friendsIntroTitle = "Находите друзей, делитесь заданиями и расписаниями",
            friendsIntroBody = "Войдите в аккаунт, чтобы начать взаимодействовать с другими людьми, а также обмениваться расписаниями и заданиями",
            loginLabel = "Войти в аккаунт",
            registerLabel = "Зарегестрироваться",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = PreviewStrings(
            continueLabel = "Next",
            backLabel = "Back",
            studyIntroTitle = "Quickly and easily track your schedule",
            studyIntroBody = "On the main screen, you can see the schedule for the current day, its progress, weekly workload, as well as all tasks",
            analyticsIntroTitle = "Perform assigned tasks more efficiently",
            analyticsIntroBody = "A convenient task management system will help you better visualize the assigned tasks, distribute the entire workload by day of the week and much more",
            organizationIntroTitle = "Create different organizations and make daily changes",
            organizationIntroBody = "The application supports working with several educational institutions and allows for quick daily adjustments to the permanent schedule",
            friendsIntroTitle = "Find friends, share tasks and schedules",
            friendsIntroBody = "Log in to your account to start interacting with other people, as well as sharing schedules and tasks",
            loginLabel = "Sign in to your account",
            registerLabel = "Register",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalPreviewStrings = staticCompositionLocalOf<PreviewStrings> {
    error("Preview Strings is not provided")
}

internal fun fetchPreviewStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> PreviewStrings.ENGLISH
    StudyAssistantLanguage.RU -> PreviewStrings.RUSSIAN
}