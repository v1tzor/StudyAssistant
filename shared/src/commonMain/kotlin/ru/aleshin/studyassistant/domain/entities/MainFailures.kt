/*
 * Copyright 2023 Stanislav Aleshin
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

package ru.aleshin.studyassistant.domain.entities

import ru.aleshin.studyassistant.core.common.functional.DomainFailures
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapFailure

/**
 * @author Stanislav Aleshin on 27.01.2024.
 */
sealed class MainFailures : DomainFailures {
    data object NetworkError : MainFailures()
    data class IapError(val type: IapFailure) : MainFailures()
    data class OtherError(val throwable: Throwable) : MainFailures()
}