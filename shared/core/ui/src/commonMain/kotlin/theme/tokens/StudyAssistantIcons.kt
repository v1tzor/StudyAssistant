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
import org.jetbrains.compose.resources.ExperimentalResourceApi
import studyassistant.shared.core.ui.generated.resources.Res
import studyassistant.shared.core.ui.generated.resources.ic_duration
import studyassistant.shared.core.ui.generated.resources.ic_organization
import studyassistant.shared.core.ui.generated.resources.ic_organization_outline
import studyassistant.shared.core.ui.generated.resources.ic_profile
import studyassistant.shared.core.ui.generated.resources.ic_profile_outline
import studyassistant.shared.core.ui.generated.resources.ic_study
import studyassistant.shared.core.ui.generated.resources.ic_study_assistant
import studyassistant.shared.core.ui.generated.resources.ic_study_outline
import studyassistant.shared.core.ui.generated.resources.ic_tasks
import studyassistant.shared.core.ui.generated.resources.ic_tasks_outline
import studyassistant.shared.core.ui.generated.resources.ic_time_outline

/**
 * @author Stanislav Aleshin on 27.01.2024.
 */
@OptIn(ExperimentalResourceApi::class)
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
) {
    companion object {
        val BASE = StudyAssistantIcons(
            logo = Res.drawable.ic_study_assistant,
            schedule = Res.drawable.ic_study,
            scheduleDisabled = Res.drawable.ic_study_outline,
            tasks = Res.drawable.ic_tasks,
            tasksDisabled = Res.drawable.ic_tasks_outline,
            information = Res.drawable.ic_organization,
            informationDisabled = Res.drawable.ic_organization_outline,
            profile = Res.drawable.ic_profile,
            profileDisabled = Res.drawable.ic_profile_outline,
            timeOutline = Res.drawable.ic_time_outline,
            duration = Res.drawable.ic_duration,
        )
    }
}

val LocalStudyAssistantIcons = staticCompositionLocalOf<StudyAssistantIcons> {
    error("Core Icons is not provided")
}

fun fetchCoreIcons() = StudyAssistantIcons.BASE
