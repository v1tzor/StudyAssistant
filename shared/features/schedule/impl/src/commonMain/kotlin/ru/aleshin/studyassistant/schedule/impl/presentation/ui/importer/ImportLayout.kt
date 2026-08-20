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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.layouts.ImportCompactLayout
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.layouts.ImportExpandedLayout

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
internal fun ImportLayout(
    modifier: Modifier = Modifier,
    state: ImportState,
    layoutMode: ImportLayoutMode,
    onSelectPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onNoteChanged: (String) -> Unit,
    onOrganizationSelect: (OrganizationShortUi?) -> Unit,
    onAddOrganization: () -> Unit,
    onExtract: () -> Unit,
    onCancelExtract: () -> Unit,
    onClassClick: (UID) -> Unit,
    onSubjectClick: (UID) -> Unit,
    onTeacherClick: (UID) -> Unit,
    onAddSubject: () -> Unit,
    onAddTeacher: () -> Unit,
    onAddClass: (Int, Int) -> Unit,
    onUpdateStartOfDay: (Int, Int, String) -> Unit,
    onUpdateClassesDuration: (Int, Int, Long, List<Pair<Int, Long>>) -> Unit,
    onUpdateBreaksDuration: (Int, Int, Long, List<Pair<Int, Long>>) -> Unit,
    onSwapDays: (Int, Int, Int) -> Unit,
    onDone: () -> Unit,
) {
    when (layoutMode) {
        ImportLayoutMode.COMPACT -> ImportCompactLayout(
            modifier = modifier,
            state = state,
            onSelectPhoto = onSelectPhoto,
            onTakePhoto = onTakePhoto,
            onNoteChanged = onNoteChanged,
            onOrganizationSelect = onOrganizationSelect,
            onAddOrganization = onAddOrganization,
            onExtract = onExtract,
            onCancelExtract = onCancelExtract,
            onClassClick = onClassClick,
            onSubjectClick = onSubjectClick,
            onTeacherClick = onTeacherClick,
            onAddSubject = onAddSubject,
            onAddTeacher = onAddTeacher,
            onAddClass = onAddClass,
            onUpdateStartOfDay = onUpdateStartOfDay,
            onUpdateClassesDuration = onUpdateClassesDuration,
            onUpdateBreaksDuration = onUpdateBreaksDuration,
            onSwapDays = onSwapDays,
            onDone = onDone,
        )
        ImportLayoutMode.EXPANDED -> ImportExpandedLayout(
            modifier = modifier,
            state = state,
            onSelectPhoto = onSelectPhoto,
            onTakePhoto = onTakePhoto,
            onNoteChanged = onNoteChanged,
            onOrganizationSelect = onOrganizationSelect,
            onAddOrganization = onAddOrganization,
            onExtract = onExtract,
            onCancelExtract = onCancelExtract,
            onClassClick = onClassClick,
            onSubjectClick = onSubjectClick,
            onTeacherClick = onTeacherClick,
            onAddSubject = onAddSubject,
            onAddTeacher = onAddTeacher,
            onAddClass = onAddClass,
            onUpdateStartOfDay = onUpdateStartOfDay,
            onUpdateClassesDuration = onUpdateClassesDuration,
            onUpdateBreaksDuration = onUpdateBreaksDuration,
            onSwapDays = onSwapDays,
            onDone = onDone,
        )
    }
}
