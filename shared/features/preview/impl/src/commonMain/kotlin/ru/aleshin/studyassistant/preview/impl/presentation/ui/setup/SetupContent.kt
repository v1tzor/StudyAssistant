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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.core.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.preview.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.preview.impl.presentation.ui.PreviewLayoutMode
import ru.aleshin.studyassistant.preview.impl.presentation.ui.fetchPreviewLayoutMode
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupState
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store.SetupComponent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.CalendarPageInfo
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.NavigationPageButton
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.OrganizationPageInfo
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.ProfilePageInfo
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SchedulePageInfo
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupPage
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupTopBar
import ru.aleshin.studyassistant.preview.impl.resources.Res
import ru.aleshin.studyassistant.preview.impl.resources.privacy_policy_disclaimer_link
import ru.aleshin.studyassistant.preview.impl.resources.privacy_policy_disclaimer_start
import ru.aleshin.studyassistant.preview.impl.resources.privacy_policy_disclaimer_suffix
import ru.aleshin.studyassistant.preview.impl.resources.schedule_import_ai_button_label
import ru.aleshin.studyassistant.preview.impl.resources.schedule_start_button_label
import ru.aleshin.studyassistant.preview.impl.resources.step_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.exceeding_limit_image_size_message as core_exceeding_limit_image_size_message

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
@Composable
internal fun SetupContent(
    setupComponent: SetupComponent,
    modifier: Modifier = Modifier,
) {
    val store = setupComponent.store
    val state by store.stateAsState()
    val coreExceedingLimitImageSizeMessage = stringResource(CoreRes.string.core_exceeding_limit_image_size_message)
    val layoutMode = currentWindowAdaptiveInfoV2().fetchPreviewLayoutMode()
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            Box(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                contentAlignment = Alignment.TopCenter,
            ) {
                val setupWidthModifier = if (layoutMode == PreviewLayoutMode.EXPANDED) {
                    Modifier
                        .fillMaxHeight()
                        .widthIn(max = 920.dp)
                        .fillMaxWidth()
                } else {
                    Modifier.fillMaxSize().widthIn(max = 720.dp)
                }
                SetupLayout(
                    state = state,
                    modifier = setupWidthModifier,
                    layoutMode = layoutMode,
                    onUpdateProfile = { store.dispatchEvent(SetupEvent.UpdateProfile(it)) },
                    onUpdateOrganization = { store.dispatchEvent(SetupEvent.UpdateOrganization(it)) },
                    onUpdateCalendarSettings = { store.dispatchEvent(SetupEvent.UpdateCalendarSettings(it)) },
                    onSaveProfile = { store.dispatchEvent(SetupEvent.ClickSaveProfileInfo) },
                    onUpdateProfileAvatar = { store.dispatchEvent(SetupEvent.UpdateProfileAvatar(it)) },
                    onDeleteProfileAvatar = { store.dispatchEvent(SetupEvent.DeleteProfileAvatar) },
                    onSaveOrganization = { store.dispatchEvent(SetupEvent.ClickSaveOrganizationInfo) },
                    onUpdateOrganizationAvatar = { store.dispatchEvent(SetupEvent.UpdateOrganizationAvatar(it)) },
                    onDeleteOrganizationAvatar = { store.dispatchEvent(SetupEvent.DeleteOrganizationAvatar) },
                    onSaveCalendar = { store.dispatchEvent(SetupEvent.ClickSaveCalendarInfo) },
                    onImportSchedule = { store.dispatchEvent(SetupEvent.ClickImportSchedule) },
                    onFillOutSchedule = { store.dispatchEvent(SetupEvent.ClickEditWeekSchedule) },
                    onStartUsing = { store.dispatchEvent(SetupEvent.ClickGoToApp) },
                    onExceedingAvatarSizeLimit = {
                        coroutineScope.launch {
                            snackbarState.showSnackbar(
                                message = coreExceedingLimitImageSizeMessage,
                                withDismissAction = true,
                            )
                        }
                    }
                )
            }
        },
        topBar = {
            SetupTopBar(
                enabled = state.currentPage.id != 0,
                onBackPressed = { store.dispatchEvent(SetupEvent.ClickBackPage) },
                stepProgress = state.currentPage.progress(),
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is SetupEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun SetupLayout(
    state: SetupState,
    modifier: Modifier,
    layoutMode: PreviewLayoutMode,
    onUpdateProfile: (ProfileUi) -> Unit,
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
    onImportSchedule: () -> Unit,
    onFillOutSchedule: () -> Unit,
    onStartUsing: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        SetupPageInfoSection(
            modifier = Modifier.weight(1f),
            currentPage = state.currentPage,
            layoutMode = layoutMode,
            profile = state.profile,
            actionWithProfileAvatar = state.actionWithProfileAvatar,
            organization = state.organization,
            actionWithOrganizationAvatar = state.actionWithOrganizationAvatar,
            calendarSettings = state.calendarSettings,
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
            layoutMode = layoutMode,
            enabledSaveProfile = state.profile?.username?.isNotBlank() == true,
            enabledSaveOrganization = state.organization?.shortName?.isNotBlank() == true,
            currentPage = state.currentPage,
            onSaveProfile = onSaveProfile,
            onSaveOrganization = onSaveOrganization,
            onSaveCalendar = onSaveCalendar,
            onImportSchedule = onImportSchedule,
            onFillOutSchedule = onFillOutSchedule,
            onStartUsing = onStartUsing,
        )
    }
}

@Composable
private fun SetupPageInfoSection(
    modifier: Modifier = Modifier,
    currentPage: SetupPage,
    layoutMode: PreviewLayoutMode,
    profile: ProfileUi?,
    actionWithProfileAvatar: ActionWithAvatar,
    organization: OrganizationUi?,
    actionWithOrganizationAvatar: ActionWithAvatar,
    calendarSettings: CalendarSettingsUi?,
    onUpdateProfile: (ProfileUi) -> Unit,
    onUpdateProfileAvatar: (PlatformFile) -> Unit,
    onDeleteProfileAvatar: () -> Unit,
    onUpdateOrganization: (OrganizationUi) -> Unit,
    onUpdateOrganizationAvatar: (PlatformFile) -> Unit,
    onDeleteOrganizationAvatar: () -> Unit,
    onUpdateCalendarSettings: (CalendarSettingsUi) -> Unit,
    onExceedingAvatarSizeLimit: (Int) -> Unit,
) {
    AnimatedContent(
        targetState = if (profile != null) currentPage else null,
        modifier = modifier.padding(
            horizontal = if (layoutMode == PreviewLayoutMode.EXPANDED) {
                AdaptiveLayoutDefaults.ExpandedHorizontalPadding
            } else {
                24.dp
            },
        ),
        transitionSpec = {
            fadeIn(animationSpec = tween(320, delayMillis = 90)).togetherWith(
                fadeOut(animationSpec = tween(320))
            )
        },
    ) { page ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (page != null) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    SetupStepHeader(
                        title = page.stepTitle,
                        currentStep = page.id.inc(),
                        maxSteps = SetupPage.entries.size,
                        centered = true,
                    )
                    when (page) {
                        SetupPage.PROFILE -> if (profile != null) {
                            ProfilePageInfo(
                                profile = profile,
                                avatar = when (actionWithProfileAvatar) {
                                    is ActionWithAvatar.None -> actionWithProfileAvatar.uri
                                    is ActionWithAvatar.Set -> actionWithProfileAvatar.file.uri
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
                                    is ActionWithAvatar.Set -> actionWithOrganizationAvatar.file.uri
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
    centered: Boolean = true,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = if (centered) 24.dp else 0.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start,
    ) {
        Text(
            text = buildAnnotatedString {
                append(stringResource(Res.string.step_title))
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
            textAlign = if (centered) TextAlign.Center else TextAlign.Start,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun SetupPageNavigationSection(
    modifier: Modifier = Modifier,
    layoutMode: PreviewLayoutMode,
    enabledSaveProfile: Boolean,
    enabledSaveOrganization: Boolean,
    currentPage: SetupPage,
    onSaveProfile: () -> Unit,
    onSaveOrganization: () -> Unit,
    onSaveCalendar: () -> Unit,
    onImportSchedule: () -> Unit,
    onFillOutSchedule: () -> Unit,
    onStartUsing: () -> Unit,
) {
    Column(
        modifier = modifier.padding(
            start = if (layoutMode == PreviewLayoutMode.EXPANDED) {
                AdaptiveLayoutDefaults.ExpandedHorizontalPadding
            } else {
                24.dp
            },
            end = if (layoutMode == PreviewLayoutMode.EXPANDED) {
                AdaptiveLayoutDefaults.ExpandedHorizontalPadding
            } else {
                24.dp
            },
            bottom = 36.dp,
            top = 16.dp,
        )
            .animateContentSize(),
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
                    onClick = onImportSchedule,
                    navigationLabel = stringResource(Res.string.schedule_import_ai_button_label),
                    leadingIcon = Icons.Default.AutoAwesome,
                )
                NavigationPageButton(
                    onClick = onFillOutSchedule,
                    navigationLabel = currentPage.buttonLabel,
                    isTonal = true,
                )
                NavigationPageButton(
                    onClick = onStartUsing,
                    navigationLabel = stringResource(Res.string.schedule_start_button_label),
                    isTonal = true,
                )
                val privacyPolicyDisclaimerStart = stringResource(Res.string.privacy_policy_disclaimer_start)
                val privacyPolicyDisclaimerLink = stringResource(Res.string.privacy_policy_disclaimer_link)
                val privacyPolicyDisclaimerSuffix = stringResource(Res.string.privacy_policy_disclaimer_suffix)
                val annotatedString = buildAnnotatedString {
                    append(privacyPolicyDisclaimerStart)
                    withLink(
                        LinkAnnotation.Url(
                            url = Constants.App.PRIVACY_POLICY,
                            styles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary))
                        )
                    ) {
                        append(privacyPolicyDisclaimerLink)
                    }
                    append(privacyPolicyDisclaimerSuffix)
                }
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = annotatedString,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
