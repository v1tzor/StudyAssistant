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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorNotSelectedItemView
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.MediatedSubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
@Composable
internal fun LinkedSubjectsView(
    modifier: Modifier = Modifier,
    enabledLink: Boolean,
    sharedSubject: MediatedSubjectUi,
    linkedSubject: SubjectUi?,
    onLinkSubject: () -> Unit,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min).fillMaxWidth().animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.heightIn(40.dp).weight(1f),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.medium,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SubjectColorIndicator(indicatorColor = Color(sharedSubject.color))
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = sharedSubject.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
        if (linkedSubject != null && enabledLink) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.green,
            )
            Surface(
                onClick = onLinkSubject,
                modifier = Modifier.heightIn(40.dp).weight(1f),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    SubjectColorIndicator(indicatorColor = Color(linkedSubject.color))
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = linkedSubject.name,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowRight,
                            contentDescription = null,
                        )
                    }
                }
            }
        } else if (enabledLink) {
            Surface(
                onClick = onLinkSubject,
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
internal fun LinkedSubjectsViewPlaceholder(
    modifier: Modifier = Modifier
) {
    PlaceholderBox(
        modifier = modifier.fillMaxWidth().height(40.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@Composable
private fun SubjectColorIndicator(
    modifier: Modifier = Modifier,
    indicatorColor: Color,
) {
    Surface(
        modifier = modifier.padding(vertical = 8.dp).fillMaxHeight().width(4.dp),
        shape = MaterialTheme.shapes.full,
        color = indicatorColor,
        content = { Box(modifier = Modifier.fillMaxHeight()) }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SubjectLinkerDialog(
    modifier: Modifier = Modifier,
    selected: SubjectUi?,
    subjects: List<SubjectUi>,
    onDismiss: () -> Unit,
    onConfirm: (SubjectUi?) -> Unit,
) {
    var selectedSubject by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedSubject,
        items = subjects,
        header = ScheduleThemeRes.strings.subjectLinkerDialogHeader,
        title = ScheduleThemeRes.strings.subjectLinkerDialogTitle,
        itemView = { subject ->
            SelectorItemView(
                onClick = { selectedSubject = subject },
                selected = subject.uid == selectedSubject?.uid,
                title = subject.name,
                label = subject.eventType.mapToString(StudyAssistantRes.strings),
                leadingIcon = {
                    Surface(
                        modifier = Modifier.height(IntrinsicSize.Min),
                        shape = MaterialTheme.shapes.full,
                        color = Color(subject.color),
                        content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                    )
                },
            )
        },
        notSelectedItem = {
            SelectorNotSelectedItemView(
                selected = selectedSubject == null,
                onClick = { selectedSubject = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}