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

package ru.aleshin.studyassistant.core.common.platform.services.iap

import ru.aleshin.studyassistant.core.common.functional.Constants.Date.MILLIS_IN_DAY

/**
 * @author Stanislav Aleshin on 16.06.2025.
 */
data class IapProductSubscription(
    val subscriptionPeriod: IapSubscriptionPeriod?,
    val freeTrialPeriod: IapSubscriptionPeriod?,
    val gracePeriod: IapSubscriptionPeriod?,
    val introductoryPrice: String?,
    val introductoryPriceAmount: String?,
    val introductoryPricePeriod: IapSubscriptionPeriod?,
)

data class IapSubscriptionPeriod(
    val years: Int,
    val months: Int,
    val days: Int,
) {
    fun inMillis(): Long {
        return years * 365 * MILLIS_IN_DAY + months * 31 * MILLIS_IN_DAY + days * MILLIS_IN_DAY
    }

    companion object {
        fun fromIso8601(value: String) = with(value) {
            if (!startsWith("P")) return@with IapSubscriptionPeriod(0, 0, 0)

            var years = 0
            var months = 0
            var days = 0
            var weeks = 0

            val regex = Regex("""(\d+)([YMWD])""")
            val matches = regex.findAll(this.drop(1))

            for (match in matches) {
                val value = match.groupValues[1].toInt()
                when (match.groupValues[2]) {
                    "Y" -> years += value
                    "M" -> months += value
                    "W" -> days += value * 7
                    "D" -> days += value
                }
            }

            return@with IapSubscriptionPeriod(
                years = years,
                months = months,
                days = days
            )
        }
    }
}