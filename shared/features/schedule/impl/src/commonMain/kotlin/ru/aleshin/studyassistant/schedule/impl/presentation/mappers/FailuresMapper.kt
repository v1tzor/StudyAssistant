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

package ru.aleshin.studyassistant.schedule.impl.presentation.mappers

import org.jetbrains.compose.resources.getString
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.other_error_message
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_image_too_large_error
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_invalid_error
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_invalid_image_error
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_no_text_error
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_ocr_unavailable_error
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_quota_error
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_rate_limit_error
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_server_error
import ru.aleshin.studyassistant.schedule.impl.resources.share_daily_limit_message
import ru.aleshin.studyassistant.schedule.impl.resources.share_item_limit_message
import ru.aleshin.studyassistant.schedule.impl.resources.share_payload_limit_message
import ru.aleshin.studyassistant.schedule.impl.resources.share_rate_limit_message
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.network_error_message as core_network_error_message

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal suspend fun ScheduleFailures.mapToMessage() = when (this) {
    is ScheduleFailures.InternetError -> getString(CoreRes.string.core_network_error_message)
    is ScheduleFailures.QuotaExceeded -> getString(Res.string.schedule_import_quota_error)
    is ScheduleFailures.RateLimited -> getString(Res.string.schedule_import_rate_limit_error)
    is ScheduleFailures.InvalidImport -> getString(Res.string.schedule_import_invalid_error)
    is ScheduleFailures.InvalidImage -> getString(Res.string.schedule_import_invalid_image_error)
    is ScheduleFailures.ImageTooLarge -> getString(Res.string.schedule_import_image_too_large_error)
    is ScheduleFailures.NoTextRecognized -> getString(Res.string.schedule_import_no_text_error)
    is ScheduleFailures.TextRecognitionUnavailable -> {
        getString(Res.string.schedule_import_ocr_unavailable_error)
    }
    is ScheduleFailures.ServerUnavailable -> getString(Res.string.schedule_import_server_error)
    is ScheduleFailures.OtherError -> when (throwable) {
        is ShareException.RateLimit -> getString(Res.string.share_rate_limit_message)
        is ShareException.ShareLimit -> getString(Res.string.share_daily_limit_message)
        is ShareException.ItemLimit -> getString(Res.string.share_item_limit_message)
        is ShareException.PayloadTooLarge -> getString(Res.string.share_payload_limit_message)
        else -> getString(Res.string.other_error_message)
    }
}
