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

package ru.aleshin.studyassistant.info.impl.navigation

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.inject.StartFeatureConfig
import ru.aleshin.studyassistant.info.api.InfoFeatureComponent
import ru.aleshin.studyassistant.info.api.InfoFeatureComponentFactory
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.store.EmployeeComposeStore
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.store.OrganizationsComposeStore
import ru.aleshin.studyassistant.info.impl.presentation.ui.root.InternalInfoFeatureComponent
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store.SubjectsComposeStore

/**
 * @author Stanislav Aleshin on 28.08.2025.
 */
internal class DefaultInfoComponentFactory(
    private val organizationsStoreFactory: OrganizationsComposeStore.Factory,
    private val employeeStoreFactory: EmployeeComposeStore.Factory,
    private val subjectsStoreFactory: SubjectsComposeStore.Factory,
) : InfoFeatureComponentFactory {

    override fun createComponent(
        componentContext: ComponentContext,
        startConfig: StartFeatureConfig<InfoFeatureComponent.InfoConfig>,
        outputConsumer: OutputConsumer<InfoFeatureComponent.InfoOutput>
    ): InfoFeatureComponent {
        return InternalInfoFeatureComponent.Default(
            componentContext = componentContext,
            startConfig = startConfig,
            outputConsumer = outputConsumer,
            organizationsStoreFactory = organizationsStoreFactory,
            employeeStoreFactory = employeeStoreFactory,
            subjectsStoreFactory = subjectsStoreFactory,
        )
    }
}