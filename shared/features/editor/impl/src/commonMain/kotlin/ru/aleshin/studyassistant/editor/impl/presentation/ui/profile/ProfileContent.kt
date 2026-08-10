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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.BirthdayInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.store.ProfileComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.CityInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.DescriptionInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.GenderInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.ProfileTopBar
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.ProfileTopSheet
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.UsernameInfoField
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.exceeding_limit_image_size_message as core_exceeding_limit_image_size_message

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
@Composable
internal fun ProfileContent(
    profileComponent: ProfileComponent,
    modifier: Modifier = Modifier
) {
    val store = profileComponent.store
    val state by store.stateAsState()
    val coreExceedingLimitImageSizeMessage = stringResource(CoreRes.string.core_exceeding_limit_image_size_message)
    val coroutineScope = rememberCoroutineScope()
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseProfileContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onUpdateName = { store.dispatchEvent(ProfileEvent.UpdateUsername(it)) },
                onUpdateDescription = { store.dispatchEvent(ProfileEvent.UpdateDescription(it)) },
                onUpdateBirthday = { store.dispatchEvent(ProfileEvent.UpdateBirthday(it)) },
                onUpdateGender = { store.dispatchEvent(ProfileEvent.UpdateGender(it)) },
                onUpdateCity = { store.dispatchEvent(ProfileEvent.UpdateCity(it)) },
            )
        },
        topBar = {
            Column {
                ProfileTopBar(
                    onBackClick = { store.dispatchEvent(ProfileEvent.NavigateToBack) },
                )
                ProfileTopSheet(
                    isLoading = state.isLoading,
                    profile = state.profile,
                    onUpdateAvatar = { file -> store.dispatchEvent(ProfileEvent.UpdateAvatar(file)) },
                    onDeleteAvatar = { store.dispatchEvent(ProfileEvent.DeleteAvatar) },
                    onExceedingLimit = {
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
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is ProfileEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseProfileContent(
    state: ProfileState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateName: (String) -> Unit,
    onUpdateDescription: (String?) -> Unit,
    onUpdateBirthday: (String?) -> Unit,
    onUpdateGender: (Gender?) -> Unit,
    onUpdateCity: (String?) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        UsernameInfoField(
            isLoading = state.isLoading,
            username = state.profile?.username ?: "",
            onUpdateName = onUpdateName,
        )
        DescriptionInfoField(
            isLoading = state.isLoading,
            description = state.profile?.description,
            onUpdateDescription = onUpdateDescription,
        )
        BirthdayInfoField(
            isLoading = state.isLoading,
            birthday = state.profile?.birthday,
            onSelected = onUpdateBirthday,
        )
        GenderInfoField(
            isLoading = state.isLoading,
            gender = state.profile?.gender,
            onUpdateGender = onUpdateGender,
        )
        CityInfoField(
            isLoading = state.isLoading,
            city = state.profile?.city,
            onUpdateCity = onUpdateCity,
        )
        Spacer(modifier = Modifier.height(60.dp))
    }
}
