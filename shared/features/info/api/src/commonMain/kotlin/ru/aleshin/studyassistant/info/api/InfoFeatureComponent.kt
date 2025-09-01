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

package ru.aleshin.studyassistant.info.api

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.info.api.InfoFeatureComponent.InfoConfig
import ru.aleshin.studyassistant.info.api.InfoFeatureComponent.InfoOutput

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
public abstract class InfoFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<InfoConfig>,
    outputConsumer: OutputConsumer<InfoOutput>,
) : FeatureComponent<InfoConfig, InfoOutput>(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    @Serializable
    public sealed class InfoConfig {

        @Serializable
        public data object Organizations : InfoConfig()

        @Serializable
        public data class Employee(val organizationId: UID) : InfoConfig()

        @Serializable
        public data class Subjects(val organizationId: UID) : InfoConfig()
    }

    public sealed class InfoOutput : BaseOutput {
        public data object NavigateToBack : InfoOutput()

        public data class NavigateToEmployeeProfile(val employeeId: UID) : InfoOutput()

        public data object NavigateToBilling : InfoOutput()

        public sealed class NavigateToEditor : InfoOutput() {

            public data class Subject(
                val subjectId: UID?,
                val organizationId: UID,
            ) : NavigateToEditor()

            public data class Employee(
                val employeeId: UID?,
                val organizationId: UID,
            ) : NavigateToEditor()

            public data class Organization(
                val organizationId: UID?,
            ) : NavigateToEditor()
        }
    }
}