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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder.OVERVIEW_ITEMS
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.common.ClassBottomSheet
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.DetailsClassHomeworkBadge
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.DetailsClassTestBadge
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.DetailsClassViewItem
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.DetailsClassViewPlaceholder

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
internal fun OverviewContent(
    state: OverviewViewState,
    modifier: Modifier = Modifier,
    onAddHomework: (ClassDetailsUi, Instant) -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onAgainHomework: (HomeworkDetailsUi) -> Unit,
    onCompleteHomework: (HomeworkDetailsUi) -> Unit,
) = with(state) {
    Crossfade(
        modifier = modifier.fillMaxSize().padding(top = 12.dp),
        targetState = isScheduleLoading,
        animationSpec = floatSpring(),
    ) { loading ->
        if (!loading && schedule != null) {
            if (schedule.classes.isNotEmpty()) {
                val classListState = rememberLazyListState()
                LazyColumn(
                    state = classListState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                ) {
                    items(schedule.classes, key = { it.uid }) { classModel ->
                        val classSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                        var openClassBottomSheet by remember { mutableStateOf(false) }

                        DetailsClassViewItem(
                            modifier = Modifier.animateItem(),
                            onClick = { openClassBottomSheet = true },
                            isActive = activeClass?.uid == classModel.uid,
                            number = classModel.number,
                            progress = activeClass?.progress?.takeIf { activeClass.isStarted } ?: -1f,
                            timeRange = classModel.timeRange,
                            subject = classModel.subject,
                            eventType = classModel.eventType,
                            office = classModel.office,
                            organization = classModel.organization,
                            teacher = classModel.teacher,
                            location = classModel.location,
                            headerBadge = {
                                if (classModel.homework != null) {
                                    DetailsClassHomeworkBadge(
                                        homeworkStatus = classModel.homework.status,
                                    )
                                }
                                if (classModel.homework?.test != null) {
                                    DetailsClassTestBadge()
                                }
                            }
                        )

                        if (openClassBottomSheet && selectedDate != null) {
                            ClassBottomSheet(
                                sheetState = classSheetState,
                                activeClass = activeClass,
                                classModel = classModel,
                                classDate = selectedDate,
                                onEditHomework = onEditHomework,
                                onAddHomework = onAddHomework,
                                onAgainHomework = onAgainHomework,
                                onCompleteHomework = onCompleteHomework,
                                onDismissRequest = { openClassBottomSheet = false },
                            )
                        }
                    }
                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            } else {
                EmptyClassesView(modifier = Modifier.fillMaxSize())
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp),
            ) {
                items(OVERVIEW_ITEMS) {
                    DetailsClassViewPlaceholder()
                }
            }
        }
    }
}

@Composable
private fun EmptyClassesView(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize().padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = ScheduleThemeRes.strings.emptyClassesTitle,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Start,
                fontWeight = FontWeight.Normal,
                style = MaterialTheme.typography.titleLarge,
            )
            Image(
                modifier = Modifier.fillMaxWidth(0.85f),
                painter = painterResource(ScheduleThemeRes.icons.emptyClassesIllustration),
                contentDescription = null,
                contentScale = ContentScale.Fit,
            )
        }
    }
}