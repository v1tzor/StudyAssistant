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
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames

/**
 * @author Stanislav Aleshin on 27.01.2024.
 */
data class StudyAssistantStrings(
    val appName: String,
    val cancelTitle: String,
    val addTitle: String,
    val selectConfirmTitle: String,
    val createConfirmTitle: String,
    val saveConfirmTitle: String,
    val changeConfirmTitle: String,
    val okConfirmTitle: String,
    val deleteConfirmTitle: String,
    val timePickerDialogHeader: String,
    val datePickerDialogHeader: String,
    val warningDialogTitle: String,
    val warningDeleteConfirmTitle: String,
    val minutesSuffix: String,
    val hoursSuffix: String,
    val separator: String,
    val amFormatTitle: String,
    val pmFormatTitle: String,
    val clearSearchBarDesk: String,
    val backTitle: String,
    val dateMenuTitle: String,
    val timeMenuTitle: String,
    val timeReset: String,
    val noneTitle: String,
    val absentTitle: String,
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
    val backIconDesc: String,
    val noneGender: String,
    val maleGender: String,
    val femaleGender: String,
    val oneWeekPlural: String,
    val twoWeekPlural: String,
    val threeWeekPlural: String,
    val januaryTitle: String,
    val februaryTitle: String,
    val marchTitle: String,
    val aprilTitle: String,
    val mayTitle: String,
    val juneTitle: String,
    val julyTitle: String,
    val augustTitle: String,
    val septemberTitle: String,
    val octoberTitle: String,
    val novemberTitle: String,
    val decemberTitle: String,
    val mondayTitle: String,
    val mondayShortTitle: String,
    val tuesdayTitle: String,
    val tuesdayShortTitle: String,
    val wednesdayTitle: String,
    val wednesdayShortTitle: String,
    val thursdayTitle: String,
    val thursdayShortTitle: String,
    val fridayTitle: String,
    val fridayShortTitle: String,
    val saturdayTitle: String,
    val saturdayShortTitle: String,
    val sundayTitle: String,
    val sundayShortTitle: String,
    val schoolOrganizationType: String,
    val lyceumOrganizationType: String,
    val seminaryOrganizationType: String,
    val gymnasiumOrganizationType: String,
    val collegeOrganizationType: String,
    val universityOrganizationType: String,
    val additionalEducationOrganizationType: String,
    val coursesOrganizationType: String,
    val pcsUnitSuffix: String,
    val specifyTitle: String,
    val hoursTitle: String,
    val minutesTitle: String,
    val eventTypeLesson: String,
    val eventTypeLecture: String,
    val eventTypePractice: String,
    val eventTypeSeminar: String,
    val eventTypeClass: String,
    val eventTypeOnlineClass: String,
    val eventTypeWebinar: String,
    val postEmployee: String,
    val postTeacher: String,
    val postDirector: String,
    val postMentor: String,
    val postTutor: String,
    val postManager: String,
    val notSelectedTitle: String,
    val avatarDesc: String,
    val contactInfoLabel: String,
    val contactInfoValue: String,
    val standardPriorityTitle: String,
    val mediumPriorityTitle: String,
    val highPriorityTitle: String,
    val theoreticalTasksTitle: String,
    val practicalTasksTitle: String,
    val presentationsTasksTitle: String,
    val noResultTitle: String,
    val todayTitle: String,
    val tomorrowTitle: String,
    val yesterdayTitle: String,
)

