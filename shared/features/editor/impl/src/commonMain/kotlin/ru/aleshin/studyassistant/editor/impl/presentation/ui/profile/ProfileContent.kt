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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.SocialNetworkUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.BirthdayInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.AppUserEmailInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.CityInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.DescriptionInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.GenderInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.SocialNetworksInfoFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.UsernameInfoField

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
@Composable
internal fun ProfileContent(
    state: ProfileViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onUpdateName: (String) -> Unit,
    onUpdateDescription: (String?) -> Unit,
    onUpdateBirthday: (String?) -> Unit,
    onUpdateGender: (Gender?) -> Unit,
    onUpdateCity: (String?) -> Unit,
    onUpdateSocialNetworks: (List<SocialNetworkUi>) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(top = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        UsernameInfoField(
            isLoading = isLoading,
            username = appUser?.username ?: "",
            onUpdateName = onUpdateName,
        )
        DescriptionInfoField(
            isLoading = isLoading,
            description = appUser?.description,
            onUpdateDescription = onUpdateDescription,
        )
        AppUserEmailInfoField(
            isLoading = isLoading,
            email = appUser?.email ?: "",
        )
        BirthdayInfoField(
            isLoading = isLoading,
            birthday = appUser?.birthday,
            onSelected = onUpdateBirthday,
        )
        GenderInfoField(
            isLoading = isLoading,
            gender = appUser?.gender,
            onUpdateGender = onUpdateGender,
        )
        CityInfoField(
            isLoading = isLoading,
            city = appUser?.city,
            onUpdateCity = onUpdateCity,
        )
        SocialNetworksInfoFields(
            isLoading = isLoading,
            socialNetworks = appUser?.socialNetworks ?: emptyList(),
            onUpdateSocialNetworks = onUpdateSocialNetworks,
        )
        Spacer(modifier = Modifier.height(60.dp))
    }
}