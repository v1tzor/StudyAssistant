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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer

import ru.aleshin.studyassistant.core.ui.ads.RewardedAdSession
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
class RewardedAdSessionTest {

    @AfterTest
    fun tearDown() {
        RewardedAdSession.clear("challenge-a")
        RewardedAdSession.clear("challenge-b")
    }

    @Test
    fun rewardedKeySurvivesUntilCleared() {
        RewardedAdSession.markPresented("challenge-a")
        assertTrue(RewardedAdSession.isPresented("challenge-a"))
        assertFalse(RewardedAdSession.hasRewarded("challenge-a"))

        RewardedAdSession.markRewarded("challenge-a")
        assertTrue(RewardedAdSession.hasRewarded("challenge-a"))
        assertTrue(RewardedAdSession.isPresented("challenge-a"))
        assertFalse(RewardedAdSession.hasRewarded("challenge-b"))

        RewardedAdSession.clear("challenge-b")
        assertTrue(RewardedAdSession.hasRewarded("challenge-a"))

        RewardedAdSession.clear("challenge-a")
        assertFalse(RewardedAdSession.hasRewarded("challenge-a"))
        assertFalse(RewardedAdSession.isPresented("challenge-a"))
    }
}
