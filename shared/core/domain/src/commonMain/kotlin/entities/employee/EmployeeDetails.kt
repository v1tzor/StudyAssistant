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

package entities.employee

import entities.common.ContactInfo
import entities.subject.Subject
import functional.TimeRange
import functional.UID

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
data class EmployeeDetails(
    val uid: UID,
    val organizationId: UID,
    val firstName: String,
    val secondName: String?,
    val patronymic: String?,
    val post: EmployeePost,
    val subjects: List<Subject>,
    val avatar: String? = null,
    val birthday: String? = null,
    val workTime: TimeRange? = null,
    val emails: List<ContactInfo> = emptyList(),
    val phones: List<ContactInfo> = emptyList(),
    val locations: List<ContactInfo> = emptyList(),
    val webs: List<ContactInfo> = emptyList(),
)

fun Employee.convertToDetails(subjects: List<Subject>) = EmployeeDetails(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    subjects = subjects,
    avatar = avatar,
    birthday = birthday,
    workTime = workTime,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
)

fun EmployeeDetails.convertToBase() = Employee(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    avatar = avatar,
    birthday = birthday,
    workTime = workTime,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
)