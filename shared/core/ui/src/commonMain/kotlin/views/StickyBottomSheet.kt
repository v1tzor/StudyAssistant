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

package views

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mappers.pxToDp

/**
 * @author Stanislav Aleshin on 24.05.2024.
 */
@Composable
@ExperimentalMaterial3Api
fun StickyBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState,
    layoutHeight: Int,
    dragHandle: (@Composable () -> Unit)? = { SmallDragHandle() },
    header: @Composable () -> Unit,
    expandedContent: @Composable () -> Unit,
    footer: @Composable (PaddingValues) -> Unit,
    divideContent: Boolean = true,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    color: Color = MaterialTheme.colorScheme.surfaceContainer,
    tonalElevation: Dp = 0.dp,
    shadowElevation: Dp = 0.dp,
) {
    var headerHeight by rememberSaveable { mutableIntStateOf(0) }
    var footerHeight by rememberSaveable { mutableIntStateOf(0) }
    var sheetOffset by rememberSaveable { mutableFloatStateOf(0f) }

    Surface(
        modifier = modifier,
        shape = shape,
        color = color,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    ) {
        Box(modifier = Modifier.onGloballyPositioned { sheetOffset = it.boundsInRoot().top }) {
            Column {
                dragHandle?.invoke()
                Box(
                    modifier = Modifier.onGloballyPositioned { headerHeight = it.size.height },
                    content = { header() },
                )
                if (divideContent) HorizontalDivider()
                expandedContent()
                Spacer(modifier = Modifier.height(footerHeight.pxToDp()))
            }
            Box(
                modifier = Modifier
                    .offset(
                        y = calculateSheetFooterOffset(
                            sheetHeight = calculateSheetHeight(
                                state = sheetState,
                                initSheetOffset = sheetOffset,
                                layoutHeight = layoutHeight,
                            ),
                            headerHeight = headerHeight
                        )
                    )
                    .onGloballyPositioned { footerHeight = it.size.height }
                    .background(MaterialTheme.colorScheme.surfaceContainer),
                content = { footer(WindowInsets.navigationBars.asPaddingValues()) },
            )
        }
    }
}

@Composable
fun SmallDragHandle(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Surface(
            modifier = modifier.padding(top = 12.dp, bottom = 4.dp),
            color = color,
            shape = shape
        ) {
            Box(Modifier.size(width = 32.dp, height = 4.dp))
        }
    }
}

@Composable
@ExperimentalMaterial3Api
private fun calculateSheetHeight(
    state: SheetState,
    initSheetOffset: Float,
    layoutHeight: Int,
): Dp {
    val density = LocalDensity.current
    var isInitialize by remember { mutableStateOf(false) }
    val sheetOffset by remember { derivedStateOf { if (isInitialize) state.requireOffset() else initSheetOffset } }

    DisposableEffect(Unit) {
        isInitialize = true
        onDispose { isInitialize = false }
    }

    return density.run { layoutHeight.toDp() - sheetOffset.toDp() }
}

@Composable
private fun calculateSheetFooterOffset(
    sheetHeight: Dp,
    headerHeight: Int,
    verticalPadding: Dp = 8.dp,
): Dp {
    val density = LocalDensity.current

    return density.run { sheetHeight - headerHeight.toDp() + verticalPadding }
}