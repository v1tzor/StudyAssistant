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

package ru.aleshin.studyassistant.tasks.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class TasksStrings(
    val overviewHeader: String,
    val homeworksHeader: String,
    val tasksHeader: String,
    val tasksProgressViewHeader: String,
    val tasksProgressTomorrowLabel: String,
    val tasksProgressInTheWeekLabel: String,
    val todosProgressTitle: String,
    val todosProgressLabel: String,
    val homeworkErrorsViewHeader: String,
    val homeworkErrorsTitle: String,
    val showHomeworkErrorsTitle: String,
    val homeworkAnalyticsHeader: String,
    val homeworksSectionHeader: String,
    val noneTasksTitle: String,
    val theoreticalTasksBarName: String,
    val practicalTasksBarName: String,
    val presentationsTasksBarName: String,
    val todosSectionHeader: String,
    val totalTodosSuffix: String,
    val showAllTodosTitle: String,
    val untilDeadlineDateSuffix: String,
    val overallHomeworksProgress: String,
    val homeworkTestLabel: String,
    val overdueHomeworksHeader: String,
    val detachedActiveHomeworksHeader: String,
    val overdueTodosHeader: String,
    val noneErrorsTitle: String,
    val otherErrorMessage: String,
) {
    companion object {
        val RUSSIAN = TasksStrings(
            overviewHeader = "Обзор заданий",
            homeworksHeader = "Задания",
            tasksHeader = "Задачи",
            tasksProgressViewHeader = "Выполнение",
            tasksProgressTomorrowLabel = "На завтра",
            tasksProgressInTheWeekLabel = "На неделе",
            todosProgressTitle = "Задачи",
            todosProgressLabel = "Ожидают",
            homeworkErrorsViewHeader = "Контроль ошибок",
            homeworkErrorsTitle = "Ошибки",
            showHomeworkErrorsTitle = "Просмотреть",
            homeworkAnalyticsHeader = "Объём заданий",
            homeworksSectionHeader = "Обзор домашних заданий",
            noneTasksTitle = "Нету заданий",
            theoreticalTasksBarName = "Теория",
            practicalTasksBarName = "Практика",
            presentationsTasksBarName = "Доклады",
            todosSectionHeader = "Задачи",
            totalTodosSuffix = "Всего",
            showAllTodosTitle = "Показать все",
            untilDeadlineDateSuffix = "До",
            overallHomeworksProgress = "Общий прогресс",
            homeworkTestLabel = "Тест",
            overdueHomeworksHeader = "Пропущенные задания",
            detachedActiveHomeworksHeader = "Активные открепленные задания",
            overdueTodosHeader = "Пропущенные задачи",
            noneErrorsTitle = "Ошибки отсутствуют",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = TasksStrings(
            overviewHeader = "Task overview",
            homeworksHeader = "Tasks",
            tasksHeader = "Todos",
            tasksProgressViewHeader = "Accomplishment",
            tasksProgressTomorrowLabel = "For tomorrow",
            tasksProgressInTheWeekLabel = "In the week",
            todosProgressTitle = "Todos",
            todosProgressLabel = "Waiting",
            homeworkErrorsViewHeader = "Error control",
            homeworkErrorsTitle = "Errors",
            showHomeworkErrorsTitle = "View",
            homeworkAnalyticsHeader = "Scope of tasks",
            homeworksSectionHeader = "Review of homeworks",
            noneTasksTitle = "There are no tasks",
            theoreticalTasksBarName = "Theory",
            practicalTasksBarName = "Practice",
            presentationsTasksBarName = "Presentations",
            todosSectionHeader = "Todos",
            totalTodosSuffix = "Total",
            showAllTodosTitle = "Show all",
            untilDeadlineDateSuffix = "Until",
            overallHomeworksProgress = "Overall progress",
            homeworkTestLabel = "Test",
            overdueHomeworksHeader = "Missed tasks",
            detachedActiveHomeworksHeader = "Active detached tasks",
            overdueTodosHeader = "Missed todos",
            noneErrorsTitle = "There are no errors",
            otherErrorMessage = "Error! Contact the developer!",
        )
    }
}

internal val LocalTasksStrings = staticCompositionLocalOf<TasksStrings> {
    error("Tasks Strings is not provided")
}

internal fun fetchTasksStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> TasksStrings.ENGLISH
    StudyAssistantLanguage.RU -> TasksStrings.RUSSIAN
}