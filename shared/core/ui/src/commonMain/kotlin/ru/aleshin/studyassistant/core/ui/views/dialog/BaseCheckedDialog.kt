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

package ru.aleshin.studyassistant.core.ui.views.dialog

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
@Composable
@ExperimentalMaterial3Api
fun <T> BaseCheckedDialog(
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    selected: List<T>,
    items: List<T>,
    header: String,
    title: String?,
    itemView: @Composable LazyItemScope.(T) -> Unit,
    addItemView: @Composable (LazyItemScope.() -> Unit)? = null,
    filters: @Composable (RowScope.() -> Unit)? = null,
    properties: DialogProperties = DialogProperties(),
    sizes: CheckedDialogSizes = CheckedDialogSizes(),
    itemsListState: LazyListState = rememberLazyListState(),
    shadowElevation: Dp = 4.dp,
    onDismiss: () -> Unit,
    onConfirm: (List<T>) -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        properties = properties,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier.width(sizes.dialogWidth).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = shadowElevation,
        ) {
            Column {
                DialogHeader(
                    header = header,
                    title = title,
                )
                if (filters != null) {
                    Row(
                        modifier = Modifier.padding(sizes.filtersPaddings),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = filters,
                    )
                }
                HorizontalDivider()
                LazyColumn(
                    modifier = Modifier.height(sizes.contentHeight).padding(sizes.itemsListPaddings),
                    state = itemsListState,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(items = items, itemContent = itemView)
                    if (addItemView != null) {
                        item(content = addItemView)
                    }
                }
                DialogButtons(
                    enabledConfirm = confirmEnabled,
                    confirmTitle = StudyAssistantRes.strings.selectConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmClick = { onConfirm(selected) },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun LazyItemScope.CheckedDialogItemView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean,
    title: String,
    label: String?,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .alphaByEnabled(enabled)
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .animateItemPlacement(),
        shape = MaterialTheme.shapes.large,
        color = when (selected) {
            true -> MaterialTheme.colorScheme.primaryContainer
            false -> MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(top = 12.dp, bottom = 12.dp, start = 12.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke()
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                Text(
                    text = title,
                    color = when (selected) {
                        true -> MaterialTheme.colorScheme.onPrimaryContainer
                        false -> MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Checkbox(
                checked = selected,
                onCheckedChange = null,
                modifier = Modifier.size(24.dp),
                enabled = enabled,
            )
        }
    }
}

data class CheckedDialogSizes(
    val dialogWidth: Dp = 350.dp,
    val contentHeight: Dp = 300.dp,
    val filtersPaddings: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, bottom = 12.dp),
    val itemsListPaddings: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
)