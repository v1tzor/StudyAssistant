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

package ru.aleshin.studyassistant.core.ui.views

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
@Immutable
interface SegmentedButtonItem {
    val title: String
        @Composable
        @ReadOnlyComposable
        get
}

@Composable
fun <Item : SegmentedButtonItem> SegmentedButtons(
    modifier: Modifier = Modifier,
    enabled: (Item) -> Boolean = { true },
    items: Array<Item>,
    selectedItem: Item?,
    onItemClick: (Item) -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            if (index == 0) {
                SegmentedButton(
                    enabled = enabled(item),
                    modifier = Modifier.weight(1f),
                    title = item.title,
                    isSelected = item == selectedItem,
                    shape = SegmentedButtonDefaults.firstButtonShape(),
                    onClick = { if (items != selectedItem) onItemClick.invoke(item) },
                )
            } else if (items.lastIndex == index) {
                SegmentedButton(
                    enabled = enabled(item),
                    modifier = Modifier.weight(1f),
                    title = item.title,
                    isSelected = item == selectedItem,
                    shape = SegmentedButtonDefaults.lastButtonShape(),
                    onClick = { if (items != selectedItem) onItemClick.invoke(item) },
                )
            } else {
                SegmentedButton(
                    enabled = enabled(item),
                    modifier = Modifier.weight(1f),
                    title = item.title,
                    isSelected = item == selectedItem,
                    shape = SegmentedButtonDefaults.centerButtonShape(),
                    onClick = { if (items != selectedItem) onItemClick.invoke(item) },
                )
            }
        }
    }
}

@Composable
private fun SegmentedButton(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    title: String,
    shape: Shape,
    isSelected: Boolean,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.alphaByEnabled(enabled).height(SegmentedButtonDefaults.height),
        contentPadding = SegmentedButtonDefaults.contentPadding(),
        colors = SegmentedButtonDefaults.buttonColors(isSelected = isSelected),
        shape = shape,
    ) {
        if (isSelected) {
            Icon(
                modifier = Modifier
                    .size(SegmentedButtonDefaults.selectedIconSize)
                    .padding(end = SegmentedButtonDefaults.selectedIconPadding),
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SegmentedButtonCornerShape(
    cornerStart: Dp = 0.dp,
    cornerEnd: Dp = 0.dp,
) = RoundedCornerShape(
    topStart = cornerStart,
    bottomStart = cornerStart,
    topEnd = cornerEnd,
    bottomEnd = cornerEnd,
)

object SegmentedButtonDefaults {

    val height = 40.dp

    val selectedIconSize = 16.dp

    val selectedIconPadding = 4.dp

    private val horizontalContentPadding = 4.dp

    private val verticalContentPadding = 0.dp

    private val shapeCorner = 100.dp

    @Composable
    fun contentPadding(
        horizontal: Dp = horizontalContentPadding,
        vertical: Dp = verticalContentPadding,
    ) = PaddingValues(horizontal = horizontal, vertical = vertical)

    @Composable
    fun selectedButtonColors(): ButtonColors = ButtonDefaults.filledTonalButtonColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    )

    @Composable
    fun defaultButtonColors(): ButtonColors = ButtonDefaults.outlinedButtonColors()

    @Composable
    fun buttonColors(isSelected: Boolean): ButtonColors = if (isSelected) {
        selectedButtonColors()
    } else {
        defaultButtonColors()
    }

    @Composable
    fun firstButtonShape(corner: Dp = shapeCorner): RoundedCornerShape =
        SegmentedButtonCornerShape(cornerStart = corner)

    @Composable
    fun centerButtonShape(corner: Dp = 0.dp): RoundedCornerShape =
        SegmentedButtonCornerShape(cornerStart = corner, cornerEnd = corner)

    @Composable
    fun lastButtonShape(corner: Dp = shapeCorner): RoundedCornerShape =
        SegmentedButtonCornerShape(cornerEnd = corner)
}