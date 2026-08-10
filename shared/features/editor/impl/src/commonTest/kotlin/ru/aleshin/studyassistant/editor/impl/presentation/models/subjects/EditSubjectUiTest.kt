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

package ru.aleshin.studyassistant.editor.impl.presentation.models.subjects

import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
class EditSubjectUiTest {

    @Test
    fun subjectWithoutOptionalData_isValid() {
        val subject = createValidSubject()

        assertTrue(subject.isValid())
    }

    @Test
    fun subjectWithoutRequiredData_isInvalid() {
        val subject = createValidSubject()

        assertFalse(subject.copy(eventType = null).isValid())
        assertFalse(subject.copy(name = "").isValid())
        assertFalse(subject.copy(color = null).isValid())
    }

    @Test
    fun subjectWithoutOptionalData_convertsToBaseModel() {
        val subject = createValidSubject().convertToBase()

        assertNull(subject.teacher)
        assertNull(subject.location)
        assertEquals("", subject.office)
    }

    private fun createValidSubject() = EditSubjectUi(
        uid = "subject-id",
        organizationId = "organization-id",
        eventType = EventType.LECTURE,
        name = "Subject",
        color = 0,
    )
}
