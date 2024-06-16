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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import extensions.boldWeight
import theme.StudyAssistantRes
import theme.tokens.contentColorFor

/**
 * @author Stanislav Aleshin on 25.05.2024.
 */
@Composable
fun SmallInfoBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = StudyAssistantRes.colors.accents.redContainer,
    contentColor: Color = StudyAssistantRes.colors.accents.contentColorFor(containerColor),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.animateContentSize(),
        shape = RoundedCornerShape(6.dp),
        color = containerColor,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall.boldWeight()) {
                Box(
                    modifier = Modifier.padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center,
                    content = { content() },
                )
            }
        }
    }
}

@Composable
fun InfoBadge(
    modifier: Modifier = Modifier,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    containerColor: Color = StudyAssistantRes.colors.accents.red,
    contentColor: Color = StudyAssistantRes.colors.accents.contentColorFor(containerColor),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides contentColor
        ) {
            ProvideTextStyle(value = MaterialTheme.typography.labelSmall) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    leadingIcon?.invoke()
                    content()
                    trailingIcon?.invoke()
                }
            }
        }
    }
}