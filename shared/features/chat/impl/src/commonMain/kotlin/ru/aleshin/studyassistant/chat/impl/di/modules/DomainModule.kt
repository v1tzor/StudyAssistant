/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.chat.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.chat.impl.domain.common.ChatEitherWrapper
import ru.aleshin.studyassistant.chat.impl.domain.common.ChatErrorHandler
import ru.aleshin.studyassistant.chat.impl.domain.interactors.AiAssistantInteractor
import ru.aleshin.studyassistant.chat.impl.domain.tools.AiToolCallProcessor
import ru.aleshin.studyassistant.chat.impl.domain.tools.AiToolCallStateResolver
import ru.aleshin.studyassistant.chat.impl.domain.tools.validation.AiToolArgumentsValidator

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val domainModule = DI.Module("Domain") {
    bindSingleton<ChatErrorHandler> { ChatErrorHandler.Base() }
    bindSingleton<ChatEitherWrapper> { ChatEitherWrapper.Base(instance(), instance()) }

    bindSingleton<AiToolArgumentsValidator> { AiToolArgumentsValidator.Base() }
    bindSingleton<AiToolCallStateResolver> { AiToolCallStateResolver.Base() }
    bindSingleton<AiToolCallProcessor> {
        AiToolCallProcessor.Base(
            todoRepository = instance(),
            homeworksRepository = instance(),
            subjectsRepository = instance(),
            organizationsRepository = instance(),
            baseScheduleRepository = instance(),
            customScheduleRepository = instance(),
            employeeRepository = instance(),
            calendarSettingsRepository = instance(),
            todoReminderManager = instance(),
            notificationSettingsRepository = instance(),
            startClassesReminderManager = instance(),
            endClassesReminderManager = instance(),
            profileRepository = instance(),
            dateManager = instance(),
            validator = instance(),
            stateResolver = instance(),
        )
    }
    bindSingleton<AiAssistantInteractor> {
        AiAssistantInteractor.Base(
            aiAssistantRepository = instance(),
            aiSettingsRepository = instance(),
            adRewardRepository = instance(),
            toolCallProcessor = instance(),
            dateManager = instance(),
            eitherWrapper = instance(),
        )
    }
}
