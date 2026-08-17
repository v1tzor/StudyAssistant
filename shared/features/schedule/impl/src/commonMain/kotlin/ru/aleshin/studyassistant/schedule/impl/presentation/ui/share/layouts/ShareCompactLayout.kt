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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.layouts

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ShareStatus
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ShareInputSection
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ShareLoadingState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.SharePreviewSection
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ShareReadySection
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ShareResultState
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.other_error_message
import ru.aleshin.studyassistant.schedule.impl.resources.share_claimed_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_consumed_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_expired_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_import_success_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_invalid_title
import ru.aleshin.studyassistant.schedule.impl.resources.share_offline_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ShareCompactLayout(
    modifier: Modifier = Modifier,
    state: ShareState,
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
    Crossfade(
        modifier = modifier.animateContentSize(),
        targetState = state.status,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = Spring.DefaultDisplacementThreshold,
        ),
    ) { status ->
        when (status) {
            ShareStatus.INPUT -> ShareInputSection(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                state = state,
                onCodeChange = onCodeChange,
                onCreateClick = onCreateClick,
                onClaimClick = onClaimClick,
                onScanClick = onScanClick,
            )
            ShareStatus.READY -> ShareReadySection(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                state = state,
                centered = true,
                onCopyLinkClick = onCopyLinkClick,
                onResetClick = onResetClick,
            )
            ShareStatus.PREVIEW -> SharePreviewSection(
                state = state,
                horizontalPadding = 16.dp,
                onLinkOrganization = onLinkOrganization,
                onLinkSubjects = onLinkSubjects,
                onLinkTeachers = onLinkTeachers,
            )
            ShareStatus.LOADING, ShareStatus.IMPORTING -> ShareLoadingState()
            ShareStatus.SUCCESS -> ShareResultState(
                title = stringResource(Res.string.share_import_success_title),
                onResetClick = onResetClick,
            )
            ShareStatus.INVALID -> ShareResultState(
                title = stringResource(Res.string.share_invalid_title),
                onResetClick = onResetClick,
            )
            ShareStatus.EXPIRED -> ShareResultState(
                title = stringResource(Res.string.share_expired_title),
                onResetClick = onResetClick,
            )
            ShareStatus.CLAIMED -> ShareResultState(
                title = stringResource(Res.string.share_claimed_title),
                onResetClick = onResetClick,
            )
            ShareStatus.CONSUMED -> ShareResultState(
                title = stringResource(Res.string.share_consumed_title),
                onResetClick = onResetClick,
            )
            ShareStatus.OFFLINE -> ShareResultState(
                title = stringResource(Res.string.share_offline_title),
                onResetClick = onResetClick,
            )
            ShareStatus.ERROR -> ShareResultState(
                title = stringResource(Res.string.other_error_message),
                onResetClick = onResetClick,
            )
        }
    }
}
