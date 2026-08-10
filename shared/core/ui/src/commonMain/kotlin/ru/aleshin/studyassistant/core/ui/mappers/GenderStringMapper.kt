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

package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.users.Gender
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.female_gender as core_female_gender
import ru.aleshin.studyassistant.core.ui.resources.male_gender as core_male_gender
import ru.aleshin.studyassistant.core.ui.resources.none_gender as core_none_gender

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
@Composable
fun Gender.mapToSting() = when (this) {
    Gender.NONE -> stringResource(CoreRes.string.core_none_gender)
    Gender.FEMALE -> stringResource(CoreRes.string.core_female_gender)
    Gender.MALE -> stringResource(CoreRes.string.core_male_gender)
}