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
import studyassistant.shared.features.editor.impl.generated.resources.ic_circle_edit_outline
import studyassistant.shared.features.editor.impl.generated.resources.ic_clear_circular
import studyassistant.shared.features.editor.impl.generated.resources.ic_clock
import studyassistant.shared.features.editor.impl.generated.resources.ic_color
import studyassistant.shared.features.editor.impl.generated.resources.ic_employee_post
import studyassistant.shared.features.editor.impl.generated.resources.ic_notify
import studyassistant.shared.features.editor.impl.generated.resources.ic_number
import studyassistant.shared.features.editor.impl.generated.resources.ic_subject_name
import studyassistant.shared.features.editor.impl.generated.resources.ic_worktime

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@OptIn(ExperimentalResourceApi::class)
internal data class EditorIcons(
    val breaks: DrawableResource,
    val number: DrawableResource,
    val time: DrawableResource,
    val clearCircular: DrawableResource,
    val notification: DrawableResource,
    val name: DrawableResource,
    val color: DrawableResource,
    val pickColor: DrawableResource,
    val employeePost: DrawableResource,
    val workTime: DrawableResource,
) {
    companion object {
        val LIGHT = EditorIcons(
            breaks = Res.drawable.ic_break,
            number = Res.drawable.ic_number,
            time = Res.drawable.ic_clock,
            clearCircular = Res.drawable.ic_clear_circular,
            notification = Res.drawable.ic_notify,
            name = Res.drawable.ic_subject_name,
            color = Res.drawable.ic_color,
            pickColor = Res.drawable.ic_circle_edit_outline,
            employeePost = Res.drawable.ic_employee_post,
            workTime = Res.drawable.ic_worktime,
        )
        val DARK = EditorIcons(
            breaks = Res.drawable.ic_break,
            number = Res.drawable.ic_number,
            time = Res.drawable.ic_clock,
            clearCircular = Res.drawable.ic_clear_circular,
            notification = Res.drawable.ic_notify,
            name = Res.drawable.ic_subject_name,
            color = Res.drawable.ic_color,
            pickColor = Res.drawable.ic_circle_edit_outline,
            employeePost = Res.drawable.ic_employee_post,
            workTime = Res.drawable.ic_worktime,
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