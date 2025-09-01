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

package ru.aleshin.studyassistant.profile.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponent
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponent.ProfileConfig
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponent.ProfileOutput
import ru.aleshin.studyassistant.profile.api.ProfileFeatureComponentFactory
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.InternalProfileFeatureComponent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.ProfileComposeStore

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultProfileComponentFactory(
    private val profileStoreFactory: ProfileComposeStore.Factory,
) : ProfileFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<ProfileConfig>,
        outputConsumer: OutputConsumer<ProfileOutput>
    ): ProfileFeatureComponent {
        return InternalProfileFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            storeFactory = profileStoreFactory,
        )
    }
}