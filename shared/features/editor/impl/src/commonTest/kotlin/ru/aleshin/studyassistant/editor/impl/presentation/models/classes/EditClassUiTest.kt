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

package ru.aleshin.studyassistant.editor.impl.presentation.models.classes

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
class EditClassUiTest {

    @Test
    fun classWithoutOptionalData_isValid() {
        val classModel = createValidClass()

        assertTrue(classModel.isValid())
    }

    @Test
    fun classWithoutRequiredData_isInvalid() {
        val classModel = createValidClass()

        assertFalse(classModel.copy(organization = null).isValid())
        assertFalse(classModel.copy(eventType = null).isValid())
        assertFalse(classModel.copy(subject = null).isValid())
        assertFalse(classModel.copy(startTime = null).isValid())
        assertFalse(classModel.copy(endTime = null).isValid())
    }

    @Test
    fun classWithoutOptionalData_convertsToBaseModel() {
        val classModel = createValidClass().convertToBase()

        assertNull(classModel.teacher)
        assertNull(classModel.location)
        assertEquals("", classModel.office)
    }

    private fun createValidClass() = EditClassUi(
        uid = "class-id",
        scheduleId = "schedule-id",
        organization = organization,
        eventType = EventType.LECTURE,
        subject = subject,
        startTime = Instant.fromEpochMilliseconds(0L),
        endTime = Instant.fromEpochMilliseconds(3_600_000L),
    )

    private val organization = OrganizationShortUi(
        uid = "organization-id",
        shortName = "Organization",
    )

    private val subject = SubjectUi(
        uid = "subject-id",
        organizationId = organization.uid,
        eventType = EventType.LECTURE,
        name = "Subject",
        teacher = null,
        office = "",
        color = 0,
        location = null,
    )
}
