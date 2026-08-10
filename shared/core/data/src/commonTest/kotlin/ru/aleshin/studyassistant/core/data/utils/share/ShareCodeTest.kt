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

package ru.aleshin.studyassistant.core.data.utils.share

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class ShareCodeTest {

    @Test
    fun normalize_acceptsFormattingAndCrockfordAliases() {
        assertEquals("01AB23456789", ShareCode.normalize("o-iab 2345-6789"))
    }

    @Test
    fun format_groupsTwelveCharacters() {
        assertEquals("01AB-2345-6789", ShareCode.format("01AB23456789"))
    }

    @Test
    fun normalize_rejectsInvalidLengthAndAlphabet() {
        assertFailsWith<IllegalArgumentException> { ShareCode.normalize("ABC") }
        assertFailsWith<IllegalArgumentException> { ShareCode.normalize("01AB-2345-678U") }
    }

}
