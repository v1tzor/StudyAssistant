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

package ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import entities.common.NumberOfRepeatWeek
import functional.UID
import kotlinx.datetime.DayOfWeek

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Parcelize
internal data class BaseScheduleUi(
    val uid: UID,
    val dateVersion: DateVersionUi,
    val dayOfWeek: DayOfWeek,
    val week: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
    val classes: List<ClassUi>,
) : Parcelable