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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.MediumInfoBadge
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.ReceivedMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.MediatedHomeworkLinkData
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareViewState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.MediatedHomeworksLinkerBottomSheet
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.NoneReceivedSharedHomeworksView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.NoneSentSharedHomeworksView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.ReceivedSharedHomeworksPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.ReceivedSharedHomeworksView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.SentSharedHomeworksPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.views.SentSharedHomeworksView

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
@Composable
internal fun ShareContent(
    state: ShareViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onOpenUserProfile: (AppUserUi) -> Unit,
    onLoadSubjects: (organizationId: UID) -> Unit,
    onAddSubject: (organizationId: UID) -> Unit,
    onLoadLinkData: (ReceivedMediatedHomeworksDetailsUi?) -> Unit,
    onUpdateLinkData: (MediatedHomeworkLinkData) -> Unit,
    onAcceptHomework: (ReceivedMediatedHomeworksDetailsUi, List<MediatedHomeworkLinkData>) -> Unit,
    onRejectHomework: (ReceivedMediatedHomeworksDetailsUi) -> Unit,
    onCancelSend: (SentMediatedHomeworksDetailsUi) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.padding(top = 8.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ReceivedTasksSection(
            isLoading = isLoading,
            isLoadingLink = isLoadingLink,
            currentTime = currentTime,
            organizations = organizations,
            receivedMediatedHomeworks = sharedHomeworks?.received ?: emptyList(),
            linkDataList = linkDataList,
            linkSchedule = linkSchedule,
            linkSubjects = linkSubjects,
            onOpenUserProfile = onOpenUserProfile,
            onLoadSubjects = onLoadSubjects,
            onAddSubject = onAddSubject,
            onLoadLinkData = onLoadLinkData,
            onAcceptHomework = onAcceptHomework,
            onUpdateLinkData = onUpdateLinkData,
            onRejectHomework = onRejectHomework,
        )
        HorizontalDivider()
        SentTasksSection(
            isLoading = isLoading,
            currentTime = currentTime,
            sentMediatedHomeworks = sharedHomeworks?.sent ?: emptyList(),
            onCancelSend = onCancelSend,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ReceivedTasksSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isLoadingLink: Boolean,
    currentTime: Instant,
    organizations: List<OrganizationShortUi>,
    receivedMediatedHomeworks: List<ReceivedMediatedHomeworksDetailsUi>,
    linkDataList: List<MediatedHomeworkLinkData>,
    linkSchedule: ScheduleUi?,
    linkSubjects: List<SubjectUi>,
    onOpenUserProfile: (AppUserUi) -> Unit,
    onAddSubject: (organizationId: UID) -> Unit,
    onLoadSubjects: (organizationId: UID) -> Unit,
    onLoadLinkData: (ReceivedMediatedHomeworksDetailsUi?) -> Unit,
    onUpdateLinkData: (MediatedHomeworkLinkData) -> Unit,
    onAcceptHomework: (ReceivedMediatedHomeworksDetailsUi, List<MediatedHomeworkLinkData>) -> Unit,
    onRejectHomework: (ReceivedMediatedHomeworksDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = TasksThemeRes.strings.receivedHomeworksHeader,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                MediumInfoBadge(
                    containerColor = StudyAssistantRes.colors.accents.orangeContainer,
                    contentColor = StudyAssistantRes.colors.accents.orange,
                ) {
                    Text(text = receivedMediatedHomeworks.size.toString())
                }
            }
        }
        Crossfade(
            modifier = modifier.animateContentSize(),
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { loading ->
            if (loading) {
                ReceivedSharedHomeworksPlaceholder()
            } else if (receivedMediatedHomeworks.isNotEmpty()) {
                val pagerState = rememberPagerState { receivedMediatedHomeworks.size }
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 8.dp,
                ) { homeworkIndex ->
                    receivedMediatedHomeworks[homeworkIndex].apply {
                        var isOpenMediatedHomeworksLinker by rememberSaveable { mutableStateOf(false) }

                        ReceivedSharedHomeworksView(
                            homeworks = homeworks,
                            targetDate = date,
                            sendDate = sendDate,
                            currentTime = currentTime,
                            sender = sender,
                            onOpenProfile = { onOpenUserProfile(sender) },
                            onAccept = { isOpenMediatedHomeworksLinker = true },
                            onReject = { onRejectHomework(this) },
                        )

                        if (isOpenMediatedHomeworksLinker) {
                            MediatedHomeworksLinkerBottomSheet(
                                isLoading = isLoadingLink,
                                organizations = organizations,
                                linkDataList = linkDataList,
                                linkSchedule = linkSchedule,
                                linkSubjects = linkSubjects,
                                onDismissRequest = {
                                    isOpenMediatedHomeworksLinker = false
                                    onLoadLinkData(null)
                                },
                                onLoadSubjects = onLoadSubjects,
                                onAddSubject = onAddSubject,
                                onUpdateLinkData = onUpdateLinkData,
                                onAdd = {
                                    onAcceptHomework(this, it)
                                    onLoadLinkData(null)
                                },
                            )
                            LaunchedEffect(true) {
                                if (linkDataList.isEmpty()) {
                                    onLoadLinkData(this@apply)
                                    val organization =
                                        organizations.find { it.isMain } ?: organizations.getOrNull(
                                            0
                                        )
                                    if (organization != null) onLoadSubjects(organization.uid)
                                }
                            }
                        }
                    }
                }
            } else {
                NoneReceivedSharedHomeworksView()
            }
        }
    }
}

@Composable
internal fun SentTasksSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentTime: Instant,
    sentMediatedHomeworks: List<SentMediatedHomeworksDetailsUi>,
    onCancelSend: (SentMediatedHomeworksDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = TasksThemeRes.strings.sentHomeworksHeader,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(
                visible = !isLoading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                MediumInfoBadge(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary,
                ) {
                    Text(text = sentMediatedHomeworks.size.toString())
                }
            }
        }
        Crossfade(
            modifier = modifier.animateContentSize(),
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { loading ->
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (loading) {
                    SentSharedHomeworksPlaceholder()
                    SentSharedHomeworksPlaceholder()
                } else if (sentMediatedHomeworks.isNotEmpty()) {
                    sentMediatedHomeworks.forEach { sentHomeworks ->
                        SentSharedHomeworksView(
                            homeworks = sentHomeworks.homeworks,
                            targetDate = sentHomeworks.date,
                            sendDate = sentHomeworks.sendDate,
                            currentTime = currentTime,
                            recipients = sentHomeworks.recipients,
                            onCancelSend = { onCancelSend(sentHomeworks) },
                        )
                    }
                } else {
                    NoneSentSharedHomeworksView()
                }
            }
        }
    }
}