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

package ru.aleshin.studyassistant.tasks.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksErrorHandler
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.HomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.TodoInteractor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<TasksErrorHandler> { TasksErrorHandler.Base() }
    bindSingleton<TasksEitherWrapper> { TasksEitherWrapper.Base(instance()) }

    bindSingleton<HomeworksInteractor> { HomeworksInteractor.Base(instance(), instance(), instance(), instance(), instance(), instance()) }
    bindSingleton<TodoInteractor> { TodoInteractor.Base(instance(), instance(), instance()) }
    bindSingleton<ScheduleInteractor> { ScheduleInteractor.Base(instance(), instance(), instance(), instance(), instance()) }
}