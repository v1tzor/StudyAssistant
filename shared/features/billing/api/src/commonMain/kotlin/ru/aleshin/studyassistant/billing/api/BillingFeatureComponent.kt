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

package ru.aleshin.studyassistant.billing.api

import com.arkivanov.decompose.ComponentContext
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.billing.api.BillingFeatureComponent.BillingConfig
import ru.aleshin.studyassistant.billing.api.BillingFeatureComponent.BillingOutput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.FeatureComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig

/**
 * @author Stanislav Aleshin on 25.08.2025.
 */
public abstract class BillingFeatureComponent(
    componentContext: ComponentContext,
    startConfig: StartFeatureConfig<BillingConfig>,
    outputConsumer: OutputConsumer<BillingOutput>,
) : FeatureComponent<BillingConfig, BillingOutput>(
    componentContext = componentContext,
    startConfig = startConfig,
    outputConsumer = outputConsumer,
) {

    @Serializable
    public sealed class BillingConfig {

        @Serializable
        public data object Subscription : BillingConfig()
    }

    public sealed class BillingOutput : BaseOutput {
        public data object NavigateToBack : BillingOutput()
    }
}