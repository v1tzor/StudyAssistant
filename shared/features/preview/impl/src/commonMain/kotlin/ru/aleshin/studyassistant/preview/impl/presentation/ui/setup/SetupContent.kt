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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.core.PlatformFile
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.preview.impl.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupViewState
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.CalendarPageInfo
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.NavigationPageButton
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.OrganizationPageInfo
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.ProfilePageInfo
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SchedulePageInfo
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupPage

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
@Composable
internal fun SetupContent(
    state: SetupViewState,
    modifier: Modifier,
    onUpdateProfile: (AppUserUi) -> Unit,
    onUpdateOrganization: (OrganizationUi) -> Unit,
    onUpdateCalendarSettings: (CalendarSettingsUi) -> Unit,
    onSaveProfile: () -> Unit,
    onUpdateProfileAvatar: (PlatformFile) -> Unit,
    onDeleteProfileAvatar: () -> Unit,
    onSaveOrganization: () -> Unit,
    onUpdateOrganizationAvatar: (PlatformFile) -> Unit,
    onDeleteOrganizationAvatar: () -> Unit,
    onSaveCalendar: () -> Unit,
    onExceedingAvatarSizeLimit: (Int) -> Unit,
    onFillOutSchedule: () -> Unit,
    onStartUsing: () -> Unit,
) = with(state) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SetupPageInfoSection(
            modifier = Modifier.weight(1f),
            currentPage = currentPage,
            profile = profile,
            actionWithProfileAvatar = actionWithProfileAvatar,
            organization = organization,
            actionWithOrganizationAvatar = actionWithOrganizationAvatar,
            calendarSettings = calendarSettings,
            onUpdateProfile = onUpdateProfile,
            onUpdateProfileAvatar = onUpdateProfileAvatar,
            onDeleteProfileAvatar = onDeleteProfileAvatar,
            onUpdateOrganization = onUpdateOrganization,
            onUpdateOrganizationAvatar = onUpdateOrganizationAvatar,
            onDeleteOrganizationAvatar = onDeleteOrganizationAvatar,
            onUpdateCalendarSettings = onUpdateCalendarSettings,
            onExceedingAvatarSizeLimit = onExceedingAvatarSizeLimit,
        )
        SetupPageNavigationSection(
            enabledSaveProfile = profile?.username?.isNotBlank() == true,
            enabledSaveOrganization = organization?.shortName?.isNotBlank() == true,
            currentPage = currentPage,
            isFillOutSchedule = isFillOutSchedule,
            onSaveProfile = onSaveProfile,
            onSaveOrganization = onSaveOrganization,
            onSaveCalendar = onSaveCalendar,
            onFillOutSchedule = onFillOutSchedule,
            onStartUsing = onStartUsing,
        )
    }
}

