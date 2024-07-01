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

package ru.aleshin.studyassistant.schedule.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
internal data class ScheduleStrings(
    val overviewHeader: String,
    val detailsHeader: String,
    val currentWeekTitle: String,
    val previousWeekTitle: String,
    val nextWeekTitle: String,
    val commonScheduleViewType: String,
    val verticalScheduleViewType: String,
    val testLabel: String,
    val emptyClassesTitle: String,
    val untilEndClassTitle: String,
    val untilStartClassTitle: String,
    val homeworksProgressTitle: String,
    val tasksProgressTitle: String,
    val completeScheduleTitle: String,
    val analysisDayTitle: String,
    val analysisHomeworksLabel: String,
    val analysisTestsLabel: String,
    val analysisClassesLabel: String,
    val analysisMovementLabel: String,
    val analysisTasksLabel: String,
    val classSheetTaskTitle: String,
    val noneHomeworkTitle: String,
    val homeworkIsCompleteTitle: String,
    val homeworkIsCompleteShortTitle: String,
    val homeworkInProgressTitle: String,
    val homeworkInProgressShortTitle: String,
    val homeworkIsSetTitle: String,
    val homeworkIsSetShortTitle: String,
    val homeworkIsNotCompleteTitle: String,
    val homeworkIsNotCompleteShortTitle: String,
    val homeworkIsSkippedTitle: String,
    val homeworkIsSkippedShortTitle: String,
    val againHomeworkTitle: String,
    val completeHomeworkTitle: String,
    val addHomeworkTitle: String,
    val otherErrorMessage: String,
) {
    companion object {
        val RUSSIAN = ScheduleStrings(
            overviewHeader = "Обзор",
            detailsHeader = "Расписание",
            currentWeekTitle = "Текущая неделя",
            nextWeekTitle = "Следующая неделя",
            previousWeekTitle = "Предыдущая неделя",
            commonScheduleViewType = "Общий вид",
            verticalScheduleViewType = "Вертикальный вид",
            testLabel = "Тест",
            emptyClassesTitle = "Занятия отсутствуют",
            untilEndClassTitle = "До конца занятия",
            untilStartClassTitle = "До начала занятия",
            homeworksProgressTitle = "Выполнено домашних заданий",
            tasksProgressTitle = "Выполнено заданий",
            completeScheduleTitle = "Нету ближайших событий",
            analysisDayTitle = "Анализ дня",
            analysisHomeworksLabel = "Задания",
            analysisTestsLabel = "Тесты",
            analysisClassesLabel = "Занятия",
            analysisMovementLabel = "Перемещения",
            analysisTasksLabel = "Задачи",
            classSheetTaskTitle = "Задание",
            noneHomeworkTitle = "Задание отсутствует",
            homeworkIsCompleteTitle = "Выполнено",
            homeworkIsCompleteShortTitle = "ДЗ готово",
            homeworkInProgressTitle = "Выполняется ",
            homeworkInProgressShortTitle = "ДЗ в процессе",
            homeworkIsSetTitle = "Задание задано",
            homeworkIsSetShortTitle = "ДЗ задано",
            homeworkIsNotCompleteTitle = "Задание не выполнено",
            homeworkIsNotCompleteShortTitle = "ДЗ не выполнено",
            homeworkIsSkippedTitle = "Задание пропущено",
            homeworkIsSkippedShortTitle = "ДЗ пропущено",
            addHomeworkTitle = "Добавить задание",
            againHomeworkTitle = "Заново",
            completeHomeworkTitle = "Выполнить",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = ScheduleStrings(
            overviewHeader = "Overview",
            detailsHeader = "Schedule",
            currentWeekTitle = "Current week",
            nextWeekTitle = "Next week",
            previousWeekTitle = "Previous week",
            commonScheduleViewType = "General view",
            verticalScheduleViewType = "Vertical view",
            testLabel = "Test",
            emptyClassesTitle = "Classes are absent",
            untilEndClassTitle = "Until end of lesson",
            untilStartClassTitle = "Until start of lesson",
            tasksProgressTitle = "Completed tasks",
            homeworksProgressTitle = "Completed homeworks",
            completeScheduleTitle = "There are no upcoming events",
            analysisDayTitle = "Day analysis",
            analysisHomeworksLabel = "Homeworks",
            analysisTestsLabel = "Tests",
            analysisClassesLabel = "Classes",
            analysisMovementLabel = "Movements",
            analysisTasksLabel = "Tasks",
            classSheetTaskTitle = "Task",
            noneHomeworkTitle = "The task is missing",
            homeworkIsCompleteTitle = "Completed",
            homeworkIsCompleteShortTitle = "H.W. is complete",
            homeworkInProgressTitle = "In progress",
            homeworkInProgressShortTitle = "H.W. in progress",
            homeworkIsSetTitle = "The task is set",
            homeworkIsSetShortTitle = "H.W. is set",
            homeworkIsNotCompleteTitle = "The task not completed",
            homeworkIsNotCompleteShortTitle = "H.W. not completed",
            homeworkIsSkippedTitle = "Task was skipped",
            homeworkIsSkippedShortTitle = "H.W. skipped",
            addHomeworkTitle = "Add homework",
            againHomeworkTitle = "Again",
            completeHomeworkTitle = "Complete",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalScheduleStrings = staticCompositionLocalOf<ScheduleStrings> {
    error("Schedule Strings is not provided")
}

internal fun fetchScheduleStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> ScheduleStrings.ENGLISH
    StudyAssistantLanguage.RU -> ScheduleStrings.RUSSIAN
}