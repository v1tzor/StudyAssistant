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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.views

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import ru.aleshin.studyassistant.preview.impl.resources.Res
import ru.aleshin.studyassistant.preview.impl.resources.ai_intro_body
import ru.aleshin.studyassistant.preview.impl.resources.ai_intro_highlight
import ru.aleshin.studyassistant.preview.impl.resources.ai_intro_title
import ru.aleshin.studyassistant.preview.impl.resources.analytics_intro_body
import ru.aleshin.studyassistant.preview.impl.resources.analytics_intro_title
import ru.aleshin.studyassistant.preview.impl.resources.il_analytics
import ru.aleshin.studyassistant.preview.impl.resources.il_organizations
import ru.aleshin.studyassistant.preview.impl.resources.il_sharing
import ru.aleshin.studyassistant.preview.impl.resources.il_study
import ru.aleshin.studyassistant.preview.impl.resources.sharing_intro_body
import ru.aleshin.studyassistant.preview.impl.resources.sharing_intro_title
import ru.aleshin.studyassistant.preview.impl.resources.study_intro_body
import ru.aleshin.studyassistant.preview.impl.resources.study_intro_title

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal enum class IntroPage(
    val headline: StringResource,
    val body: StringResource,
    val illustration: DrawableResource,
    val highlight: StringResource? = null,
) {
    STUDY(
        headline = Res.string.study_intro_title,
        body = Res.string.study_intro_body,
        illustration = Res.drawable.il_study,
    ),
    ANALYTICS(
        headline = Res.string.analytics_intro_title,
        body = Res.string.analytics_intro_body,
        illustration = Res.drawable.il_analytics,
    ),
    AGENT(
        headline = Res.string.ai_intro_title,
        body = Res.string.ai_intro_body,
        illustration = Res.drawable.il_organizations,
        highlight = Res.string.ai_intro_highlight,
    ),
    SHARING(
        headline = Res.string.sharing_intro_title,
        body = Res.string.sharing_intro_body,
        illustration = Res.drawable.il_sharing,
    ),
}
