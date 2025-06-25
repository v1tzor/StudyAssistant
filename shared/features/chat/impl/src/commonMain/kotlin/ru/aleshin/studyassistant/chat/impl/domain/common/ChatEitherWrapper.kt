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

package ru.aleshin.studyassistant.chat.impl.domain.common

import ru.aleshin.studyassistant.chat.impl.domain.entities.ChatFailures
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.common.wrappers.FlowEitherWrapper

/**
 * @author Stanislav Aleshin on 27.05.2024
 */
internal interface ChatEitherWrapper : FlowEitherWrapper<ChatFailures> {

    class Base(
        errorHandler: ChatErrorHandler,
        crashlyticsService: CrashlyticsService,
    ) : ChatEitherWrapper, FlowEitherWrapper.Abstract<ChatFailures>(
        errorHandler = errorHandler,
        crashlyticsService = crashlyticsService,
    )
}