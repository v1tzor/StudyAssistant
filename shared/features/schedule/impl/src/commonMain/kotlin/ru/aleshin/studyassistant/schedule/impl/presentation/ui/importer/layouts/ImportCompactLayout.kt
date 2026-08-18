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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.layouts

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportLoadingSection
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportReviewSection
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportSourceSection
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportSuccessSection

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ImportCompactLayout(
    modifier: Modifier = Modifier,
    state: ImportState,
    onSelectPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onNoteChanged: (String) -> Unit,
    onOrganizationSelect: (OrganizationShortUi?) -> Unit,
    onAddOrganization: () -> Unit,
    onExtract: () -> Unit,
    onClassClick: (UID) -> Unit,
    onReorderDayClasses: (Int, Int, List<UID>) -> Unit,
    onSubjectClick: (UID) -> Unit,
    onTeacherClick: (UID) -> Unit,
    onAddSubject: () -> Unit,
    onAddTeacher: () -> Unit,
    onDone: () -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        when {
            state.isAnalysisInProgress -> ImportLoadingSection(
                modifier = Modifier.fillMaxSize(),
                startedAt = state.analysisStartedAt,
            )
            state.isApplied -> ImportSuccessSection(
                modifier = Modifier.widthIn(max = 600.dp),
                onDone = onDone,
            )
            state.session != null -> ImportReviewSection(
                modifier = Modifier.widthIn(max = 800.dp),
                state = state,
                onClassClick = onClassClick,
                onReorderDayClasses = onReorderDayClasses,
                onSubjectClick = onSubjectClick,
                onTeacherClick = onTeacherClick,
                onAddSubject = onAddSubject,
                onAddTeacher = onAddTeacher,
            )
            else -> ImportSourceSection(
                modifier = Modifier.widthIn(max = 600.dp),
                state = state,
                enabled = !state.isAnalysisInProgress,
                onSelectPhoto = onSelectPhoto,
                onTakePhoto = onTakePhoto,
                onNoteChanged = onNoteChanged,
                onOrganizationSelect = onOrganizationSelect,
                onAddOrganization = onAddOrganization,
                onExtract = onExtract,
            )
        }
    }
}
