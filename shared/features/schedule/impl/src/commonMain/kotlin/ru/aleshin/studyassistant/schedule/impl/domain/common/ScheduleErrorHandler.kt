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

package ru.aleshin.studyassistant.schedule.impl.domain.common

import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.handlers.ErrorHandler
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportException
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleTextRecognitionException

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
internal interface ScheduleErrorHandler : ErrorHandler<ScheduleFailures> {

    class Base : ScheduleErrorHandler {

        override fun handle(throwable: Throwable) = when (throwable) {
            is InternetConnectionException -> ScheduleFailures.InternetError
            is AiServiceException.QuotaExceeded -> ScheduleFailures.QuotaExceeded
            is AiServiceException.RateLimited -> ScheduleFailures.RateLimited
            is AiServiceException.InvalidRequest,
            is ScheduleImportException,
            -> ScheduleFailures.InvalidImport
            ScheduleTextRecognitionException.InvalidImage -> ScheduleFailures.InvalidImage
            ScheduleTextRecognitionException.ImageTooLarge -> ScheduleFailures.ImageTooLarge
            ScheduleTextRecognitionException.NoText -> ScheduleFailures.NoTextRecognized
            ScheduleTextRecognitionException.Unavailable -> {
                ScheduleFailures.TextRecognitionUnavailable
            }
            is AiServiceException.ServerUnavailable -> ScheduleFailures.ServerUnavailable
            else -> ScheduleFailures.OtherError(throwable)
        }
    }
}
