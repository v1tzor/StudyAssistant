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

package ru.aleshin.studyassistant.info.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.info.api.InfoFeatureComponentFactory
import ru.aleshin.studyassistant.info.impl.navigation.DefaultInfoComponentFactory
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.store.EmployeeComposeStore
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.store.EmployeeWorkProcessor
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.store.OrganizationsComposeStore
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.store.OrganizationsWorkProcessor
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store.SubjectsComposeStore
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.store.SubjectsWorkProcessor

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<InfoFeatureComponentFactory> { DefaultInfoComponentFactory(instance(), instance(), instance()) }

    bindSingleton<OrganizationsWorkProcessor> { OrganizationsWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<OrganizationsComposeStore.Factory> { OrganizationsComposeStore.Factory(instance(), instance()) }

    bindSingleton<SubjectsWorkProcessor> { SubjectsWorkProcessor.Base(instance(), instance()) }
    bindSingleton<SubjectsComposeStore.Factory> { SubjectsComposeStore.Factory(instance(), instance()) }

    bindSingleton<EmployeeWorkProcessor> { EmployeeWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<EmployeeComposeStore.Factory> { EmployeeComposeStore.Factory(instance(), instance()) }
}