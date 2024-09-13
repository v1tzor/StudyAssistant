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
import ru.aleshin.studyassistant.info.impl.domain.common.InfoEitherWrapper
import ru.aleshin.studyassistant.info.impl.domain.common.InfoErrorHandler
import ru.aleshin.studyassistant.info.impl.domain.interactors.ClassesInfoInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.EmployeesInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.info.impl.domain.interactors.SubjectsInteractor

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<InfoErrorHandler> { InfoErrorHandler.Base() }
    bindSingleton<InfoEitherWrapper> { InfoEitherWrapper.Base(instance(), instance()) }

    bindSingleton<OrganizationsInteractor> { OrganizationsInteractor.Base(instance(), instance(), instance()) }
    bindSingleton<SubjectsInteractor> { SubjectsInteractor.Base(instance(), instance(), instance()) }
    bindSingleton<EmployeesInteractor> { EmployeesInteractor.Base(instance(), instance(), instance()) }
    bindSingleton<ClassesInfoInteractor> { ClassesInfoInteractor.Base(instance(), instance(), instance(), instance()) }
}