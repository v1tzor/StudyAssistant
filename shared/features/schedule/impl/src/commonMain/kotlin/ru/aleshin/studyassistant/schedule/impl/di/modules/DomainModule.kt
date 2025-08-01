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

package ru.aleshin.studyassistant.schedule.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleErrorHandler
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.AnalysisInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.HomeworkInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.OrganizationsInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.schedule.impl.domain.interactors.ShareSchedulesInteractor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<ScheduleErrorHandler> { ScheduleErrorHandler.Base() }
    bindSingleton<ScheduleEitherWrapper> { ScheduleEitherWrapper.Base(instance(), instance()) }

    bindSingleton<ScheduleInteractor> { ScheduleInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<HomeworkInteractor> { HomeworkInteractor.Base(instance(), instance(), instance()) }
    bindSingleton<ShareSchedulesInteractor> { ShareSchedulesInteractor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<OrganizationsInteractor> { OrganizationsInteractor.Base(instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<AnalysisInteractor> { AnalysisInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance()) }
}