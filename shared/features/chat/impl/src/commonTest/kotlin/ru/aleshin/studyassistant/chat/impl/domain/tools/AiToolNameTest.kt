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

package ru.aleshin.studyassistant.chat.impl.domain.tools

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AiToolNameTest {

    @Test
    fun `only write tools require confirmation`() {
        assertTrue(AiToolName.CREATE_TODO.mutatesData)
        assertTrue(AiToolName.CREATE_HOMEWORK.mutatesData)
        assertTrue(AiToolName.CREATE_CLASS.mutatesData)
        assertTrue(AiToolName.UPDATE_CLASS.mutatesData)
        assertFalse(AiToolName.GET_HOMEWORKS.mutatesData)
        assertFalse(AiToolName.GET_CLASSES_BY_DATE.mutatesData)
    }

    @Test
    fun removedToolsAreNotInClientCatalog() {
        val removed = setOf(
            "get_free_time",
            "delete_todo",
            "delete_homework",
            "delete_class",
            "delete_goal",
        )
        assertTrue(AiToolName.supportedWireNames.none { name -> name in removed })
        assertTrue("create_homework" in AiToolName.supportedWireNames)
        assertTrue("get_near_class" in AiToolName.supportedWireNames)
        assertTrue("get_subjects" in AiToolName.supportedWireNames)
    }
}
