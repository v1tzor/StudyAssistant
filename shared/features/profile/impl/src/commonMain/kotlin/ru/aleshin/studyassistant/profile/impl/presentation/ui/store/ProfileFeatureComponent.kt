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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.profile.api.ProfileConfig
import ru.aleshin.studyassistant.profile.api.ProfileOutput
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileState

/**
 * @author Stanislav Aleshin on 24.08.2025.
 */
internal abstract class ProfileFeatureComponent(
    componentContext: ComponentContext,
    protected val startConfig: ProfileConfig,
    protected val outputConsumer: OutputConsumer<ProfileOutput>,
) : FeatureComponent<ProfileConfig, ProfileOutput>(componentContext) {

    abstract val store: ProfileComposeStore

    class Default(
        componentContext: ComponentContext,
        startConfig: ProfileConfig,
        outputConsumer: OutputConsumer<ProfileOutput>,
        profileStoreFactory: ProfileComposeStore.Factory
    ) : ProfileFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {

        override val store by saveableStore(
            storeFactory = profileStoreFactory,
            defaultState = ProfileState(),
            stateSerializer = ProfileState.serializer(),
            outputConsumer = outputConsumer,
            storeKey = COMPONENT_KEY,
        )

        private companion object {
            const val COMPONENT_KEY = "PROFILE_COMPONENT_KEY"
        }

        override fun navigateToBack() {
            outputConsumer.consume(ProfileOutput.NavigateToBack)
        }
    }
}