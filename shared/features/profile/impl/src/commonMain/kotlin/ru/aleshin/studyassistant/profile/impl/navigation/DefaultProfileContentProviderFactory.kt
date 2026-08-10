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

package ru.aleshin.studyassistant.profile.impl.navigation

import com.arkivanov.decompose.ComponentContext
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.on
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponentDeps
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.FeatureContentProvider
import ru.aleshin.studyassistant.profile.api.ProfileConfig
import ru.aleshin.studyassistant.profile.api.ProfileContentProviderFactory
import ru.aleshin.studyassistant.profile.api.ProfileOutput
import ru.aleshin.studyassistant.profile.impl.presentation.ui.ProfileContentProvider
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.ProfileFeatureComponent

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultProfileContentProviderFactory(
    private val di: DI
) : ProfileContentProviderFactory {

    override fun createProvider(
        componentContext: ComponentContext,
        startConfig: ProfileConfig,
        outputConsumer: OutputConsumer<ProfileOutput>
    ): FeatureContentProvider {
        val deps = ProfileComponentDeps(startConfig, outputConsumer)
        val component by di.on(componentContext).instance<ProfileComponentDeps, ProfileFeatureComponent>(arg = deps)

        return ProfileContentProvider(di, component)
    }
}

internal typealias ProfileComponentDeps = FeatureComponentDeps<ProfileConfig, ProfileOutput>