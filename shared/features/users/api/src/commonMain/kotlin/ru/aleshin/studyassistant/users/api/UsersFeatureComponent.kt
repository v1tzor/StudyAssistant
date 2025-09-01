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

package ru.aleshin.studyassistant.users.api

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersOutput

/**
 * @author Stanislav Aleshin on 24.08.2025.
 */
public abstract class UsersFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<UsersConfig>,
    outputConsumer: OutputConsumer<UsersOutput>,
) : FeatureComponent<UsersConfig, UsersOutput>(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    @Serializable
    public sealed class UsersConfig {

        @Serializable
        public data object Friends : UsersConfig()

        @Serializable
        public data object Requests : UsersConfig()

        @Serializable
        public data class EmployeeProfile(val employeeId: UID) : UsersConfig()

        @Serializable
        public data class UserProfile(val userId: UID) : UsersConfig()
    }

    public sealed class UsersOutput : BaseOutput {
        public data object NavigateToBack : UsersOutput()

        public data class NavigateToEmployeeEditor(
            val employeeId: UID?,
            val organizationId: UID,
        ) : UsersOutput()
    }
}