internal val russianFamelString = StudyAssistantStrings(
    appName = "Study Assistant",
    cancelTitle = "Отменить",
    addTitle = "Добавить",
    selectConfirmTitle = "Выбрать",
    createConfirmTitle = "Создать",
    saveConfirmTitle = "Сохранить",
    changeConfirmTitle = "Изменить",
    okConfirmTitle = "ОК",
    deleteConfirmTitle = "Удалить",
    timePickerDialogHeader = "Выберите время",
    datePickerDialogHeader = "Выберите дату",
    warningDialogTitle = "Предупреждение!",
    warningDeleteConfirmTitle = "Удалить",
    minutesSuffix = "м",
    hoursSuffix = "ч",
    separator = ":",
    amFormatTitle = "AM",
    pmFormatTitle = "PM",
    clearSearchBarDesk = "Очистить поиск",
    backTitle = "Назад",
    dateMenuTitle = "Дата",
    timeMenuTitle = "Время",
    timeReset = "Сбросить время",
    noneTitle = "Нету",
    absentTitle = "Отсутствует",
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
    backIconDesc = "Назад",
    noneGender = "Не выбран",
    maleGender = "Мужской",
    femaleGender = "Женский",
    oneWeekPlural = "1 неделя",
    twoWeekPlural = "2 недели",
    threeWeekPlural = "3 недели",
    januaryTitle = "Января",
    februaryTitle = "Февраля",
    marchTitle = "Марта",
    aprilTitle = "Апреля",
    mayTitle = "Мая",
    juneTitle = "Июня",
    julyTitle = "Июля",
    augustTitle = "Августа",
    septemberTitle = "Сентября",
    octoberTitle = "Октября",
    novemberTitle = "Ноября",
    decemberTitle = "Декабря",
    mondayTitle = "Понедельник",
    tuesdayTitle = "Вторник",
    wednesdayTitle = "Среда",
    thursdayTitle = "Четверг",
    fridayTitle = "Пятница",
    saturdayTitle = "Суббота",
    sundayTitle = "Воскресенье",
    mondayShortTitle = "Пон",
    tuesdayShortTitle = "Вто",
    wednesdayShortTitle = "Сре",
    thursdayShortTitle = "Чет",
    fridayShortTitle = "Пят",
    saturdayShortTitle = "Суб",
    sundayShortTitle = "Вос",
    schoolOrganizationType = "Школа",
    lyceumOrganizationType = "Лицей",
    seminaryOrganizationType = "Семинария",
    gymnasiumOrganizationType = "Гимназия",
    collegeOrganizationType = "Колледж",
    universityOrganizationType = "Университет",
    additionalEducationOrganizationType = "Доп. образование",
    coursesOrganizationType = "Образовательные курсы",
    pcsUnitSuffix = "шт.",
    specifyTitle = "Укажите",
    hoursTitle = "Часы",
    minutesTitle = "Минуты",
    eventTypeLesson = "Урок",
    eventTypeLecture = "Лекция",
    eventTypePractice = "Практика",
    eventTypeSeminar = "Семинар",
    eventTypeClass = "Занятие",
    eventTypeOnlineClass = "Онлайн занятие",
    eventTypeWebinar = "Вебинар",
    postEmployee = "Сотрудник",
    postTeacher = "Преподаватель",
    postDirector = "Директор",
    postMentor = "Наставник",
    postTutor = "Куратор",
    postManager = "Менеджер",
    notSelectedTitle = "Не выбрано",
    avatarDesc = "Выбрать фото профиля",
    contactInfoLabel = "Название",
    contactInfoValue = "Данные *",
    highPriorityTitle = "Очень важно",
    mediumPriorityTitle = "Важно",
    standardPriorityTitle = "Обычно",
    theoreticalTasksTitle = "Теория",
    practicalTasksTitle = "Практика",
    presentationsTasksTitle = "Доклад",
    noResultTitle = "Нет результатов",
    todayTitle = "Сегодня",
    tomorrowTitle = "Завтра",
    yesterdayTitle = "Вчера",
)

