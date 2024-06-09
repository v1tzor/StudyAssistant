/*
 * Copyright 2023 Stanislav Aleshin
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
 * imitations under the License.
 */
package views

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
@Composable
fun DialogButtons(
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    confirmTitle: String = StudyAssistantRes.strings.selectConfirmTitle,
    onCancelClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    Row(
        modifier = modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onCancelClick) {
            Text(
                text = StudyAssistantRes.strings.cancelTitle,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        TextButton(
            enabled = confirmEnabled,
            onClick = onConfirmClick
        ) {
            Text(
                text = confirmTitle,
                color = when (confirmEnabled) {
                    true -> MaterialTheme.colorScheme.primary
                    false -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
fun DialogButtons(
    modifier: Modifier = Modifier,
    enabledConfirmFirst: Boolean = true,
    enabledConfirmSecond: Boolean = true,
    confirmFirstTitle: String,
    confirmSecondTitle: String,
    onCancelClick: () -> Unit,
    onConfirmFirstClick: () -> Unit,
    onConfirmSecondClick: () -> Unit,
) {
    Row(
        modifier = modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TextButton(
            enabled = enabledConfirmSecond,
            onClick = onConfirmSecondClick
        ) {
            Text(
                text = confirmSecondTitle,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        TextButton(onClick = onCancelClick) {
            Text(
                text = StudyAssistantRes.strings.cancelTitle,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        TextButton(
            enabled = enabledConfirmFirst,
            onClick = onConfirmFirstClick,
        ) {
            Text(
                text = confirmFirstTitle,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
fun DialogHeader(
    modifier: Modifier = Modifier,
    header: String,
    title: String? = null,
    paddingValues: PaddingValues = PaddingValues(top = 24.dp, bottom = 12.dp, start = 24.dp, end = 24.dp),
    headerColor: Color = MaterialTheme.colorScheme.onSurface,
    titleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(modifier = modifier.padding(paddingValues)) {
        Text(
            text = header,
            color = headerColor,
            style = MaterialTheme.typography.headlineSmall,
        )
        if (title != null) {
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
fun DialogSwitchParameter(
    modifier: Modifier = Modifier,
    checked: Boolean,
    title: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    onCheckedChange: (Boolean) -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(48.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leadingIcon != null) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.primary,
                    content = leadingIcon,
                )
            }
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                interactionSource = interactionSource,
                thumbContent = {
                    if (checked) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun DialogChipParameter(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    chipLabel: @Composable () -> Unit,
    headerIcon: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(116.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.primary,
                content = headerIcon,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.titleMedium,
                )
                FilterChip(
                    modifier = Modifier.fillMaxWidth(),
                    selected = false,
                    onClick = onClick,
                    label = {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            chipLabel()
                        }
                    },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null
                        )
                    }
                )
            }
        }
    }
}
