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

package ru.aleshin.studyassistant.backend.ai.schedule.api.validation

import ru.aleshin.studyassistant.backend.ai.schedule.testJpegBytes
import ru.aleshin.studyassistant.backend.ai.schedule.testPngWithDeclaredSize
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
class ScheduleImageGuardTest {

    @Test
    fun compactJpegShouldBeAccepted() {
        assertTrue(ScheduleImageGuard.hasSafeDimensions(testJpegBytes()))
    }

    @Test
    fun undeclaredImageBytesShouldBeRejected() {
        assertFalse(ScheduleImageGuard.hasSafeDimensions(ByteArray(2_048)))
    }

    @Test
    fun hugeDeclaredPngShouldBeRejectedWithoutDecodingRaster() {
        val imageBytes = testPngWithDeclaredSize(width = 20_000, height = 20_000)

        assertFalse(ScheduleImageGuard.hasSafeDimensions(imageBytes))
        assertFailsWith<InvalidRequestException> {
            ScheduleImageGuard.requireSafeDimensions(imageBytes)
        }
    }
}
