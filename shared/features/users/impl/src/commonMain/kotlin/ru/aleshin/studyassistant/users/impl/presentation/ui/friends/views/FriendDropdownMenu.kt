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

package ru.aleshin.studyassistant.users.impl.presentation.ui.friends.views

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PersonRemove
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes

/**
 * @author Stanislav Aleshin on 13.07.2024.
 */
@Composable
internal fun FriendDropdownMenu(
    modifier: Modifier = Modifier,
    isExpand: Boolean,
    onDismiss: () -> Unit,
    onDeleteFromFriend: () -> Unit,
    offset: DpOffset = DpOffset(0.dp, 6.dp),
) {
    DropdownMenu(
        expanded = isExpand,
        onDismissRequest = onDismiss,
        modifier = modifier,
        offset = offset,
        shape = MaterialTheme.shapes.large,
    ) {
        DropdownMenuItem(
            onClick = onDeleteFromFriend,
            text = { Text(text = UsersThemeRes.strings.deleteUserFromFriendsTitle) },
            leadingIcon = {
                Icon(imageVector = Icons.Outlined.PersonRemove, contentDescription = null)
            }
        )
    }
}