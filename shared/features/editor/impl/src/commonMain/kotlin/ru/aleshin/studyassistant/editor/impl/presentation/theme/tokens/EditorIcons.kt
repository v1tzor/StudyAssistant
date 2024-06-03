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

package ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import studyassistant.shared.features.editor.impl.generated.resources.Res
import studyassistant.shared.features.editor.impl.generated.resources.ic_break
import studyassistant.shared.features.editor.impl.generated.resources.ic_class
import studyassistant.shared.features.editor.impl.generated.resources.ic_clear_circular
import studyassistant.shared.features.editor.impl.generated.resources.ic_clock
import studyassistant.shared.features.editor.impl.generated.resources.ic_employee
import studyassistant.shared.features.editor.impl.generated.resources.ic_map_marker
import studyassistant.shared.features.editor.impl.generated.resources.ic_notify
import studyassistant.shared.features.editor.impl.generated.resources.ic_number
import studyassistant.shared.features.editor.impl.generated.resources.ic_organization

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@OptIn(ExperimentalResourceApi::class)
internal data class EditorIcons(
    val organization: DrawableResource,
    val classes: DrawableResource,
    val breaks: DrawableResource,
    val number: DrawableResource,
    val time: DrawableResource,
    val clearCircular: DrawableResource,
    val location: DrawableResource,
    val employee: DrawableResource,
    val notification: DrawableResource,
) {
    companion object {
        val LIGHT = EditorIcons(
            organization = Res.drawable.ic_organization,
            classes = Res.drawable.ic_class,
            breaks = Res.drawable.ic_break,
            number = Res.drawable.ic_number,
            time = Res.drawable.ic_clock,
            location = Res.drawable.ic_map_marker,
            clearCircular = Res.drawable.ic_clear_circular,
            employee = Res.drawable.ic_employee,
            notification = Res.drawable.ic_notify
        )
        val DARK = EditorIcons(
            organization = Res.drawable.ic_organization,
            classes = Res.drawable.ic_class,
            breaks = Res.drawable.ic_break,
            number = Res.drawable.ic_number,
            time = Res.drawable.ic_clock,
            location = Res.drawable.ic_map_marker,
            clearCircular = Res.drawable.ic_clear_circular,
            employee = Res.drawable.ic_employee,
            notification = Res.drawable.ic_notify
        )
    }
}

internal val LocalEditorIcons = staticCompositionLocalOf<EditorIcons> {
    error("Editor Icons is not provided")
}

internal fun fetchEditorIcons(isDark: Boolean) = when (isDark) {
    true -> EditorIcons.DARK
    false -> EditorIcons.LIGHT
}