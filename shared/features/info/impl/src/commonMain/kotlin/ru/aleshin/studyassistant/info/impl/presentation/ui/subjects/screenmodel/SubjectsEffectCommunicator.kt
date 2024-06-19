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

package ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.screenmodel

import architecture.communications.state.EffectCommunicator
import ru.aleshin.studyassistant.info.impl.presentation.ui.subjects.contract.SubjectsEffect

/**
 * @author Stanislav Aleshin on 17.06.2024.
 */
internal interface SubjectsEffectCommunicator : EffectCommunicator<SubjectsEffect> {
    class Base : SubjectsEffectCommunicator, EffectCommunicator.Abstract<SubjectsEffect>()
}