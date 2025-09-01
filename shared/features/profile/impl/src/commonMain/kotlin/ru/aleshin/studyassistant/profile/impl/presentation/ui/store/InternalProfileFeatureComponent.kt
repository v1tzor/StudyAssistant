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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponent
import ru.aleshin.studyassistant.profile.impl.di.holder.ProfileFeatureManager
import ru.aleshin.studyassistant.profile.impl.presentation.ui.ProfileContentProvider
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileState

/**
 * @author Stanislav Aleshin on 24.08.2025.
 */
internal abstract class InternalProfileFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<ProfileConfig>,
    outputConsumer: OutputConsumer<ProfileOutput>
) : ProfileFeatureComponent(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    abstract val store: ProfileComposeStore

    class Default(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<ProfileConfig>,
        outputConsumer: OutputConsumer<ProfileOutput>,
        storeFactory: ProfileComposeStore.Factory
    ) : InternalProfileFeatureComponent(
        componentContext = componentContext,
        startConfig = startConfig,
        outputConsumer = outputConsumer,
    ) {

        override val contentProvider = ProfileContentProvider(this)

        override val store by saveableStore(
            storeFactory = storeFactory,
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

        override fun onDestroyInstance() {
            ProfileFeatureManager.finish()
        }
    }
}