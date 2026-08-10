/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.preview.impl.resources.Res
import ru.aleshin.studyassistant.preview.impl.resources.calendar_button_label
import ru.aleshin.studyassistant.preview.impl.resources.calendar_step_title
import ru.aleshin.studyassistant.preview.impl.resources.organization_button_label
import ru.aleshin.studyassistant.preview.impl.resources.organization_step_title
import ru.aleshin.studyassistant.preview.impl.resources.profile_button_label
import ru.aleshin.studyassistant.preview.impl.resources.profile_step_title
import ru.aleshin.studyassistant.preview.impl.resources.schedule_fill_out_button_label
import ru.aleshin.studyassistant.preview.impl.resources.schedule_step_title

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
internal enum class SetupPage : SetupPageData {
    PROFILE {
        override val id get() = 0
        override val stepTitle @Composable get() = stringResource(Res.string.profile_step_title)
        override val buttonLabel @Composable get() = stringResource(Res.string.profile_button_label)
    },
    ORGANIZATION {
        override val id get() = 1
        override val stepTitle @Composable get() = stringResource(Res.string.organization_step_title)
        override val buttonLabel @Composable get() = stringResource(Res.string.organization_button_label)
    },
    CALENDAR {
        override val id get() = 2
        override val stepTitle @Composable get() = stringResource(Res.string.calendar_step_title)
        override val buttonLabel @Composable get() = stringResource(Res.string.calendar_button_label)
    },
    SCHEDULE {
        override val id get() = 3
        override val stepTitle @Composable get() = stringResource(Res.string.schedule_step_title)
        override val buttonLabel @Composable get() = stringResource(Res.string.schedule_fill_out_button_label)
    };

    fun progress(): Float {
        return id.inc() / entries.size.toFloat()
    }

    companion object {
        fun previousPage(current: SetupPage) =
            SetupPage.entries.find { it.id == current.id - 1 } ?: current

        fun nextPage(current: SetupPage) =
            SetupPage.entries.find { it.id == current.id + 1 } ?: current
    }
}

@Immutable
internal interface SetupPageData {
    val id: Int
    val stepTitle: String @Composable get
    val buttonLabel: String @Composable get
}