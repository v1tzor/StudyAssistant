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
    val overviewHeader: String,
    val detailsHeader: String,
    val editorTitle: String,
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
    val otherErrorMessage: String,
) {

    companion object {
        val RUSSIAN = EditorStrings(
            overviewHeader = "Обзор",
            detailsHeader = "Расписание",
            editorTitle = "Редактор расписания",
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
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = EditorStrings(
            overviewHeader = "Overview",
            detailsHeader = "Schedule",
            editorTitle = "Schedule editor",
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