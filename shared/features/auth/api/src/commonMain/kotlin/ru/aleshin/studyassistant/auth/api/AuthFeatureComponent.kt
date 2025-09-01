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

package ru.aleshin.studyassistant.auth.api

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent.AuthConfig
import ru.aleshin.studyassistant.auth.api.AuthFeatureComponent.AuthOutput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig

/**
 * @author Stanislav Aleshin on 24.08.2025.
 */
public abstract class AuthFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<AuthConfig>,
    outputConsumer: OutputConsumer<AuthOutput>,
) : FeatureComponent<AuthConfig, AuthOutput>(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    @Serializable
    public sealed class AuthConfig {

        @Serializable
        public data object Login : AuthConfig()

        @Serializable
        public data object Register : AuthConfig()

        @Serializable
        public data object Forgot : AuthConfig()

        @Serializable
        public data object Verification : AuthConfig()
    }

    public sealed class AuthOutput : BaseOutput {
        public data object NavigateToBack : AuthOutput()
        public data object NavigateToFirstSetup : AuthOutput()
        public data object NavigateToApp : AuthOutput()
    }
}