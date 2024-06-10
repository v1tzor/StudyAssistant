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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views

import androidx.compose.runtime.Composable
import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
@Parcelize
internal enum class SetupPage : SetupPageData {
    PROFILE {
        override val id get() = 0
        override val stepTitle @Composable get() = PreviewThemeRes.strings.profileStepTitle
        override val buttonLabel @Composable get() = PreviewThemeRes.strings.profileButtonLabel
    },
    ORGANIZATION {
        override val id get() = 1
        override val stepTitle @Composable get() = PreviewThemeRes.strings.organizationStepTitle
        override val buttonLabel @Composable get() = PreviewThemeRes.strings.organizationButtonLabel
    },
    CALENDAR {
        override val id get() = 2
        override val stepTitle @Composable get() = PreviewThemeRes.strings.calendarStepTitle
        override val buttonLabel @Composable get() = PreviewThemeRes.strings.calendarButtonLabel
    },
    SCHEDULE {
        override val id get() = 3
        override val stepTitle @Composable get() = PreviewThemeRes.strings.scheduleStepTitle
        override val buttonLabel @Composable get() = PreviewThemeRes.strings.scheduleFillOutButtonLabel
    };

    fun progress(): Float {
        return id.inc() / entries.size.toFloat()
    }

    companion object {
        fun previousPage(current: SetupPage) = SetupPage.entries.find { it.id == current.id - 1 } ?: current
        fun nextPage(current: SetupPage) = SetupPage.entries.find { it.id == current.id + 1 } ?: current
    }
}

interface SetupPageData : Parcelable {
    val id: Int
    val stepTitle: String @Composable get
    val buttonLabel: String @Composable get
}