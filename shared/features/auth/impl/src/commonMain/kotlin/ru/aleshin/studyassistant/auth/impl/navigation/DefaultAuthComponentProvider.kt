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

package ru.aleshin.studyassistant.auth.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.auth.api.navigation.AuthComponentProvider
import ru.aleshin.studyassistant.auth.api.presentation.BaseAuthRootComponent
import ru.aleshin.studyassistant.auth.impl.presentation.ui.navigation.AuthRootComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer

/**
 * @author Stanislav Aleshin on 22.08.2025.
 */
internal class DefaultAuthComponentProvider(
    private val componentSingletonFactory: (AuthComponentProviderArgs) -> AuthRootComponent
) : AuthComponentProvider {

    override fun provideRootComponent(
        componentContext: ComponentContext,
        startConfig: List<BaseAuthRootComponent.Config>?,
        outputConsumer: OutputConsumer<BaseAuthRootComponent.Output>
    ): AuthRootComponent {
        val args = AuthComponentProviderArgs(componentContext, startConfig, outputConsumer)
        return componentSingletonFactory(args)
    }
}

internal data class AuthComponentProviderArgs(
    val componentContext: ComponentContext,
    val startConfig: List<BaseAuthRootComponent.Config>?,
    val outputConsumer: OutputConsumer<BaseAuthRootComponent.Output>,
)