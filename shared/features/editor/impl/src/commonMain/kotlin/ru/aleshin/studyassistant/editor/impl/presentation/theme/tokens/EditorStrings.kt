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

package ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class EditorStrings(
    val scheduleEditorHeader: String,
    val classEditorHeader: String,
    val saveButtonTitle: String,
    val numberOfClassesLabel: String,
    val standardTimeIntervalTitle: String,
    val classesTitle: String,
    val breaksTitle: String,
    val scheduleIntervalsDialogHeader: String,
    val startOfClassesTitle: String,
    val startOfClassesPlaceholder: String,
    val exceptTitle: String,
    val allTitle: String,
    val addTitle: String,
    val quantityOfClassesTitle: String,
    val numberOfClassTitle: String,
    val durationTitle: String,
    val organizationFieldLabel: String,
    val organizationFieldPlaceholder: String,
    val organizationSelectorHeader: String,
    val organizationSelectorTitle: String,
    val eventTypeFieldLabel: String,
    val eventTypeFieldPlaceholder: String,
    val eventTypeSelectorHeader: String,
    val eventTypeSelectorTitle: String,
    val subjectFieldPlaceholder: String,
    val subjectSelectorHeader: String,
    val subjectSelectorTitle: String,
    val teacherFieldLabel: String,
    val teacherFieldPlaceholder: String,
    val teacherSelectorHeader: String,
    val teacherSelectorTitle: String,
    val locationFieldLabel: String,
    val locationFieldPlaceholder: String,
    val locationSelectorHeader: String,
    val locationSelectorTitle: String,
    val officeFieldLabel: String,
    val officeFieldPlaceholder: String,
    val officeSelectorHeader: String,
    val officeSelectorTitle: String,
    val startTimeFieldLabel: String,
    val startTimeFieldPlaceholder: String,
    val endTimeFieldLabel: String,
    val endTimeFieldPlaceholder: String,
    val notificationParamsLabel: String,
    val notificationParamsTitle: String,
    val notCollectedTimeData: String,
    val otherErrorMessage: String,
) {
    companion object {
        val RUSSIAN = EditorStrings(
            scheduleEditorHeader = "Редактор расписания",
            classEditorHeader = "Редактор событий",
            saveButtonTitle = "Сохранить",
            numberOfClassesLabel = "Количество занятий",
            standardTimeIntervalTitle = "Стандартные значения",
            classesTitle = "Занятия",
            breaksTitle = "Перерывы",
            scheduleIntervalsDialogHeader = "Стандартные значения",
            startOfClassesTitle = "Начало занятий",
            startOfClassesPlaceholder = "Введите нужное время",
            exceptTitle = "Кроме",
            allTitle = "Все",
            addTitle = "Добавить",
            quantityOfClassesTitle = "Кол-во:",
            numberOfClassTitle = "Номер",
            durationTitle = "Длительность",
            organizationFieldLabel = "Организация",
            organizationFieldPlaceholder = "Выберите необходимое",
            organizationSelectorHeader = "Организация",
            organizationSelectorTitle = "Выберите необходимую организацию",
            eventTypeFieldLabel = "Событие",
            eventTypeFieldPlaceholder = "Выберите тип предмета",
            eventTypeSelectorHeader = "Событие",
            eventTypeSelectorTitle = "Выберите тип предмета",
            subjectFieldPlaceholder = "Предмет",
            subjectSelectorHeader = "Предмет",
            subjectSelectorTitle = "Выберите необходимый предмет",
            teacherFieldLabel = "Преподаватель",
            teacherFieldPlaceholder = "Выберите преподавателя",
            teacherSelectorHeader = "Преподаватель",
            teacherSelectorTitle = "Выберите преподавателя занятия",
            locationFieldLabel = "Место",
            locationFieldPlaceholder = "Место проведения",
            locationSelectorHeader = "Место",
            locationSelectorTitle = "Выберите место проведения",
            officeFieldLabel = "Кабинет",
            officeFieldPlaceholder = "123",
            officeSelectorHeader = "Кабинет",
            officeSelectorTitle = "Выберите необходимый кабинет",
            startTimeFieldLabel = "Начало",
            startTimeFieldPlaceholder = "00:00",
            endTimeFieldLabel = "Конец",
            endTimeFieldPlaceholder = "23:59",
            notificationParamsTitle = "Особые уведомления",
            notificationParamsLabel = "Исключительно уведомлять о начале и конце события",
            notCollectedTimeData = "Не удалось собрать данные",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = EditorStrings(
            scheduleEditorHeader = "Schedule editor",
            classEditorHeader = "Event editor",
            saveButtonTitle = "Save",
            numberOfClassesLabel = "Number of classes",
            standardTimeIntervalTitle = "Standard values",
            classesTitle = "Classes",
            breaksTitle = "Breaks",
            scheduleIntervalsDialogHeader = "Standard values",
            startOfClassesTitle = "Beginning of classes",
            startOfClassesPlaceholder = "Enter the desired time",
            exceptTitle = "Except",
            allTitle = "All",
            addTitle = "Add",
            quantityOfClassesTitle = "Quantity:",
            numberOfClassTitle = "Number",
            durationTitle = "Duration",
            organizationFieldLabel = "Organization",
            organizationFieldPlaceholder = "Select the desired one",
            organizationSelectorHeader = "Organization",
            organizationSelectorTitle = "Select the desired organization",
            eventTypeFieldLabel = "Event",
            eventTypeFieldPlaceholder = "Select type of subject",
            eventTypeSelectorHeader = "Event",
            eventTypeSelectorTitle = "Select type of subject",
            subjectFieldPlaceholder = "Subject",
            subjectSelectorHeader = "Subject",
            subjectSelectorTitle = "Select the desired subject",
            teacherFieldLabel = "Teacher",
            teacherFieldPlaceholder = "Choose teacher",
            teacherSelectorHeader = "Teacher",
            teacherSelectorTitle = "Choose a class teacher",
            locationFieldLabel = "Location",
            locationFieldPlaceholder = "Choose event location",
            locationSelectorHeader = "Location",
            locationSelectorTitle = "Choose location of the event",
            officeFieldLabel = "Office",
            officeFieldPlaceholder = "123",
            officeSelectorHeader = "Office",
            officeSelectorTitle = "Select the desired cabinet",
            startTimeFieldLabel = "Start",
            startTimeFieldPlaceholder = "00:00",
            endTimeFieldLabel = "End",
            endTimeFieldPlaceholder = "23:59",
            notificationParamsTitle = "Special notifications",
            notificationParamsLabel = "Exclusively notify about the beginning and end of the event",
            notCollectedTimeData = "Data could not be collected",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalEditorStrings = staticCompositionLocalOf<EditorStrings> {
    error("Editor Strings is not provided")
}

internal fun fetchEditorStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> EditorStrings.ENGLISH
    StudyAssistantLanguage.RU -> EditorStrings.RUSSIAN
}