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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.classes

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import entities.organizations.Millis
import functional.UID
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views.Progress

/**
 * @author Stanislav Aleshin on 13.06.2024.
 */
@Parcelize
internal data class ActiveClassUi(
    val uid: UID,
    val isStarted: Boolean,
    val progress: Progress,
    val duration: Millis,
) : Parcelable