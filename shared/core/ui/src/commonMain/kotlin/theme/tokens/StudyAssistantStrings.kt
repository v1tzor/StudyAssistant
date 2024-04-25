/*
 * Copyright 2023 Stanislav Aleshin
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
 * imitations under the License.
 */
package theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * @author Stanislav Aleshin on 27.01.2024.
 */
data class StudyAssistantStrings(
    val appName: String,
    val cancelTitle: String,
    val selectConfirmTitle: String,
    val createConfirmTitle: String,
    val changeConfirmTitle: String,
    val okConfirmTitle: String,
    val timePickerDialogHeader: String,
    val datePickerDialogHeader: String,
    val warningDialogTitle: String,
    val warningDeleteConfirmTitle: String,
    val minutesSymbol: String,
    val hoursSymbol: String,
    val separator: String,
    val amFormatTitle: String,
    val pmFormatTitle: String,
    val clearSearchBarDesk: String,
    val backTitle: String,
    val dateMenuTitle: String,
    val timeMenuTitle: String,
    val timeReset: String,
    val noneTitle: String,
    val warningGrantedPermissionMessage: String,
    val weekTitle: String,
    val monthTitle: String,
    val halfYearTitle: String,
    val yearTitle: String,
    val scheduleBottomItem: String,
    val tasksBottomItem: String,
    val infoBottomItem: String,
    val profileBottomItem: String,
    val userCodeLabel: String,
)

internal val russianFamelString = StudyAssistantStrings(
    appName = "Study Assistant",
    cancelTitle = "Отменить",
    selectConfirmTitle = "Выбрать",
    createConfirmTitle = "Создать",
    changeConfirmTitle = "Изменить",
    okConfirmTitle = "ОК",
    timePickerDialogHeader = "Выберите время",
    datePickerDialogHeader = "Выберите дату",
    warningDialogTitle = "Предупреждение!",
    warningDeleteConfirmTitle = "Удалить",
    minutesSymbol = "м",
    hoursSymbol = "ч",
    separator = ":",
    amFormatTitle = "AM",
    pmFormatTitle = "PM",
    clearSearchBarDesk = "Очистить поиск",
    backTitle = "Назад",
    dateMenuTitle = "Дата",
    timeMenuTitle = "Время",
    timeReset = "Сбросить время",
    noneTitle = "Нету",
    warningGrantedPermissionMessage = "Доступ к отправке оповещений запрещён!",
    weekTitle = "Неделя",
    monthTitle = "Месяц",
    halfYearTitle = "Пол года",
    yearTitle = "Год",
    scheduleBottomItem = "Расписание",
    tasksBottomItem = "Задания",
    infoBottomItem = "Информация",
    profileBottomItem = "Профиль",
    userCodeLabel = "Код: ",
)

internal val englishFamelString = StudyAssistantStrings(
    appName = "Study Assistant",
    cancelTitle = "Cancel",
    selectConfirmTitle = "Select",
    createConfirmTitle = "Create",
    changeConfirmTitle = "Change",
    okConfirmTitle = "OK",
    timePickerDialogHeader = "Selected time",
    datePickerDialogHeader = "Selected date",
    warningDialogTitle = "Warning!",
    warningDeleteConfirmTitle = "Delete",
    minutesSymbol = "m",
    hoursSymbol = "h",
    separator = ":",
    amFormatTitle = "AM",
    pmFormatTitle = "PM",
    clearSearchBarDesk = "Clear search",
    backTitle = "Back",
    dateMenuTitle = "Date",
    timeMenuTitle = "Time",
    timeReset = "Reset Time",
    noneTitle = "None",
    warningGrantedPermissionMessage = "Access to sending notifications is prohibited!",
    weekTitle = "Week",
    monthTitle = "Month",
    halfYearTitle = "Half year",
    yearTitle = "Year",
    scheduleBottomItem = "Schedule",
    tasksBottomItem = "Tasks",
    infoBottomItem = "Info",
    profileBottomItem = "Profile",
    userCodeLabel = "Code: ",
)

val LocalStudyAssistantStrings = staticCompositionLocalOf<StudyAssistantStrings> {
    error("Core Strings is not provided")
}

fun fetchCoreStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> englishFamelString
    StudyAssistantLanguage.RU -> russianFamelString
}
