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

package ru.aleshin.studyassistant.preview.impl.presentation.models

import entities.employee.EmployeePost
import functional.UID
import kotlinx.datetime.Instant
import platform.JavaSerializable

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal data class EmployeeUi(
    val uid: UID,
    val firstName: String,
    val secondName: String?,
    val patronymic: String?,
    val post: EmployeePost,
    val avatar: String? = null,
    val birthday: String? = null,
    val workTimeStart: Instant? = null,
    val workTimeEnd: Instant? = null,
    val emails: List<ContactInfoUi> = emptyList(),
    val phones: List<ContactInfoUi> = emptyList(),
    val locations: List<ContactInfoUi> = emptyList(),
    val webs: List<ContactInfoUi> = emptyList(),
) : JavaSerializable
