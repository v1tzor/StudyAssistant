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

package ru.aleshin.studyassistant.core.api.models

import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
@Serializable
data class UploadProgressPojo(
    val id: String,
    val progress: Double,
    val sizeUploaded: Long,
    val chunksTotal: Int,
    val chunksUploaded: Int
)