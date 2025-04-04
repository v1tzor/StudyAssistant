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
    val todosHeader: String,
    val dailyGoalsSectionHeader: String,
    val homeworksTasksTab: String,
    val todosTasksTab: String,
    val shareHomeworksHeader: String,
    val shareHomeworksButtonTitle: String,
    val tasksProgressTomorrowLabel: String,
    val tasksProgressInTheWeekLabel: String,
    val todosProgressTitle: String,
    val todosProgressLabel: String,
    val homeworkErrorsViewHeader: String,
    val homeworkErrorsTitle: String,
    val homeworkExecutionAnalysisHeader: String,
    val comingHomeworksExecutionAnalysisTitle: String,
    val allHomeworksExecutionAnalysisTitle: String,
    val showHomeworkErrorsTitle: String,
    val showSharedHomeworksTitle: String,
    val scopeOfHomeworksHeader: String,
    val homeworksSectionHeader: String,
    val showAllHomeworksTitle: String,
    val homeworksSectionSubtitle: String,
    val noneTasksTitle: String,
    val noneTodosTitle: String,
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
    val receivedHomeworksHeader: String,
    val noneReceivedHomeworksTitle: String,
    val sentHomeworksHeader: String,
    val noneSentHomeworksTitle: String,
    val acceptHomeworkTitle: String,
    val rejectHomeworkTitle: String,
    val cancelSentHomeworkTitle: String,
    val agoSuffix: String,
    val applySharedHomeworksTitle: String,
    val homeworksLinkedHeader: String,
    val homeworksLinkedLabel: String,
    val targetUserSubjectsTitle: String,
    val currentUserSubjectsTitle: String,
    val specifySubjectLabel: String,
    val classesNotFoundLabel: String,
    val subjectSelectorHeader: String,
    val subjectSelectorTitle: String,
    val linkedClassSelectorHeader: String,
    val linkedClassSelectorTitle: String,
    val numberOfClassSuffix: String,
    val attachClassesLabel: String,
    val selectionSubjectStepHeader: String,
    val selectionSubjectStepLabel: String,
    val selectionSubjectStepAction: String,
    val specifyRecipientsStepHeader: String,
    val specifyRecipientsStepLabel: String,
    val specifyRecipientsStepAction: String,
    val currentTimeRangeDesc: String,
    val otherErrorMessage: String,
) {

    companion object {
        val RUSSIAN = TasksStrings(
            overviewHeader = "Обзор заданий",
            homeworksHeader = "Задания",
            todosHeader = "Задачи",
            dailyGoalsSectionHeader = "Дневные цели",
            homeworksTasksTab = "Домашние задгия",
            todosTasksTab = "Задачи",
            shareHomeworksHeader = "Обмен ДЗ",
            shareHomeworksButtonTitle = "Поделиться",
            tasksProgressTomorrowLabel = "На завтра",
            tasksProgressInTheWeekLabel = "На неделе",
            todosProgressTitle = "Задачи",
            todosProgressLabel = "Ожидают",
            homeworkErrorsViewHeader = "Контроль ошибок",
            homeworkErrorsTitle = "Ошибки",
            homeworkExecutionAnalysisHeader = "Аналитика выполенния",
            comingHomeworksExecutionAnalysisTitle = "Ближайшее время",
            allHomeworksExecutionAnalysisTitle = "За всё время",
            showHomeworkErrorsTitle = "Просмотреть",
            showSharedHomeworksTitle = "Перейти",
            scopeOfHomeworksHeader = "Объём заданий",
            homeworksSectionHeader = "Обзор домашних заданий",
            homeworksSectionSubtitle = "Текущая неделя",
            showAllHomeworksTitle = "Смотреть все",
            noneTasksTitle = "Задания отсутствуют",
            noneTodosTitle = "Задачи отсутствуют",
            theoreticalTasksBarName = "Теория",
            practicalTasksBarName = "Практика",
            presentationsTasksBarName = "Доклады",
            todosSectionHeader = "Активные задачи",
            totalTodosSuffix = "Всего",
            showAllTodosTitle = "Показать все",
            untilDeadlineDateSuffix = "До",
            overallHomeworksProgress = "Общий прогресс",
            homeworkTestLabel = "Тест",
            overdueHomeworksHeader = "Пропущенные задания",
            detachedActiveHomeworksHeader = "Активные открепленные задания",
            overdueTodosHeader = "Пропущенные задачи",
            noneErrorsTitle = "Ошибки отсутствуют",
            receivedHomeworksHeader = "Полученные задания",
            noneReceivedHomeworksTitle = "Задания отсутствуют",
            sentHomeworksHeader = "Отправленные задания",
            noneSentHomeworksTitle = "Задания отсутствуют",
            acceptHomeworkTitle = "Принять",
            rejectHomeworkTitle = "Отклонить",
            cancelSentHomeworkTitle = "Отменить",
            agoSuffix = "назад",
            applySharedHomeworksTitle = "Добавить",
            homeworksLinkedHeader = "Добавление заданий",
            homeworksLinkedLabel = "Свяжите предметы и занятия по названию",
            targetUserSubjectsTitle = "Пользователь",
            currentUserSubjectsTitle = "Вы",
            specifySubjectLabel = "Указать",
            classesNotFoundLabel = "Занятия не найдены",
            subjectSelectorHeader = "Предмет",
            subjectSelectorTitle = "Выберите соответствующий предмет",
            linkedClassSelectorHeader = "Занятие",
            linkedClassSelectorTitle = "Выберите соответствующее занятие",
            numberOfClassSuffix = "занятие",
            attachClassesLabel = "Прикрепите задание",
            selectionSubjectStepHeader = "1. Выбор предметов",
            selectionSubjectStepLabel = "Выберите предметы которыми хотите поделиться",
            selectionSubjectStepAction = "Продолжить",
            specifyRecipientsStepHeader = "2. Укажите пользователей",
            specifyRecipientsStepLabel = "Укажите с кем вы хотите поделиться заданием",
            specifyRecipientsStepAction = "Отправить",
            currentTimeRangeDesc = "Отобразить текущие даты",
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
        )
        val ENGLISH = TasksStrings(
            overviewHeader = "Task overview",
            homeworksHeader = "Tasks",
            todosHeader = "Todos",
            dailyGoalsSectionHeader = "Daily goals",
            homeworksTasksTab = "Homeworks",
            todosTasksTab = "Todos",
            shareHomeworksHeader = "Exchange H.W.",
            shareHomeworksButtonTitle = "Share",
            tasksProgressTomorrowLabel = "For tomorrow",
            tasksProgressInTheWeekLabel = "In the week",
            todosProgressTitle = "Todos",
            todosProgressLabel = "Waiting",
            homeworkErrorsViewHeader = "Error control",
            homeworkErrorsTitle = "Errors",
            showHomeworkErrorsTitle = "View",
            homeworkExecutionAnalysisHeader = "Execution analysis",
            comingHomeworksExecutionAnalysisTitle = "Coming time",
            allHomeworksExecutionAnalysisTitle = "For all the time",
            showSharedHomeworksTitle = "Go over",
            scopeOfHomeworksHeader = "Scope of tasks",
            homeworksSectionHeader = "Review of homeworks",
            homeworksSectionSubtitle = "Current week",
            showAllHomeworksTitle = "Show all",
            noneTasksTitle = "Tasks are missing",
            noneTodosTitle = "Todos is missing",
            theoreticalTasksBarName = "Theory",
            practicalTasksBarName = "Practice",
            presentationsTasksBarName = "Presentations",
            todosSectionHeader = "Active todos",
            totalTodosSuffix = "Total",
            showAllTodosTitle = "Show all",
            untilDeadlineDateSuffix = "Until",
            overallHomeworksProgress = "Overall progress",
            homeworkTestLabel = "Test",
            overdueHomeworksHeader = "Missed tasks",
            detachedActiveHomeworksHeader = "Active detached tasks",
            overdueTodosHeader = "Missed todos",
            noneErrorsTitle = "There are no errors",
            receivedHomeworksHeader = "Received tasks",
            noneReceivedHomeworksTitle = "Tasks are missing",
            sentHomeworksHeader = "Sent tasks",
            noneSentHomeworksTitle = "Tasks are missing",
            acceptHomeworkTitle = "Accept",
            rejectHomeworkTitle = "Reject",
            cancelSentHomeworkTitle = "Cancel",
            agoSuffix = "ago",
            applySharedHomeworksTitle = "Add",
            homeworksLinkedHeader = "Adding tasks",
            homeworksLinkedLabel = "Link subjects and classes by name",
            targetUserSubjectsTitle = "User",
            currentUserSubjectsTitle = "You",
            specifySubjectLabel = "Specify",
            classesNotFoundLabel = "Classes not found",
            subjectSelectorHeader = "Subject",
            subjectSelectorTitle = "Select the appropriate subject",
            linkedClassSelectorHeader = "Class",
            linkedClassSelectorTitle = "Select the appropriate class",
            numberOfClassSuffix = "class",
            attachClassesLabel = "Attach an class",
            selectionSubjectStepHeader = "1. Selection of subjects",
            selectionSubjectStepLabel = "Select the subjects you want to share",
            selectionSubjectStepAction = "Continue",
            specifyRecipientsStepHeader = "2. Specify the users",
            specifyRecipientsStepLabel = "Please specify who you would like to share this task with",
            specifyRecipientsStepAction = "Send",
            currentTimeRangeDesc = "Display current dates",
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