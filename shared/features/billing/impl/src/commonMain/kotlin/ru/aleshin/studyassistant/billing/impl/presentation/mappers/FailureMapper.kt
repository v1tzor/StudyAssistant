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

package ru.aleshin.studyassistant.billing.impl.presentation.mappers

import ru.aleshin.studyassistant.billing.impl.domain.entities.BillingFailures
import ru.aleshin.studyassistant.billing.impl.presentation.theme.tokens.BillingStrings
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings

/**
 * @author Stanislav Aleshin on 17.06.2025.
 */
internal fun BillingFailures.mapToMessage(
    strings: BillingStrings,
    coreStrings: StudyAssistantStrings,
) = when (this) {
    is BillingFailures.PaymentError -> type.mapToString(coreStrings)
    is BillingFailures.InternetError -> coreStrings.networkErrorMessage
    is BillingFailures.OtherError -> strings.otherErrorMessage
}