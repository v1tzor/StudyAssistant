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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.login.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.OAuthSignInRowContainer
import ru.aleshin.studyassistant.core.api.utils.OAuthProvider
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.domain.entities.users.UserSession

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
@Composable
internal fun LoginActionsSection(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    enabledGoogle: Boolean,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onSuccessOAuthLogin: (UserSession) -> Unit,
    onFailureOAuthLogin: () -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Button(
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth().height(44.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.large,
        ) {
            if (!isLoading) {
                Text(
                    text = AuthThemeRes.strings.loginLabel,
                    style = MaterialTheme.typography.titleSmall,
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    strokeWidth = 2.dp,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Text(
                text = AuthThemeRes.strings.otherSignInWayTitle,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleSmall,
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
        OAuthSignInRowContainer(
            onCreateSession = onSuccessOAuthLogin,
            onError = { exception ->
                exception?.printStackTrace()
                onFailureOAuthLogin()
            },
        ) { callback ->
            OutlinedButton(
                onClick = { callback(OAuthProvider.GOOGLE) },
                modifier = Modifier.alphaByEnabled(enabledGoogle).size(48.dp),
                enabled = !isLoading && enabledGoogle,
                shape = MaterialTheme.shapes.large,
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                contentPadding = PaddingValues(0.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Image(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(AuthThemeRes.icons.google),
                        contentDescription = AuthThemeRes.strings.loginViaGoogleLabel,
                    )
                }
            }
            OutlinedButton(
                onClick = { callback(OAuthProvider.YANDEX) },
                modifier = Modifier.alphaByEnabled(enabledGoogle).size(48.dp),
                enabled = !isLoading && enabledGoogle,
                shape = MaterialTheme.shapes.large,
                contentPadding = PaddingValues(0.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
//                    Image(
//                        modifier = Modifier.size(24.dp),
//                        painter = painterResource(AuthThemeRes.icons.google),
//                        contentDescription = AuthThemeRes.strings.loginViaGoogleLabel,
//                    )
                    Text(
                        text = "Y",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Red,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
internal fun NotAccountButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier
                .clip(MaterialTheme.shapes.small)
                .clickable(
                    interactionSource = interactionSource,
                    indication = LocalIndication.current,
                    enabled = enabled,
                    onClick = onClick,
                )
                .padding(contentPadding),
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                    append(AuthThemeRes.strings.notAccountLabelFirst)
                }
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                    append(AuthThemeRes.strings.notAccountLabelSecond)
                }
            },
            style = MaterialTheme.typography.labelMedium,
        )
    }
}