@Composable
private fun SetupPageInfoSection(
    modifier: Modifier = Modifier,
    currentPage: SetupPage,
    profile: AppUserUi?,
    actionWithProfileAvatar: ActionWithAvatar,
    organization: OrganizationUi?,
    actionWithOrganizationAvatar: ActionWithAvatar,
    calendarSettings: CalendarSettingsUi?,
    onUpdateProfile: (AppUserUi) -> Unit,
    onUpdateProfileAvatar: (PlatformFile) -> Unit,
    onDeleteProfileAvatar: () -> Unit,
    onUpdateOrganization: (OrganizationUi) -> Unit,
    onUpdateOrganizationAvatar: (PlatformFile) -> Unit,
    onDeleteOrganizationAvatar: () -> Unit,
    onUpdateCalendarSettings: (CalendarSettingsUi) -> Unit,
    onExceedingAvatarSizeLimit: (Int) -> Unit
) {
    AnimatedContent(
        targetState = if (profile != null) currentPage else null,
        modifier = modifier.padding(horizontal = 24.dp),
        transitionSpec = {
            fadeIn(animationSpec = tween(320, delayMillis = 90)).togetherWith(
                fadeOut(animationSpec = tween(320))
            )
        },
    ) { page ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (page != null) {
                Column(verticalArrangement = Arrangement.spacedBy(32.dp)) {
                    SetupStepHeader(
                        title = page.stepTitle,
                        currentStep = page.id.inc(),
                        maxSteps = SetupPage.entries.size,
                    )
                    when (page) {
                        SetupPage.PROFILE -> if (profile != null) {
                            ProfilePageInfo(
                                profile = profile,
                                avatar = when (actionWithProfileAvatar) {
                                    is ActionWithAvatar.None -> actionWithProfileAvatar.uri
                                    is ActionWithAvatar.Set -> actionWithProfileAvatar.uri
                                    is ActionWithAvatar.Delete -> null
                                },
                                onUpdateProfile = onUpdateProfile,
                                onUpdateAvatar = onUpdateProfileAvatar,
                                onDeleteAvatar = onDeleteProfileAvatar,
                                onExceedingLimit = onExceedingAvatarSizeLimit,
                            )
                        }
                        SetupPage.ORGANIZATION -> if (organization != null) {
                            OrganizationPageInfo(
                                organization = organization,
                                avatar = when (actionWithOrganizationAvatar) {
                                    is ActionWithAvatar.None -> actionWithOrganizationAvatar.uri
                                    is ActionWithAvatar.Set -> actionWithOrganizationAvatar.uri
                                    is ActionWithAvatar.Delete -> null
                                },
                                onUpdateOrganization = onUpdateOrganization,
                                onUpdateAvatar = onUpdateOrganizationAvatar,
                                onDeleteAvatar = onDeleteOrganizationAvatar,
                                onExceedingLimit = onExceedingAvatarSizeLimit,
                            )
                        }
                        SetupPage.CALENDAR -> if (calendarSettings != null) {
                            CalendarPageInfo(
                                calendarSettings = calendarSettings,
                                onUpdateCalendarSettings = onUpdateCalendarSettings,
                            )
                        }
                        SetupPage.SCHEDULE -> SchedulePageInfo()
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun SetupStepHeader(
    modifier: Modifier = Modifier,
    title: String,
    currentStep: Int,
    maxSteps: Int,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = buildAnnotatedString {
                append(PreviewThemeRes.strings.stepTitle)
                append(currentStep.toString())
                append('/')
                append(maxSteps.toString())
            },
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun SetupPageNavigationSection(
    modifier: Modifier = Modifier,
    enabledSaveProfile: Boolean,
    enabledSaveOrganization: Boolean,
    currentPage: SetupPage,
    isFillOutSchedule: Boolean,
    onSaveProfile: () -> Unit,
    onSaveOrganization: () -> Unit,
    onSaveCalendar: () -> Unit,
    onFillOutSchedule: () -> Unit,
    onStartUsing: () -> Unit,
) {
    Column(
        modifier = modifier.padding(start = 24.dp, end = 24.dp, bottom = 36.dp, top = 16.dp).animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        when (currentPage) {
            SetupPage.PROFILE -> NavigationPageButton(
                enabled = enabledSaveProfile,
                onClick = onSaveProfile,
                navigationLabel = currentPage.buttonLabel,
            )
            SetupPage.ORGANIZATION -> NavigationPageButton(
                enabled = enabledSaveOrganization,
                onClick = onSaveOrganization,
                navigationLabel = currentPage.buttonLabel,
            )
            SetupPage.CALENDAR -> NavigationPageButton(
                onClick = onSaveCalendar,
                navigationLabel = currentPage.buttonLabel,
            )
            SetupPage.SCHEDULE -> {
                NavigationPageButton(
                    onClick = onFillOutSchedule,
                    navigationLabel = currentPage.buttonLabel,
                )
                NavigationPageButton(
                    enabled = isFillOutSchedule,
                    onClick = onStartUsing,
                    navigationLabel = PreviewThemeRes.strings.scheduleStartButtonLabel,
                    isTonal = true,
                )
            }
        }
    }
}