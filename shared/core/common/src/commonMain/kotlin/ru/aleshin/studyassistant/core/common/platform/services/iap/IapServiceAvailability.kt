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

@file:OptIn(ExperimentalObjCName::class)

package ru.aleshin.studyassistant.core.common.platform.services.iap

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
@ObjCName("IapServiceAvailability", exact = true)
sealed class IapServiceAvailability {
    @ObjCName("IapServiceAvailabilityAvailable", exact = true)
    data object Available : IapServiceAvailability()

    @ObjCName("IapServiceAvailabilityUnavailable", exact = true)
    data class Unavailable(val throwable: Throwable?) : IapServiceAvailability()
}