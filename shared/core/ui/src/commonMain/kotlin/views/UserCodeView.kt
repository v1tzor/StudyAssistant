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

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import theme.StudyAssistantRes
import theme.material.full

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
@Composable
fun UserCodeView(
    modifier: Modifier = Modifier,
    code: String,
    contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
    backgroundColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    shape: Shape = MaterialTheme.shapes.full(),
) {
    Surface(
        modifier = modifier.padding(contentPadding),
        shape = shape,
        color = backgroundColor,
        contentColor = contentColor,
    ) {
        Text(
            text = StudyAssistantRes.strings.userCodeLabel + code,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}