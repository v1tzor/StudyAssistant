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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsSection(
    title: String,
    modifier: Modifier = Modifier,
    contentPaddings: PaddingValues = PaddingValues(16.dp),
    contentArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        AnalyticsSectionCard(
            content = content,
            paddingValues = contentPaddings,
            contentArrangement = contentArrangement
        )
    }
}

@Composable
internal fun AnalyticsSectionCard(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(16.dp),
    contentArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues),
            verticalArrangement = contentArrangement,
            content = content,
        )
    }
}

