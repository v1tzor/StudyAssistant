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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.info.impl.presentation.ui.InfoLayoutMode
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsState
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.DetailsSubjectViewItem
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.no_result_title as core_no_result_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun SubjectsLayout(
    modifier: Modifier = Modifier,
    layoutMode: InfoLayoutMode,
    state: SubjectsState,
    onEditSubject: (UID) -> Unit,
    onDeleteSubject: (UID) -> Unit,
) {
    when (layoutMode) {
        InfoLayoutMode.COMPACT -> SubjectsCompactLayout(
            modifier = modifier,
            state = state,
            onEditSubject = onEditSubject,
            onDeleteSubject = onDeleteSubject,
        )
        InfoLayoutMode.EXPANDED -> SubjectsExpandedLayout(
            modifier = modifier,
            state = state,
            onEditSubject = onEditSubject,
            onDeleteSubject = onDeleteSubject,
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun SubjectsCompactLayout(
    modifier: Modifier = Modifier,
    state: SubjectsState,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    onEditSubject: (UID) -> Unit,
    onDeleteSubject: (UID) -> Unit,
) = with(state) {
    Crossfade(
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        targetState = isLoading,
        animationSpec = floatSpring(),
    ) { loading ->
        if (loading) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize(),
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(Placeholder.EMPLOYEES_OR_SUBJECTS) {
                    PlaceholderBox(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large,
                    )
                }
            }
        } else if (subjects.isNotEmpty()) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize(),
                state = gridState,
                verticalItemSpacing = 12.dp,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(subjects, key = { it.uid }) { subject ->
                    DetailsSubjectViewItem(
                        modifier = Modifier.animateItem(),
                        eventType = subject.eventType,
                        office = subject.office,
                        color = Color(subject.color),
                        name = subject.name,
                        teacher = subject.teacher,
                        location = subject.location,
                        onEdit = { onEditSubject(subject.uid) },
                        onDelete = { onDeleteSubject(subject.uid) },
                    )
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxSize(),
                text = stringResource(CoreRes.string.core_no_result_title),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun SubjectsExpandedLayout(
    modifier: Modifier = Modifier,
    state: SubjectsState,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    onEditSubject: (UID) -> Unit,
    onDeleteSubject: (UID) -> Unit,
) = with(state) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Crossfade(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = AdaptiveLayoutDefaults.ExpandedContentMaxWidth)
                .fillMaxWidth()
                .padding(
                    start = AdaptiveLayoutDefaults.ExpandedHorizontalPadding,
                    end = AdaptiveLayoutDefaults.ExpandedHorizontalPadding,
                    top = AdaptiveLayoutDefaults.SpaceExtraLarge,
                ),
            targetState = isLoading,
            animationSpec = floatSpring(),
        ) { loading ->
            if (loading) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(220.dp),
                    modifier = Modifier.fillMaxSize(),
                    verticalItemSpacing = AdaptiveLayoutDefaults.GridSpacing,
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                ) {
                    items(Placeholder.EMPLOYEES_OR_SUBJECTS) {
                        PlaceholderBox(
                            modifier = Modifier.fillMaxWidth().height(180.dp),
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.extraLarge,
                        )
                    }
                }
            } else if (subjects.isNotEmpty()) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Adaptive(220.dp),
                    modifier = Modifier.fillMaxSize(),
                    state = gridState,
                    verticalItemSpacing = AdaptiveLayoutDefaults.GridSpacing,
                    horizontalArrangement = Arrangement.spacedBy(AdaptiveLayoutDefaults.GridSpacing),
                ) {
                    items(subjects, key = { it.uid }) { subject ->
                        DetailsSubjectViewItem(
                            modifier = Modifier.animateItem(),
                            useExpandedStyle = true,
                            eventType = subject.eventType,
                            office = subject.office,
                            color = Color(subject.color),
                            name = subject.name,
                            teacher = subject.teacher,
                            location = subject.location,
                            onEdit = { onEditSubject(subject.uid) },
                            onDelete = { onDeleteSubject(subject.uid) },
                        )
                    }
                }
            } else {
                Text(
                    modifier = Modifier.fillMaxSize(),
                    text = stringResource(CoreRes.string.core_no_result_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}
