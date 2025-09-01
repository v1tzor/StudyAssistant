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

package ru.aleshin.studyassistant.billing.impl.presentation.theme.tokens

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import studyassistant.shared.features.billing.impl.generated.resources.Res
import studyassistant.shared.features.billing.impl.generated.resources.ic_account_circle
import studyassistant.shared.features.billing.impl.generated.resources.ic_ai
import studyassistant.shared.features.billing.impl.generated.resources.ic_analytics_details
import studyassistant.shared.features.billing.impl.generated.resources.ic_cloud_sync
import studyassistant.shared.features.billing.impl.generated.resources.ic_daily_goals
import studyassistant.shared.features.billing.impl.generated.resources.ic_heart
import studyassistant.shared.features.billing.impl.generated.resources.ic_link_files
import studyassistant.shared.features.billing.impl.generated.resources.ic_notifications_and_reminders
import studyassistant.shared.features.billing.impl.generated.resources.ic_organization_type
import studyassistant.shared.features.billing.impl.generated.resources.ic_share_variant
import studyassistant.shared.features.billing.impl.generated.resources.il_premium
import studyassistant.shared.features.billing.impl.generated.resources.il_premium_dark

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Immutable
internal data class BillingIcons(
    val premiumIllustration: DrawableResource,
    val accountCircle: DrawableResource,
    val aiAssistant: DrawableResource,
    val analyticsDetails: DrawableResource,
    val cloudSync: DrawableResource,
    val dailyGoals: DrawableResource,
    val supportDevelopmentHeart: DrawableResource,
    val linkFiles: DrawableResource,
    val notificationsAndReminders: DrawableResource,
    val multipleOrganizations: DrawableResource,
    val shareHomework: DrawableResource
) {
    companion object Companion {
        val LIGHT = BillingIcons(
            premiumIllustration = Res.drawable.il_premium,
            accountCircle = Res.drawable.ic_account_circle,
            aiAssistant = Res.drawable.ic_ai,
            analyticsDetails = Res.drawable.ic_analytics_details,
            cloudSync = Res.drawable.ic_cloud_sync,
            dailyGoals = Res.drawable.ic_daily_goals,
            supportDevelopmentHeart = Res.drawable.ic_heart,
            linkFiles = Res.drawable.ic_link_files,
            notificationsAndReminders = Res.drawable.ic_notifications_and_reminders,
            multipleOrganizations = Res.drawable.ic_organization_type,
            shareHomework = Res.drawable.ic_share_variant
        )

        val DARK = BillingIcons(
            premiumIllustration = Res.drawable.il_premium_dark,
            accountCircle = Res.drawable.ic_account_circle,
            aiAssistant = Res.drawable.ic_ai,
            analyticsDetails = Res.drawable.ic_analytics_details,
            cloudSync = Res.drawable.ic_cloud_sync,
            dailyGoals = Res.drawable.ic_daily_goals,
            supportDevelopmentHeart = Res.drawable.ic_heart,
            linkFiles = Res.drawable.ic_link_files,
            notificationsAndReminders = Res.drawable.ic_notifications_and_reminders,
            multipleOrganizations = Res.drawable.ic_organization_type,
            shareHomework = Res.drawable.ic_share_variant
        )
    }
}

internal val LocalBillingIcons = staticCompositionLocalOf<BillingIcons> {
    error("Billing Icons is not provided")
}

internal fun fetchBillingIcons(isDark: Boolean) = when (isDark) {
    true -> BillingIcons.DARK
    false -> BillingIcons.LIGHT
}