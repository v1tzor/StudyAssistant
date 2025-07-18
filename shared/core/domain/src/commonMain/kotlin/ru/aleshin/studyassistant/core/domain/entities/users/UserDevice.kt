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

package ru.aleshin.studyassistant.core.domain.entities.users

import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.messages.PushServiceType
import ru.aleshin.studyassistant.core.common.platform.Platform

/**
 * @author Stanislav Aleshin on 07.08.2024.
 */
data class UserDevice(
    val uid: UID = randomUUID(),
    val platform: Platform,
    val deviceId: String,
    val deviceName: String,
    val pushToken: String? = null,
    val pushServiceType: PushServiceType = PushServiceType.NONE,
) {
    companion object {
        fun specifyDevice(
            uid: UID = randomUUID(),
            platform: Platform,
            deviceId: String,
            deviceName: String,
        ) = UserDevice(
            uid = uid,
            platform = platform,
            deviceId = deviceId,
            deviceName = deviceName,
        )
    }
}