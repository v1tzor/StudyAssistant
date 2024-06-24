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

package entities.tasks

import functional.Constants.Date
import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
enum class HomeworkStatus {
    COMPLETE, WAIT, IN_FUTURE, NOT_COMPLETE;

    companion object {
        fun calculate(isDone: Boolean, deadline: Instant?, currentTime: Instant): HomeworkStatus {
            return if (isDone) {
                COMPLETE
            } else {
                if (deadline != null) {
                    val duration = deadline - currentTime
                    if (duration.isPositive()) {
                        if (duration.inWholeMilliseconds <= Date.MILLIS_IN_DAY) {
                            WAIT
                        } else {
                            IN_FUTURE
                        }
                    } else {
                        NOT_COMPLETE
                    }
                } else {
                    NOT_COMPLETE
                }
            }
        }
    }
}