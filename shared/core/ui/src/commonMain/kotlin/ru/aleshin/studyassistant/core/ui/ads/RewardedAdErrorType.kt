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

package ru.aleshin.studyassistant.core.ui.ads

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
enum class RewardedAdErrorType {
    NO_FILL,
    NETWORK_ERROR,
    INVALID_REQUEST,
    INTERNAL_ERROR,
    UNKNOWN_LOAD_ERROR,
    SHOW_FAILED;

    companion object {
        fun parse(throwable: Throwable): RewardedAdErrorType {
            return parse(throwable.message?.lowercase())
        }

        fun parse(description: String?): RewardedAdErrorType {
            val msg = description?.lowercase() ?: return UNKNOWN_LOAD_ERROR

            return when {
                msg.contains("no fill") || msg.contains("no_fill") || msg.contains("code: 4") || msg.contains("code=4") -> NO_FILL
                msg.contains("network") || msg.contains("connection") || msg.contains("code: 3") || msg.contains("code=3") -> NETWORK_ERROR
                msg.contains("invalid") || msg.contains("code: 2") || msg.contains("code=2") -> INVALID_REQUEST
                msg.contains("internal") || msg.contains("system") || msg.contains("code: 1") || msg.contains("code: 5") -> INTERNAL_ERROR
                else -> UNKNOWN_LOAD_ERROR
            }
        }
    }
}