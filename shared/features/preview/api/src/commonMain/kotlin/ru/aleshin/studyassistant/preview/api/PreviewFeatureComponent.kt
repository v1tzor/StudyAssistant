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

package ru.aleshin.studyassistant.preview.api

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent.PreviewConfig
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponent.PreviewOutput

/**
 * @author Stanislav Aleshin on 20.08.2025.
 */
public abstract class PreviewFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<PreviewConfig>,
    outputConsumer: OutputConsumer<PreviewOutput>,
) : FeatureComponent<PreviewConfig, PreviewOutput>(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    @Serializable
    public sealed class PreviewConfig {

        @Serializable
        public data object Intro : PreviewConfig()

        @Serializable
        public data object Setup : PreviewConfig()
    }

    public sealed class PreviewOutput : BaseOutput {
        public data object NavigateToApp : PreviewOutput()
        public data object NavigateToLogin : PreviewOutput()
        public data object NavigateToRegister : PreviewOutput()
        public data object NavigateToBilling : PreviewOutput()
        public data object NavigateToWeekScheduleEditor : PreviewOutput()
        public data object NavigateToBack : PreviewOutput()
    }
}