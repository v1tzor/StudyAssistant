/*
 * Copyright 2025 Stanislav Aleshin
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

package ru.aleshin.studyassistant.settings.api

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponent.SettingsConfig
import ru.aleshin.studyassistant.settings.api.SettingsFeatureComponent.SettingsOutput

/**
 * @author Stanislav Aleshin on 26.08.2025.
 */
public abstract class SettingsFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<SettingsConfig>,
    outputConsumer: OutputConsumer<SettingsOutput>,
) : FeatureComponent<SettingsConfig, SettingsOutput>(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    @Serializable
    public sealed class SettingsConfig {

        @Serializable
        public data object General : SettingsConfig()

        @Serializable
        public data object Notification : SettingsConfig()

        @Serializable
        public data object Calendar : SettingsConfig()

        @Serializable
        public data object Subscription : SettingsConfig()

        @Serializable
        public data object AboutApp : SettingsConfig()
    }

    public sealed class SettingsOutput : BaseOutput {
        public data object NavigateToBack : SettingsOutput()
        public data object NavigateToBilling : SettingsOutput()
    }
}