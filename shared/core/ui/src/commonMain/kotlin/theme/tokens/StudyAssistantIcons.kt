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
import org.jetbrains.compose.resources.DrawableResource
import studyassistant.shared.core.ui.generated.resources.Res
import studyassistant.shared.core.ui.generated.resources.ic_additional_organization
import studyassistant.shared.core.ui.generated.resources.ic_alert_circle
import studyassistant.shared.core.ui.generated.resources.ic_alert_circle_outline
import studyassistant.shared.core.ui.generated.resources.ic_birthday
import studyassistant.shared.core.ui.generated.resources.ic_book_study
import studyassistant.shared.core.ui.generated.resources.ic_calendar_today
import studyassistant.shared.core.ui.generated.resources.ic_church
import studyassistant.shared.core.ui.generated.resources.ic_class
import studyassistant.shared.core.ui.generated.resources.ic_classes_column
import studyassistant.shared.core.ui.generated.resources.ic_clock_outline
import studyassistant.shared.core.ui.generated.resources.ic_duration
import studyassistant.shared.core.ui.generated.resources.ic_email
import studyassistant.shared.core.ui.generated.resources.ic_employee
import studyassistant.shared.core.ui.generated.resources.ic_lecture
import studyassistant.shared.core.ui.generated.resources.ic_map_marker
import studyassistant.shared.core.ui.generated.resources.ic_movements
import studyassistant.shared.core.ui.generated.resources.ic_online_lesson
import studyassistant.shared.core.ui.generated.resources.ic_organization
import studyassistant.shared.core.ui.generated.resources.ic_organization_geo
import studyassistant.shared.core.ui.generated.resources.ic_organization_geo_outline
import studyassistant.shared.core.ui.generated.resources.ic_phone
import studyassistant.shared.core.ui.generated.resources.ic_practice
import studyassistant.shared.core.ui.generated.resources.ic_presentation
import studyassistant.shared.core.ui.generated.resources.ic_priority_high
import studyassistant.shared.core.ui.generated.resources.ic_profile
import studyassistant.shared.core.ui.generated.resources.ic_profile_outline
import studyassistant.shared.core.ui.generated.resources.ic_select_date
import studyassistant.shared.core.ui.generated.resources.ic_seminar
import studyassistant.shared.core.ui.generated.resources.ic_sorted_type
import studyassistant.shared.core.ui.generated.resources.ic_study
import studyassistant.shared.core.ui.generated.resources.ic_study_assistant
import studyassistant.shared.core.ui.generated.resources.ic_study_outline
import studyassistant.shared.core.ui.generated.resources.ic_tasks
import studyassistant.shared.core.ui.generated.resources.ic_tasks_circular
import studyassistant.shared.core.ui.generated.resources.ic_tasks_outline
import studyassistant.shared.core.ui.generated.resources.ic_university
import studyassistant.shared.core.ui.generated.resources.ic_upload_circular
import studyassistant.shared.core.ui.generated.resources.ic_web
import studyassistant.shared.core.ui.generated.resources.ic_webinar

/**
 * @author Stanislav Aleshin on 27.01.2024.
 */
data class StudyAssistantIcons(
    val logo: DrawableResource,
    val schedule: DrawableResource,
    val scheduleDisabled: DrawableResource,
    val tasks: DrawableResource,
    val tasksDisabled: DrawableResource,
    val information: DrawableResource,
    val informationDisabled: DrawableResource,
    val profile: DrawableResource,
    val profileDisabled: DrawableResource,
    val timeOutline: DrawableResource,
    val duration: DrawableResource,
    val organizationGeo: DrawableResource,
    val organization: DrawableResource,
    val upload: DrawableResource,
    val birthday: DrawableResource,
    val email: DrawableResource,
    val phone: DrawableResource,
    val website: DrawableResource,
    val selectDate: DrawableResource,
    val location: DrawableResource,
    val classes: DrawableResource,
    val lesson: DrawableResource,
    val lecture: DrawableResource,
    val practice: DrawableResource,
    val seminar: DrawableResource,
    val onlineClass: DrawableResource,
    val webinar: DrawableResource,
    val employee: DrawableResource,
    val homeworks: DrawableResource,
    val testsOutline: DrawableResource,
    val test: DrawableResource,
    val classesList: DrawableResource,
    val movements: DrawableResource,
    val tasksOutline: DrawableResource,
    val seminary: DrawableResource,
    val university: DrawableResource,
    val additionalEducation: DrawableResource,
    val sortedType: DrawableResource,
    val theoreticalTasks: DrawableResource,
    val practicalTasks: DrawableResource,
    val presentationTasks: DrawableResource,
    val calendarToday: DrawableResource,
    val priorityHigh: DrawableResource,
) {
    companion object {
        val BASE = StudyAssistantIcons(
            logo = Res.drawable.ic_study_assistant,
            schedule = Res.drawable.ic_study,
            scheduleDisabled = Res.drawable.ic_study_outline,
            tasks = Res.drawable.ic_tasks,
            tasksDisabled = Res.drawable.ic_tasks_outline,
            information = Res.drawable.ic_organization_geo,
            informationDisabled = Res.drawable.ic_organization_geo_outline,
            profile = Res.drawable.ic_profile,
            profileDisabled = Res.drawable.ic_profile_outline,
            timeOutline = Res.drawable.ic_clock_outline,
            duration = Res.drawable.ic_duration,
            organizationGeo = Res.drawable.ic_organization_geo,
            organization = Res.drawable.ic_organization,
            upload = Res.drawable.ic_upload_circular,
            birthday = Res.drawable.ic_birthday,
            email = Res.drawable.ic_email,
            phone = Res.drawable.ic_phone,
            website = Res.drawable.ic_web,
            selectDate = Res.drawable.ic_select_date,
            location = Res.drawable.ic_map_marker,
            classes = Res.drawable.ic_class,
            lesson = Res.drawable.ic_class,
            lecture = Res.drawable.ic_lecture,
            practice = Res.drawable.ic_practice,
            seminar = Res.drawable.ic_seminar,
            onlineClass = Res.drawable.ic_online_lesson,
            webinar = Res.drawable.ic_webinar,
            employee = Res.drawable.ic_employee,
            homeworks = Res.drawable.ic_tasks_circular,
            testsOutline = Res.drawable.ic_alert_circle_outline,
            test = Res.drawable.ic_alert_circle,
            classesList = Res.drawable.ic_classes_column,
            movements = Res.drawable.ic_movements,
            tasksOutline = Res.drawable.ic_tasks_outline,
            seminary = Res.drawable.ic_church,
            university = Res.drawable.ic_university,
            additionalEducation = Res.drawable.ic_additional_organization,
            sortedType = Res.drawable.ic_sorted_type,
            theoreticalTasks = Res.drawable.ic_book_study,
            practicalTasks = Res.drawable.ic_tasks_circular,
            presentationTasks = Res.drawable.ic_presentation,
            calendarToday = Res.drawable.ic_calendar_today,
            priorityHigh = Res.drawable.ic_priority_high,
        )
    }
}

val LocalStudyAssistantIcons = staticCompositionLocalOf<StudyAssistantIcons> {
    error("Core Icons is not provided")
}

fun fetchCoreIcons() = StudyAssistantIcons.BASE