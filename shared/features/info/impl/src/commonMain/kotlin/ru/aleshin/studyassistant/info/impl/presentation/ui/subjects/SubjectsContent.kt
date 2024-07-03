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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import functional.Constants.Placeholder
import functional.UID
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsViewState
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.views.DetailsSubjectViewItem
import theme.StudyAssistantRes
import views.PlaceholderBox

/**
 * @author Stanislav Aleshin on 17.06.2024
 */
@Composable
internal fun SubjectsContent(
    state: SubjectsViewState,
    modifier: Modifier,
    gridState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    onEditSubject: (UID) -> Unit,
    onDeleteSubject: (UID) -> Unit,
) = with(state) {
    Crossfade(
        modifier = modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
        targetState = isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (loading) {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(160.dp),
                modifier = Modifier.fillMaxSize(),
                state = gridState,
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
                text = StudyAssistantRes.strings.noResultTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}