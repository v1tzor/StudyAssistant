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

package ru.aleshin.studyassistant.info.impl.presentation.ui.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class InfoStrings(
    val organizationsHeader: String,
    val newOrganizationBottomTitle: String,
    val noneOrganizationTitle: String,
    val addOrganizationTitle: String,
    val editOrganizationTitle: String,
    val organizationStatusLabel: String,
    val mainOrganizationStatus: String,
    val classesDurationInWeekLabel: String,
    val numberOfClassesInWeekLabel: String,
    val contactInfoSectionTitle: String,
    val emailTitle: String,
    val phoneTitle: String,
    val locationTitle: String,
    val websiteTitle: String,
    val employeesSectionTitle: String,
    val subjectsSectionTitle: String,
    val copyMessage: String,
    val noneEmployeeSubjectTitle: String,
    val subjectsSearchBarPlaceholder: String,
    val employeeSearchBarPlaceholder: String,
    val alphabeticSortedType: String,
    val teacherSortedType: String,
    val eventTypeSortedType: String,
    val officeSortedType: String,
    val locationSortedType: String,
    val organizationSelectorHeader: String,
    val organizationSelectorTitle: String,
    val subjectSortedTypeSelectorHeader: String,
    val subjectSortedTypeSelectorTitle: String,
    val otherErrorMessage: String,
) {
    companion object {
        val RUSSIAN = InfoStrings(
            organizationsHeader = "Организации",
            newOrganizationBottomTitle = "Новая организация",
            noneOrganizationTitle = "Добавьте организацию",
            addOrganizationTitle = "Добавить",
            editOrganizationTitle = "Редактировать",
            organizationStatusLabel = "Статус",
            mainOrganizationStatus = "Главная",
            classesDurationInWeekLabel = "Часов в неделю",
            numberOfClassesInWeekLabel = "Уроков в неделю",
            contactInfoSectionTitle = "Общая информация",
            emailTitle = "Email",
            phoneTitle = "Телефон",
            locationTitle = "Место",
            websiteTitle = "Сайт",
            employeesSectionTitle = "Сотрудники",
            subjectsSectionTitle = "Предметы",
            copyMessage = "Данные скопированы!",
            noneEmployeeSubjectTitle = "Занятия отсутствуют",
            employeeSearchBarPlaceholder = "Поиск преподавателей",
            subjectsSearchBarPlaceholder = "Поиск предметов",
            alphabeticSortedType = "По алфавиту",
            teacherSortedType = "По преподавателям",
            eventTypeSortedType = "По событиям",
            officeSortedType = "По кабинетам",
            locationSortedType = "По местам",
            organizationSelectorHeader = "Организация",
            organizationSelectorTitle = "Выберите необходимую организацию",
            subjectSortedTypeSelectorHeader = "Сортировка",
            subjectSortedTypeSelectorTitle = "Выберите тип сортировки",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = InfoStrings(
            organizationsHeader = "Organizations",
            newOrganizationBottomTitle = "New organization",
            noneOrganizationTitle = "Add an organization",
            addOrganizationTitle = "Add",
            editOrganizationTitle = "Edit",
            organizationStatusLabel = "Status",
            mainOrganizationStatus = "Main",
            classesDurationInWeekLabel = "Hours per week",
            numberOfClassesInWeekLabel = "Classes per week",
            contactInfoSectionTitle = "Contact info",
            emailTitle = "Email",
            phoneTitle = "Phone",
            locationTitle = "Location",
            websiteTitle = "Website",
            employeesSectionTitle = "Employees",
            subjectsSectionTitle = "Subjects",
            copyMessage = "The data has been successfully copied!",
            noneEmployeeSubjectTitle = "There are no classes",
            employeeSearchBarPlaceholder = "Search for teachers",
            subjectsSearchBarPlaceholder = "Search for subjects",
            alphabeticSortedType = "By alphabetical",
            teacherSortedType = "By teachers",
            eventTypeSortedType = "By events",
            officeSortedType = "By offices",
            locationSortedType = "By locations",
            organizationSelectorHeader = "Organization",
            organizationSelectorTitle = "Select the desired organization",
            subjectSortedTypeSelectorHeader = "Sorting",
            subjectSortedTypeSelectorTitle = "Select the sort type",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalInfoStrings = staticCompositionLocalOf<InfoStrings> {
    error("Info Strings is not provided")
}

internal fun fetchInfoStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> InfoStrings.ENGLISH
    StudyAssistantLanguage.RU -> InfoStrings.RUSSIAN
}