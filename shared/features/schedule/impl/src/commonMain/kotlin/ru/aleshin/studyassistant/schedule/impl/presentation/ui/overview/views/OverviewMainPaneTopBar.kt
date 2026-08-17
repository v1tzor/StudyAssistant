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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.views.TopAppBarButton
import ru.aleshin.studyassistant.core.ui.views.weekdayDayMonthFormat
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.ic_list_edit
import ru.aleshin.studyassistant.schedule.impl.resources.ic_open_table
import ru.aleshin.studyassistant.schedule.impl.resources.overview_edit_desc
import ru.aleshin.studyassistant.schedule.impl.resources.overview_pane_header
import ru.aleshin.studyassistant.schedule.impl.resources.overview_today_desc
import ru.aleshin.studyassistant.schedule.impl.resources.overview_week_view_desc
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_calendar_today as core_ic_calendar_today

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OverviewMainPaneTopBar(
    modifier: Modifier = Modifier,
    selectedDate: Instant?,
    enabledEdit: Boolean,
    onEditClick: () -> Unit,
    onCurrentDay: () -> Unit,
    onDetailsClick: () -> Unit,
) {
    val dateFormat = DateTimeComponents.Formats.weekdayDayMonthFormat()
    val selectedDateTitle = remember(selectedDate, dateFormat) {
        selectedDate?.formatByTimeZone(dateFormat)?.replaceFirstChar { char ->
            if (char.isLowerCase()) char.titlecase() else char.toString()
        }.orEmpty()
    }

    TopAppBar(
        modifier = modifier,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(Res.string.overview_pane_header),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = selectedDateTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                TopAppBarButton(
                    enabled = enabledEdit,
                    imagePainter = painterResource(Res.drawable.ic_list_edit),
                    imageDescription = stringResource(Res.string.overview_edit_desc),
                    onButtonClick = onEditClick,
                )
                TopAppBarButton(
                    imagePainter = painterResource(CoreRes.drawable.core_ic_calendar_today),
                    imageDescription = stringResource(Res.string.overview_today_desc),
                    onButtonClick = onCurrentDay,
                )
                TopAppBarButton(
                    imagePainter = painterResource(Res.drawable.ic_open_table),
                    imageDescription = stringResource(Res.string.overview_week_view_desc),
                    onButtonClick = onDetailsClick,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}
