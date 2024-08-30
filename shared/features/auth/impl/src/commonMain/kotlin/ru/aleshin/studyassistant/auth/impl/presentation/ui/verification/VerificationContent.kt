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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.verification

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.auth.impl.presentation.theme.AuthThemeRes
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.AuthHeaderSection
import ru.aleshin.studyassistant.auth.impl.presentation.ui.common.EmailTextField
import ru.aleshin.studyassistant.auth.impl.presentation.ui.verification.contract.VerificationViewState
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * @author Stanislav Aleshin on 29.08.2024
 */
@Composable
internal fun VerificationContent(
    state: VerificationViewState,
    modifier: Modifier = Modifier,
    onSendEmailVerification: () -> Unit,
) = with(state) {
    Column(
        modifier = modifier.padding(bottom = 32.dp, top = 6.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AuthHeaderSection(
            modifier = Modifier.weight(1f),
            header = AuthThemeRes.strings.verificationHeadline,
            title = AuthThemeRes.strings.verificationTitle,
            illustration = painterResource(AuthThemeRes.icons.verificationIllustration),
            contentDescription = AuthThemeRes.strings.registerDesc,
        )
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            EmailTextField(
                enabled = true,
                readOnly = true,
                email = appUser?.email ?: "",
                onEmailChanged = {},
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = onSendEmailVerification,
                    modifier = Modifier.fillMaxWidth().height(44.dp),
                    enabled = retryAvailableTime == null,
                    shape = MaterialTheme.shapes.large,
                ) {
                    Crossfade(
                        targetState = isLoadingSend,
                        animationSpec = floatSpring(),
                    ) { loading ->
                        if (!loading) {
                            Text(text = AuthThemeRes.strings.verificationButtonLabel)
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = LocalContentColor.current,
                            )
                        }
                    }
                }
                if (retryAvailableTime != null) {
                    Text(
                        text = buildString {
                            append(AuthThemeRes.strings.retryAvailableTimeLabelPrefix)
                            append(retryAvailableTime.toDuration(MILLISECONDS).toLanguageString())
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}