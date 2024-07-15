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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract.DailyScheduleViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views.AddClassView
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views.DetailsClassViewItem
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views.DetailsClassViewPlaceholder
import ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.views.SwapClassesDropdownMenu

/**
 * @author Stanislav Aleshin on 14.07.2024.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun DailyScheduleContent(
    state: DailyScheduleViewState,
    modifier: Modifier = Modifier,
    onCreateClass: () -> Unit,
    onEditClass: (ClassUi) -> Unit,
    onDeleteClass: (ClassUi) -> Unit,
    onSwapClasses: (from: ClassUi, to: ClassUi) -> Unit,
) = with(state) {
    Crossfade(
        modifier = modifier.fillMaxSize().padding(top = 12.dp, end = 12.dp, start = 12.dp),
        targetState = isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (!loading) {
            val listState = rememberLazyListState()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (customSchedule != null) {
                    items(customSchedule.classes, key = { it.uid }) { classModel ->
                        DetailsClassViewItem(
                            modifier = Modifier.animateItem(),
                            onClick = { onEditClass(classModel) },
                            number = customSchedule.classes.indexOf(classModel).inc(),
                            timeRange = classModel.timeRange,
                            subject = classModel.subject,
                            office = classModel.office,
                            organization = classModel.organization,
                            teacher = classModel.teacher,
                            location = classModel.location,
                            trailingActions = {
                                IconButton(
                                    modifier = Modifier.size(32.dp),
                                    onClick = { onDeleteClass(classModel) },
                                ) {
                                    Icon(
                                        painter = painterResource(EditorThemeRes.icons.clearCircular),
                                        tint = MaterialTheme.colorScheme.error,
                                        contentDescription = null
                                    )
                                }
                                Box {
                                    var isExpandSwapClassesMenu by remember { mutableStateOf(false) }

                                    SwapClassesDropdownMenu(
                                        isExpand = isExpandSwapClassesMenu,
                                        currentClass = classModel,
                                        allClasses = customSchedule.classes.map {
                                            Pair(it, customSchedule.classes.indexOf(it).inc())
                                        },
                                        onDismiss = { isExpandSwapClassesMenu = false },
                                        onSwapTo = { targetClass ->
                                            onSwapClasses(classModel, targetClass)
                                            isExpandSwapClassesMenu = false
                                        },
                                    )
                                    IconButton(
                                        modifier = Modifier.size(32.dp),
                                        onClick = { isExpandSwapClassesMenu = true },
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SwapVert,
                                            tint = MaterialTheme.colorScheme.onSurface,
                                            contentDescription = null
                                        )
                                    }
                                }
                            },
                        )
                    }
                    item {
                        AddClassView(
                            modifier = Modifier.fillParentMaxWidth(),
                            onClick = onCreateClass,
                        )
                    }
                } else if (baseSchedule != null && baseSchedule.classes.isNotEmpty()) {
                    items(baseSchedule.classes, key = { it.uid }) { classModel ->
                        DetailsClassViewItem(
                            modifier = Modifier.animateItemPlacement(),
                            onClick = {},
                            enabled = false,
                            number = baseSchedule.classes.indexOf(classModel).inc(),
                            timeRange = classModel.timeRange,
                            subject = classModel.subject,
                            office = classModel.office,
                            organization = classModel.organization,
                            teacher = classModel.teacher,
                            location = classModel.location,
                            trailingActions = null,
                        )
                    }
                } else {
                    item {
                        NoneClassesView(modifier = Modifier.fillParentMaxWidth())
                    }
                }
                item { Spacer(modifier = Modifier.height(60.dp)) }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                userScrollEnabled = false,
            ) {
                items(Placeholder.OVERVIEW_ITEMS) {
                    DetailsClassViewPlaceholder()
                }
            }
        }
    }
}

@Composable
internal fun NoneClassesView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.padding(top = 4.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = EditorThemeRes.strings.noneClassesTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}