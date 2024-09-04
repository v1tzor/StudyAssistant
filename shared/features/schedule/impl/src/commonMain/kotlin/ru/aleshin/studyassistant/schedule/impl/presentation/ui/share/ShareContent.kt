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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.isoDayNumber
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.BaseScheduleUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.NumberOfWeekItem
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.ReceivedMediatedSchedulesUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.contract.ShareViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.OrganizationDataLinker
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.ScheduleWeekChip
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.SenderUserView
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.SenderUserViewPlaceholder
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.SharedScheduleView
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views.SharedScheduleViewPlaceholder

/**
 * @author Stanislav Aleshin on 16.08.2024
 */
@Composable
internal fun ShareContent(
    state: ShareViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onOpenProfile: (AppUserUi) -> Unit,
    onLinkOrganization: (sharedOrganization: UID, linkedOrganization: UID?) -> Unit,
    onLinkSubjects: (sharedOrganization: UID, subjects: Map<UID, SubjectUi>) -> Unit,
    onLinkTeachers: (sharedOrganization: UID, teachers: Map<UID, EmployeeUi>) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SenderSection(
            isLoading = isLoading,
            currentTime = currentTime,
            receivedMediatedSchedules = receivedMediatedSchedule,
            onOpenProfile = onOpenProfile,
        )
        SharedScheduleSection(
            isLoading = isLoading,
            linkedSchedules = linkedSchedules,
        )
        NewAndLinkedDataSectionDivider()
        OrganizationDataLinker(
            isLoading = isLoading,
            isLoadingLinkedOrganization = isLoadingLinkedOrganization,
            allOrganizations = allOrganizations,
            organizationsLinkData = organizationsLinkData,
            onLinkOrganization = onLinkOrganization,
            onLinkSubjects = onLinkSubjects,
            onLinkTeachers = onLinkTeachers,
        )
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
private fun SenderSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentTime: Instant,
    receivedMediatedSchedules: ReceivedMediatedSchedulesUi?,
    onOpenProfile: (AppUserUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = ScheduleThemeRes.strings.sharedScheduleSenderHeader,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = floatSpring(),
        ) { loading ->
            if (!loading && receivedMediatedSchedules != null) {
                SenderUserView(
                    onClick = { onOpenProfile(receivedMediatedSchedules.sender) },
                    username = receivedMediatedSchedules.sender.username,
                    email = receivedMediatedSchedules.sender.email,
                    avatar = receivedMediatedSchedules.sender.avatar,
                    leftTime = currentTime - receivedMediatedSchedules.sendDate,
                )
            } else {
                SenderUserViewPlaceholder()
            }
        }
    }
}

@Composable
private fun SharedScheduleSection(
    modifier: Modifier = Modifier,
    schedulesRowState: LazyListState = rememberLazyListState(),
    isLoading: Boolean,
    linkedSchedules: List<BaseScheduleUi>,
) {
    var numberOfWeek by rememberSaveable { mutableStateOf(NumberOfWeekItem.ONE.isoWeekNumber) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = ScheduleThemeRes.strings.sharedScheduleHeader,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Spacer(modifier = Modifier.weight(1f))
            ScheduleWeekChip(
                selected = NumberOfWeekItem.valueOf(numberOfWeek),
                maxNumberOfWeek = linkedSchedules.maxOfOrNull { it.week.isoRepeatWeekNumber } ?: 1,
                onSelect = {
                    numberOfWeek = it.isoWeekNumber
                    coroutineScope.launch { schedulesRowState.animateScrollToItem(0) }
                },
            )
        }
        Crossfade(
            targetState = isLoading,
            animationSpec = floatSpring(),
        ) { loading ->
            if (!loading) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    state = schedulesRowState,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(DayOfWeek.entries, key = { it.isoDayNumber }) { dayOfWeek ->
                        val schedule = linkedSchedules.find { schedule ->
                            schedule.dayOfWeek == dayOfWeek && schedule.week.isoRepeatWeekNumber == numberOfWeek
                        }
                        SharedScheduleView(
                            dayOfWeek = dayOfWeek,
                            classes = schedule?.classes ?: emptyList(),
                        )
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    userScrollEnabled = false,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    items(DayOfWeek.entries, key = { it.isoDayNumber }) {
                        SharedScheduleViewPlaceholder()
                    }
                }
            }
        }
    }
}

@Composable
private fun NewAndLinkedDataSectionDivider(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = ScheduleThemeRes.strings.newAndLinkedDataSectionLabel,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}