internal val englishFamelString = StudyAssistantStrings(
    appName = "Study Assistant",
    cancelTitle = "Cancel",
    addTitle = "Add",
    selectConfirmTitle = "Select",
    createConfirmTitle = "Create",
    saveConfirmTitle = "Save",
    changeConfirmTitle = "Change",
    okConfirmTitle = "OK",
    deleteConfirmTitle = "Delete",
    timePickerDialogHeader = "Selected time",
    datePickerDialogHeader = "Selected date",
    warningDialogTitle = "Warning!",
    warningDeleteConfirmTitle = "Delete",
    minutesSuffix = "m",
    hoursSuffix = "h",
    separator = ":",
    amFormatTitle = "AM",
    pmFormatTitle = "PM",
    clearSearchBarDesk = "Clear search",
    backTitle = "Back",
    dateMenuTitle = "Date",
    timeMenuTitle = "Time",
    timeReset = "Reset Time",
    noneTitle = "None",
    absentTitle = "Absent",
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
    backIconDesc = "Go back",
    noneGender = "Not stated",
    maleGender = "Male",
    femaleGender = "Female",
    oneWeekPlural = "1 week",
    twoWeekPlural = "2 week",
    threeWeekPlural = "3 week",
    januaryTitle = "January",
    februaryTitle = "February",
    marchTitle = "March",
    aprilTitle = "April",
    mayTitle = "May",
    juneTitle = "June",
    julyTitle = "July",
    augustTitle = "August",
    septemberTitle = "September",
    octoberTitle = "October",
    novemberTitle = "November",
    decemberTitle = "December",
    mondayTitle = "Monday",
    tuesdayTitle = "Tuesday",
    wednesdayTitle = "Wednesday",
    thursdayTitle = "Thursday",
    fridayTitle = "Friday",
    saturdayTitle = "Saturday",
    sundayTitle = "Sunday",
    mondayShortTitle = "Mon",
    tuesdayShortTitle = "Tue",
    wednesdayShortTitle = "Wed",
    thursdayShortTitle = "Thu",
    fridayShortTitle = "Fri",
    saturdayShortTitle = "Sat",
    sundayShortTitle = "Sun",
    schoolOrganizationType = "School",
    lyceumOrganizationType = "Lyceum",
    seminaryOrganizationType = "Seminary",
    gymnasiumOrganizationType = "Gymnasium",
    collegeOrganizationType = "College",
    universityOrganizationType = "University",
    additionalEducationOrganizationType = "Additional education",
    coursesOrganizationType = "Educational courses",
    pcsUnitSuffix = "pcs.",
    specifyTitle = "Specify",
    hoursTitle = "Hours",
    minutesTitle = "Minutes",
    eventTypeLesson = "Lesson",
    eventTypeLecture = "Lecture",
    eventTypePractice = "Practice",
    eventTypeSeminar = "Seminar",
    eventTypeClass = "Class",
    eventTypeOnlineClass = "Online class",
    eventTypeWebinar = "Webinar",
    postEmployee = "Employee",
    postTeacher = "Teacher",
    postDirector = "Director",
    postMentor = "Mentor",
    postTutor = "Tutor",
    postManager = "Manager",
    notSelectedTitle = "Not selected",
    avatarDesc = "Select profile photo",
    contactInfoLabel = "Name",
    contactInfoValue = "Data *",
    highPriorityTitle = "Very important",
    mediumPriorityTitle = "Important",
    standardPriorityTitle = "Usually",
    theoreticalTasksTitle = "Theory",
    practicalTasksTitle = "Practice",
    presentationsTasksTitle = "Presentation",
    noResultTitle = "No results",
    todayTitle = "Today",
    tomorrowTitle = "Tomorrow",
    yesterdayTitle = "Yesterday",
)

fun StudyAssistantStrings.monthNames() = MonthNames(
    january = januaryTitle,
    february = februaryTitle,
    march = marchTitle,
    april = aprilTitle,
    may = mayTitle,
    june = juneTitle,
    july = julyTitle,
    august = augustTitle,
    september = septemberTitle,
    october = octoberTitle,
    november = novemberTitle,
    december = decemberTitle,
)

fun StudyAssistantStrings.dayOfWeekNames() = DayOfWeekNames(
    monday = mondayTitle,
    tuesday = tuesdayTitle,
    wednesday = wednesdayTitle,
    thursday = thursdayTitle,
    friday = fridayTitle,
    saturday = saturdayTitle,
    sunday = sundayTitle,
)

fun StudyAssistantStrings.dayOfWeekShortNames() = DayOfWeekNames(
    monday = mondayShortTitle,
    tuesday = tuesdayShortTitle,
    wednesday = wednesdayShortTitle,
    thursday = thursdayShortTitle,
    friday = fridayShortTitle,
    saturday = saturdayShortTitle,
    sunday = sundayShortTitle,
)

val LocalStudyAssistantStrings = staticCompositionLocalOf<StudyAssistantStrings> {
    error("Core Strings is not provided")
}

fun fetchCoreStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> englishFamelString
    StudyAssistantLanguage.RU -> russianFamelString
}