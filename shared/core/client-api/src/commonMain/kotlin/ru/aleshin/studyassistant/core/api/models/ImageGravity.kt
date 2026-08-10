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

package ru.aleshin.studyassistant.core.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
@Serializable
enum class ImageGravity(val value: String) {
    @SerialName("center")
    CENTER("center"),

    @SerialName("top-left")
    TOP_LEFT("top-left"),

    @SerialName("top")
    TOP("top"),

    @SerialName("top-right")
    TOP_RIGHT("top-right"),

    @SerialName("left")
    LEFT("left"),

    @SerialName("right")
    RIGHT("right"),

    @SerialName("bottom-left")
    BOTTOM_LEFT("bottom-left"),

    @SerialName("bottom")
    BOTTOM("bottom"),

    @SerialName("bottom-right")
    BOTTOM_RIGHT("bottom-right");

    override fun toString() = value
}
