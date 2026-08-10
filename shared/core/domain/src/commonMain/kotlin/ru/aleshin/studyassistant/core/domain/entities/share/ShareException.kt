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

package ru.aleshin.studyassistant.core.domain.entities.share

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
sealed class ShareException(message: String) : RuntimeException(message) {
    class InvalidCode : ShareException("Share code is invalid")
    class Expired : ShareException("Share has expired")
    class Claimed : ShareException("Schedule share is being imported")
    class Consumed : ShareException("Schedule share was already imported")
    class Duplicate : ShareException("Homework share was already imported on this device")
    class ItemLimit : ShareException("Share contains too many items")
    class PayloadTooLarge : ShareException("Share payload exceeds 1 MiB")
    class RateLimit : ShareException("Too many share requests")
    class ShareLimit : ShareException("Daily share limit reached")
    class ServerUnavailable : ShareException("Sharing service is unavailable")
}
