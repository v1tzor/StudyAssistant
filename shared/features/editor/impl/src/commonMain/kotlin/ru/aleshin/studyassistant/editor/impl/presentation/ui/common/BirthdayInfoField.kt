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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.dialog.BirthdayDatePicker
import ru.aleshin.studyassistant.editor.impl.resources.Res
import ru.aleshin.studyassistant.editor.impl.resources.birthday_field_label
import ru.aleshin.studyassistant.editor.impl.resources.birthday_field_placeholder
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_birthday as core_ic_birthday
import ru.aleshin.studyassistant.core.ui.resources.ic_select_date as core_ic_select_date

/**
 * @author Stanislav Aleshin on 06.06.2024.
 */
@Composable
internal fun BirthdayInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    birthday: String?,
    onSelected: (String?) -> Unit,
) {
    var datePickerState by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { datePickerState = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = birthday,
        label = stringResource(Res.string.birthday_field_label),
        placeholder = stringResource(Res.string.birthday_field_placeholder),
        infoIcon = painterResource(CoreRes.drawable.core_ic_birthday),
        trailingIcon = {
            Icon(
                painter = painterResource(CoreRes.drawable.core_ic_select_date),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    )

    if (datePickerState) {
        BirthdayDatePicker(
            label = stringResource(Res.string.birthday_field_label),
            onDismiss = { datePickerState = false },
            onSelectedDate = { selectedBirthday ->
                onSelected(selectedBirthday)
                datePickerState = false
            }
        )
    }
}