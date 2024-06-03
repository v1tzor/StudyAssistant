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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import functional.Constants

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
@Composable
fun InfoTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: String?,
    maxLength: Int = Constants.Text.DEFAULT_MAX_TEXT_LENGTH,
    onValueChange: (String) -> Unit,
    labelText: String,
    infoIcon: Painter,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledBorderColor = MaterialTheme.colorScheme.outline,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    )
) {
    Row(
        modifier = Modifier.padding(paddingValues).animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = infoIcon,
            contentDescription = labelText,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            modifier = modifier.sizeIn(minHeight = 65.dp).fillMaxWidth(),
            enabled = enabled,
            value = value ?: "",
            onValueChange = { text ->
                if (text.length <= maxLength) {
                    onValueChange(text)
                }
            },
            label = {
                Text(
                    text = labelText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            textStyle = textStyle,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            colors = colors,
            shape = MaterialTheme.shapes.large,
        )
    }
}

@Composable
fun InfoTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: TextFieldValue,
    maxLength: Int = Constants.Text.DEFAULT_MAX_TEXT_LENGTH,
    onValueChange: (TextFieldValue) -> Unit,
    labelText: String,
    infoIcon: Painter,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledBorderColor = MaterialTheme.colorScheme.outline,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    )
) {
    Row(
        modifier = Modifier.padding(paddingValues).animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = infoIcon,
            contentDescription = labelText,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        OutlinedTextField(
            modifier = modifier.sizeIn(minHeight = 65.dp).fillMaxWidth(),
            enabled = enabled,
            value = value,
            onValueChange = { value1 ->
                if (value1.text.length <= maxLength) {
                    onValueChange(value1)
                }
            },
            label = {
                Text(
                    text = labelText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            textStyle = textStyle,
            trailingIcon = trailingIcon,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            colors = colors,
            shape = MaterialTheme.shapes.large,
        )
    }
}

@Composable
fun VerticalInfoTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: String?,
    maxLength: Int = Constants.Text.DEFAULT_MAX_TEXT_LENGTH,
    onValueChange: (String) -> Unit,
    labelText: String,
    placeholder: String,
    infoIcon: Painter,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    isError: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledBorderColor = MaterialTheme.colorScheme.outline,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    )
) {
    Column(
        modifier = Modifier.padding(paddingValues).animateContentSize(spring()),
        verticalArrangement = verticalArrangement,
    ) {
        Text(
            text = labelText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            modifier = modifier.sizeIn(minHeight = 56.dp).fillMaxWidth(),
            enabled = enabled,
            value = value ?: "",
            onValueChange = { text ->
                if (text.length <= maxLength) {
                    onValueChange(text)
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = infoIcon,
                    contentDescription = labelText,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = trailingIcon,
            textStyle = textStyle,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            colors = colors,
            shape = MaterialTheme.shapes.large,
        )
    }
}

@Composable
fun VerticalInfoTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: TextFieldValue,
    maxLength: Int = Constants.Text.DEFAULT_MAX_TEXT_LENGTH,
    onValueChange: (TextFieldValue) -> Unit,
    labelText: String,
    placeholder: String,
    infoIcon: Painter,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp),
    isError: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    colors: TextFieldColors = OutlinedTextFieldDefaults.colors(
        disabledLabelColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface,
        disabledBorderColor = MaterialTheme.colorScheme.outline,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    )
) {
    Column(
        modifier = Modifier.padding(paddingValues).animateContentSize(spring()),
        verticalArrangement = verticalArrangement,
    ) {
        Text(
            text = labelText,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            modifier = modifier.sizeIn(minHeight = 56.dp).fillMaxWidth(),
            enabled = enabled,
            value = value,
            onValueChange = { fieldValue ->
                if (fieldValue.text.length <= maxLength) {
                    onValueChange(fieldValue)
                }
            },
            placeholder = {
                Text(
                    text = placeholder,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = infoIcon,
                    contentDescription = labelText,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = trailingIcon,
            textStyle = textStyle,
            prefix = prefix,
            suffix = suffix,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            isError = isError,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            interactionSource = interactionSource,
            colors = colors,
            shape = MaterialTheme.shapes.large,
        )
    }
}

@Composable
fun ClickableInfoTextField(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: String?,
    label: String?,
    placeholder: String,
    leadingInfoIcon: Painter,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    paddingValues: PaddingValues = PaddingValues(),
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    containerColor: Color = MaterialTheme.colorScheme.background,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Row(
        modifier = Modifier.animateContentSize().padding(paddingValues).animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(
            modifier = Modifier.height(56.dp).padding(top = if (label != null) 5.dp else 0.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = leadingInfoIcon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(modifier = modifier.padding(top = if (label != null) 5.dp else 0.dp)) {
            Surface(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 56.dp),
                enabled = enabled,
                shape = MaterialTheme.shapes.large,
                color = containerColor,
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else borderColor
                ),
                interactionSource = interactionSource,
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        top = 12.dp,
                        end = if (trailingIcon != null) 8.dp else 16.dp,
                        bottom = 12.dp,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AnimatedContent(
                        modifier = Modifier.weight(1f),
                        targetState = value,
                    ) { textValue ->
                        if (textValue != null) {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = textValue,
                                color = textColor,
                                maxLines = if (singleLine) 1 else maxLines,
                                minLines = minLines,
                                overflow = TextOverflow.Ellipsis,
                                style = textStyle,
                            )
                        } else {
                            Text(
                                modifier = Modifier.weight(1f),
                                text = placeholder,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = textStyle,
                            )
                        }
                    }
                    if (trailingIcon != null) {
                        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                            trailingIcon.invoke()
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.offset(x = 16.dp, y = (-8).dp),
                shape = MaterialTheme.shapes.medium,
                color = containerColor,
            ) {
                if (label != null) {
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp),
                        text = label,
                        color = if (isError) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
fun ClickableTextField(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    value: String?,
    label: String?,
    placeholder: String,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    containerColor: Color = MaterialTheme.colorScheme.background,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Box(modifier = modifier.animateContentSize().padding(top = if (label != null) 5.dp else 0.dp)) {
        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 56.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.large,
            color = containerColor,
            border = BorderStroke(
                width = 1.dp,
                color = if (isError) MaterialTheme.colorScheme.error else borderColor
            ),
            interactionSource = interactionSource,
        ) {
            Row(
                modifier = Modifier.padding(
                    start = if (leadingIcon != null) 8.dp else 16.dp,
                    top = 12.dp,
                    end = if (trailingIcon != null) 8.dp else 16.dp,
                    bottom = 12.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        leadingIcon.invoke()
                    }
                }
                AnimatedContent(
                    modifier = Modifier.weight(1f),
                    targetState = value,
                ) { textValue ->
                    if (textValue != null) {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = textValue,
                            color = textColor,
                            maxLines = if (singleLine) 1 else maxLines,
                            minLines = minLines,
                            overflow = TextOverflow.Ellipsis,
                            style = textStyle,
                        )
                    } else {
                        Text(
                            modifier = Modifier.weight(1f),
                            text = placeholder,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = textStyle,
                        )
                    }
                }
                if (trailingIcon != null) {
                    Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                        trailingIcon.invoke()
                    }
                }
            }
        }
        if (label != null) {
            Surface(
                modifier = Modifier.offset(x = 16.dp, y = (-8).dp),
                shape = MaterialTheme.shapes.medium,
                color = containerColor,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = label,
                    color = if (isError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
fun MenuTextField(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    onTextChange: (String) -> Unit,
    placeholder: (@Composable () -> Unit)? = null,
    suffix: (@Composable () -> Unit)? = null,
    prefix: (@Composable () -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge,
    cursorBrush: Brush = SolidColor(MaterialTheme.colorScheme.onSurface),
    visualTransformation: VisualTransformation = VisualTransformation.None,
    shape: Shape = MaterialTheme.shapes.medium,
    border: BorderStroke = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    container: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    readOnly: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val isFocused by interactionSource.collectIsFocusedAsState()

    Surface(
        modifier = modifier,
        shape = shape,
        color = container,
        border = border,
    ) {
        BasicTextField(
            value = text,
            onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 32.dp),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = textStyle.copy(
                color = MaterialTheme.colorScheme.onSurface,
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            visualTransformation = visualTransformation,
            onTextLayout = onTextLayout,
            interactionSource = interactionSource,
            cursorBrush = cursorBrush,
        ) { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProvideTextStyle(value = MaterialTheme.typography.bodyLarge) {
                    if (text.isEmpty() && !isFocused && placeholder != null) {
                        Box {
                            ProvideTextStyle(value = MaterialTheme.typography.bodyLarge) {
                                CompositionLocalProvider(
                                    LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                                    content = placeholder
                                )
                            }
                        }
                    } else {
                        prefix?.invoke()
                        Box(Modifier.weight(1f)) {
                            innerTextField()
                        }
                        suffix?.invoke()
                    }
                }
            }
        }
    }
}