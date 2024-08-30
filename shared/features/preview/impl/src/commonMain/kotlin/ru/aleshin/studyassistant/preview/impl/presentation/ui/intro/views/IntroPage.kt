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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
@OptIn(ExperimentalResourceApi::class)
internal enum class IntroPage : IntroPageData {
    STUDY {
        override val id get() = 0
        override val headline @Composable get() = PreviewThemeRes.strings.studyIntroTitle
        override val body @Composable get() = PreviewThemeRes.strings.studyIntroBody
        override val illustration @Composable get() = PreviewThemeRes.icons.studyIllustration
    },
    ANALYTICS {
        override val id get() = 1
        override val headline @Composable get() = PreviewThemeRes.strings.analyticsIntroTitle
        override val body @Composable get() = PreviewThemeRes.strings.analyticsIntroBody
        override val illustration @Composable get() = PreviewThemeRes.icons.analyticsIllustration
    },
    ORGANIZATIONS {
        override val id get() = 2
        override val headline @Composable get() = PreviewThemeRes.strings.organizationIntroTitle
        override val body @Composable get() = PreviewThemeRes.strings.organizationIntroBody
        override val illustration @Composable get() = PreviewThemeRes.icons.organizationIllustration
    },
    FRIENDS {
        override val id get() = 3
        override val headline @Composable get() = PreviewThemeRes.strings.friendsIntroTitle
        override val body @Composable get() = PreviewThemeRes.strings.friendsIntroBody
        override val illustration @Composable get() = PreviewThemeRes.icons.friendsIllustration
    };

    companion object {
        fun fetchByIndex(index: Int) = entries.find { it.id == index }
    }
}

internal interface IntroPageData {
    val id: Int
    val headline: String @Composable get
    val body: String @Composable get
    val illustration: DrawableResource @Composable get
}