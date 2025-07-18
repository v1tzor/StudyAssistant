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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.kodein.di.compose.localDI
import org.kodein.di.direct
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.platform.getPlatformActivity
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.domain.entities.users.UserSession
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AccountService

/**
 * @author Stanislav Aleshin on 17.07.2025.
 */
@Composable
internal fun OAuthSignInRowContainer(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(12.dp),
    onCreateSession: (UserSession) -> Unit = {},
    onError: (Exception?) -> Unit = {},
    content: @Composable RowScope.(OAuthSignInCallback) -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = horizontalArrangement,
        ) {
            val localDI = localDI().direct
            val coroutineScope = rememberCoroutineScope()

            val accountService = remember { localDI.instance<AccountService>() }
            val platformActivity = getPlatformActivity()

            val createSessionCallback by rememberUpdatedState(onCreateSession)
            val errorCallback by rememberUpdatedState(onError)

            content.invoke(this@Row) { provider ->
                coroutineScope.launch {
                    try {
                        val session = accountService.createOAuth2Session(
                            activity = platformActivity,
                            provider = provider,
                            scopes = provider.scopes,
                        )
                        if (session != null) {
                            createSessionCallback(session.mapToDomain())
                        } else {
                            errorCallback(null)
                        }
                    } catch (e: Exception) {
                        errorCallback(e)
                    }
                }
            }
        }
    }
}