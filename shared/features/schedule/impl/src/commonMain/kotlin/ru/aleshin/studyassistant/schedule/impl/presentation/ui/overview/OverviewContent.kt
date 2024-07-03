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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import functional.Constants.Placeholder.OVERVIEW_ITEMS
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.DetailsClassHomeworkBadge
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.DetailsClassTestBadge
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.DetailsClassViewItem
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.DetailsClassViewPlaceholder

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun OverviewContent(
    state: OverviewViewState,
    modifier: Modifier = Modifier,
    onShowClassInfo: (ClassDetailsUi) -> Unit,
) = with(state) {
    Column(modifier = modifier.padding(top = 12.dp)) {
        Crossfade(
            modifier = Modifier.weight(1f),
            targetState = isScheduleLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!loading && schedule != null) {
                if (schedule.classes.isNotEmpty()) {
                    val classListState = rememberLazyListState()
                    LazyColumn(
                        state = classListState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        items(schedule.classes) { classModel ->
                            Modifier.fillMaxWidth()
                            DetailsClassViewItem(
                                modifier = Modifier.animateItem(),
                                onClick = { onShowClassInfo(classModel) },
                                isActive = activeClass?.uid == classModel.uid,
                                progress = activeClass?.progress?.takeIf { activeClass.isStarted }
                                    ?: -1f,
                                timeRange = classModel.timeRange,
                                subject = classModel.subject,
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
                        }
                        item { Spacer(modifier = Modifier.height(60.dp)) }
                    }
                } else {
                    EmptyClassesView(modifier = Modifier.weight(1f))
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