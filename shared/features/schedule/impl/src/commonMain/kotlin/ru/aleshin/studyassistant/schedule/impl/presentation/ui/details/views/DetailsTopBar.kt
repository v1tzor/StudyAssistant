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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.TopAppBarButton
import ru.aleshin.studyassistant.core.ui.views.TopAppBarEmptyButton
import ru.aleshin.studyassistant.core.ui.views.TopAppBarTitle
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DetailsTopBar(
    modifier: Modifier = Modifier,
    onEditClick: () -> Unit,
    onOverviewClick: () -> Unit,
    onCurrentWeek: () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = {
            TopAppBarTitle(
                header = ScheduleThemeRes.strings.detailsHeader,
                textAlign = TextAlign.Center,
            )
        },
        navigationIcon = {
            Row {
                TopAppBarButton(
                    imagePainter = painterResource(ScheduleThemeRes.icons.editTable),
                    imageDescription = null,
                    onButtonClick = onEditClick,
                )
                TopAppBarEmptyButton()
            }
        },
        actions = {
            Row {
                TopAppBarButton(
                    imagePainter = painterResource(StudyAssistantRes.icons.calendarToday),
                    imageDescription = null,
                    onButtonClick = onCurrentWeek,
                )
                TopAppBarButton(
                    imagePainter = painterResource(ScheduleThemeRes.icons.openOverview),
                    imageDescription = null,
                    onButtonClick = onOverviewClick,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}