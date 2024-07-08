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

package ru.aleshin.studyassistant.preview.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import studyassistant.shared.features.preview.impl.generated.resources.Res
import studyassistant.shared.features.preview.impl.generated.resources.ic_description
import studyassistant.shared.features.preview.impl.generated.resources.ic_gender
import studyassistant.shared.features.preview.impl.generated.resources.ic_textbox
import studyassistant.shared.features.preview.impl.generated.resources.il_analytics
import studyassistant.shared.features.preview.impl.generated.resources.il_analytics_dark
import studyassistant.shared.features.preview.impl.generated.resources.il_friends
import studyassistant.shared.features.preview.impl.generated.resources.il_friends_dark
import studyassistant.shared.features.preview.impl.generated.resources.il_organizations
import studyassistant.shared.features.preview.impl.generated.resources.il_organizations_dark
import studyassistant.shared.features.preview.impl.generated.resources.il_schedule
import studyassistant.shared.features.preview.impl.generated.resources.il_schedule_dark
import studyassistant.shared.features.preview.impl.generated.resources.il_study
import studyassistant.shared.features.preview.impl.generated.resources.il_study_dark

/**
 * @author Stanislav Aleshin on 14.06.2023.
 */
@OptIn(ExperimentalResourceApi::class)
internal data class PreviewIcons(
    val studyIllustration: DrawableResource,
    val organizationIllustration: DrawableResource,
    val analyticsIllustration: DrawableResource,
    val friendsIllustration: DrawableResource,
    val scheduleIllustration: DrawableResource,
    val name: DrawableResource,
    val description: DrawableResource,
    val gender: DrawableResource,
) {
    companion object {
        val LIGHT = PreviewIcons(
            studyIllustration = Res.drawable.il_study,
            organizationIllustration = Res.drawable.il_organizations,
            analyticsIllustration = Res.drawable.il_analytics,
            friendsIllustration = Res.drawable.il_friends,
            scheduleIllustration = Res.drawable.il_schedule,
            name = Res.drawable.ic_textbox,
            description = Res.drawable.ic_description,
            gender = Res.drawable.ic_gender,
        )
        val DARK = PreviewIcons(
            studyIllustration = Res.drawable.il_study_dark,
            organizationIllustration = Res.drawable.il_organizations_dark,
            analyticsIllustration = Res.drawable.il_analytics_dark,
            friendsIllustration = Res.drawable.il_friends_dark,
            scheduleIllustration = Res.drawable.il_schedule_dark,
            name = Res.drawable.ic_textbox,
            description = Res.drawable.ic_description,
            gender = Res.drawable.ic_gender,
        )
    }
}

internal val LocalPreviewIcons = staticCompositionLocalOf<PreviewIcons> {
    error("Preview Icons is not provided")
}

internal fun fetchPreviewIcons(isDark: Boolean) = when (isDark) {
    true -> PreviewIcons.DARK
    false -> PreviewIcons.LIGHT
}