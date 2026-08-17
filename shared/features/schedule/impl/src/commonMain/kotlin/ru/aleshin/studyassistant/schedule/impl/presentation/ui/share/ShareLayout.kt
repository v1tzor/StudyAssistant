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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.layouts.ShareCompactLayout
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.layouts.ShareExpandedLayout

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Composable
internal fun ShareLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
    layoutMode: ShareLayoutMode,
    onCodeChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onClaimClick: () -> Unit,
    onScanClick: () -> Unit,
    onCopyLinkClick: () -> Unit,
    onResetClick: () -> Unit,
    onLinkOrganization: (UID, UID?) -> Unit,
    onLinkSubjects: (UID, Map<UID, SubjectUi>) -> Unit,
    onLinkTeachers: (UID, Map<UID, EmployeeUi>) -> Unit,
) {
    when (layoutMode) {
        ShareLayoutMode.COMPACT -> ShareCompactLayout(
            modifier = modifier,
            state = state,
            onCodeChange = onCodeChange,
            onCreateClick = onCreateClick,
            onClaimClick = onClaimClick,
            onScanClick = onScanClick,
            onCopyLinkClick = onCopyLinkClick,
            onResetClick = onResetClick,
            onLinkOrganization = onLinkOrganization,
            onLinkSubjects = onLinkSubjects,
            onLinkTeachers = onLinkTeachers,
        )
        ShareLayoutMode.EXPANDED -> ShareExpandedLayout(
            modifier = modifier,
            state = state,
            onCodeChange = onCodeChange,
            onCreateClick = onCreateClick,
            onClaimClick = onClaimClick,
            onScanClick = onScanClick,
            onCopyLinkClick = onCopyLinkClick,
            onResetClick = onResetClick,
            onLinkOrganization = onLinkOrganization,
            onLinkSubjects = onLinkSubjects,
            onLinkTeachers = onLinkTeachers,
        )
    }
}
