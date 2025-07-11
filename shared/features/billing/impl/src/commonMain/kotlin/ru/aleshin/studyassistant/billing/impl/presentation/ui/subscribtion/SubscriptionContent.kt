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

package ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.billing.impl.presentation.models.products.SubscriptionProductUi
import ru.aleshin.studyassistant.billing.impl.presentation.theme.BillingThemeRes
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.contract.SubscriptionViewState
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.views.SubscriptionItemPlaceholder
import ru.aleshin.studyassistant.billing.impl.presentation.ui.subscribtion.views.SubscriptionItemView
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder.PRODUCT

/**
 * @author Stanislav Aleshin on 17.06.2025
 */
@Composable
internal fun SubscriptionContent(
    state: SubscriptionViewState,
    scrollState: ScrollState = rememberScrollState(),
    modifier: Modifier = Modifier,
    onChooseProduct: (SubscriptionProductUi) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        SubscriptionHeader()
        PremiumFunctionsSection()
        SubscriptionPlanSection(
            enabledSelect = !isLoadingPurchase,
            isLoadingProducts = isLoadingProducts,
            selectedProduct = selectedProduct,
            products = products,
            onChooseProduct = onChooseProduct,
        )
        Spacer(modifier = Modifier.padding(40.dp))
    }
}

@Composable
private fun SubscriptionHeader(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(BillingThemeRes.icons.premiumIllustration),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            alignment = Alignment.Center,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = BillingThemeRes.strings.subscribePremiumTitle,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = BillingThemeRes.strings.subscribePremiumBody,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun PremiumFunctionsSection(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = BillingThemeRes.strings.premiumFunctionsTitle,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.aiAssistant),
                    text = BillingThemeRes.strings.premiumFeatureAiAssistant
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.dailyGoals),
                    text = BillingThemeRes.strings.premiumFeatureDailyGoals
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.cloudSync),
                    text = BillingThemeRes.strings.premiumFeatureCloudSync
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.shareHomework),
                    text = BillingThemeRes.strings.premiumFeatureReceiveHomework
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.notificationsAndReminders),
                    text = BillingThemeRes.strings.premiumFeatureAdvancedNotifications
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.accountCircle),
                    text = BillingThemeRes.strings.premiumFeatureProfilePersonalization
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.analyticsDetails),
                    text = BillingThemeRes.strings.premiumFeatureDetailedAnalytics
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.linkFiles),
                    text = BillingThemeRes.strings.premiumFeatureFileAttachments
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.multipleOrganizations),
                    text = BillingThemeRes.strings.premiumFeatureMultipleOrganizations
                )
                PremiumFunctionView(
                    icon = painterResource(BillingThemeRes.icons.supportDevelopmentHeart),
                    text = BillingThemeRes.strings.premiumFeatureSupportDevelopment
                )
            }
        }
    }
}

@Composable
private fun PremiumFunctionView(
    modifier: Modifier = Modifier,
    icon: Painter,
    text: String,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = text,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
private fun SubscriptionPlanSection(
    modifier: Modifier = Modifier,
    enabledSelect: Boolean,
    isLoadingProducts: Boolean,
    selectedProduct: SubscriptionProductUi?,
    products: List<SubscriptionProductUi>,
    onChooseProduct: (SubscriptionProductUi) -> Unit,
) {
    Column(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = BillingThemeRes.strings.chooseSubscriptionPlanTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
        )
        Crossfade(
            targetState = isLoadingProducts,
            animationSpec = floatSpring(),
        ) { loading ->
            if (loading) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    repeat(PRODUCT) { SubscriptionItemPlaceholder() }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    products.forEach { product ->
                        if (product.title != null && product.amountLabel != null) {
                            SubscriptionItemView(
                                onSelect = { onChooseProduct(product) },
                                enabled = enabledSelect,
                                isSelected = selectedProduct?.productId == product.productId,
                                title = product.title,
                                description = product.description,
                                priceTitle = product.amountLabel,
                                priceSubtitle = null,
                            )
                        }
                    }
                }
            }
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = BillingThemeRes.strings.subscriptionPaymentDescription,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
        )
    }
}