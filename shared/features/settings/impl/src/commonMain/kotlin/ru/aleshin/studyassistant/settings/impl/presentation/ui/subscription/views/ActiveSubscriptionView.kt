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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.subscription.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.platform.services.iap.IapPurchaseStatus
import ru.aleshin.studyassistant.core.common.platform.services.iap.Store
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.dayMonthYearFormat
import ru.aleshin.studyassistant.settings.impl.presentation.models.billing.SubscriptionUi
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes

/**
 * @author Stanislav Aleshin on 19.06.2025.
 */
@Composable
internal fun ActiveSubscriptionView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentStore: Store?,
    subscriptions: List<SubscriptionUi>,
    onOpenBillingScreen: () -> Unit,
    onControlSubscription: () -> Unit,
    onRestoreSubscription: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ActiveSubscriptionViewHeader(onOpenBillingScreen = onOpenBillingScreen)
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Crossfade(
                    modifier = Modifier.animateContentSize(),
                    targetState = isLoading,
                    animationSpec = spring(
                        stiffness = Spring.StiffnessMediumLow,
                        visibilityThreshold = Spring.DefaultDisplacementThreshold,
                    ),
                ) { loading ->
                    if (!loading) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (subscriptions.isNotEmpty()) {
                                subscriptions.forEach { subscription ->
                                    SubscriptionView(
                                        title = subscription.title,
                                        description = subscription.description,
                                        isActive = subscription.isActive,
                                        expiryTime = subscription.expiryTime,
                                        price = subscription.amountLabel,
                                    )
                                }
                            } else {
                                NoneSubscriptionView()
                            }
                        }
                    } else {
                        PlaceholderBox(
                            modifier = Modifier.fillMaxWidth().height(110.dp),
                            shape = MaterialTheme.shapes.large,
                        )
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onControlSubscription,
                        enabled = currentStore != null,
                    ) {
                        Text(
                            text = buildString {
                                append(SettingsThemeRes.strings.controlSubscriptionInStoreSuffix)
                                append(
                                    currentStore?.mapToString()
                                        ?: StudyAssistantRes.strings.storeTitle
                                )
                            }
                        )
                    }
                    TextButton(
                        modifier = Modifier.height(32.dp),
                        onClick = onRestoreSubscription,
                        enabled = currentStore != null,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = buildString {
                                append(SettingsThemeRes.strings.restoreSubscriptionInStoreSuffix)
                                append(
                                    currentStore?.mapToString()
                                        ?: StudyAssistantRes.strings.storeTitle
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveSubscriptionViewHeader(
    modifier: Modifier = Modifier,
    onOpenBillingScreen: () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.Stars,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = SettingsThemeRes.strings.activeSubscriptionsTitle,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.weight(1f))
        FilledTonalButton(
            onClick = onOpenBillingScreen,
            modifier = Modifier.height(32.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            Text(
                text = SettingsThemeRes.strings.showSubscriptionPlansTitle,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
internal fun NoneSubscriptionView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            text = SettingsThemeRes.strings.noneSubscriptionsTitle,
            textAlign = TextAlign.Center,
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

@Composable
internal fun SubscriptionView(
    modifier: Modifier = Modifier,
    title: String?,
    description: String?,
    isActive: Boolean,
    expiryTime: Long?,
    price: String?,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        color = Color.Transparent,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title ?: SettingsThemeRes.strings.undefinedSubscriptionTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    if (description != null) {
                        Text(
                            text = description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
                if (price != null) {
                    Text(
                        text = price,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val coreStrings = StudyAssistantRes.strings
                Text(
                    text = buildAnnotatedString {
                        append(SettingsThemeRes.strings.subscriptionStatusSuffix)
                        withStyle(
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary
                            ).toSpanStyle()
                        ) {
                            if (isActive) {
                                append(IapPurchaseStatus.CONFIRMED.mapToString(coreStrings))
                            } else {
                                append(IapPurchaseStatus.EXPIRED.mapToString(coreStrings))
                            }
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = buildAnnotatedString {
                        append(SettingsThemeRes.strings.subscriptionExpiryTimeSuffix)
                        withStyle(
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = MaterialTheme.colorScheme.primary
                            ).toSpanStyle()
                        ) {
                            val date = expiryTime?.mapEpochTimeToInstant()?.formatByTimeZone(
                                format = DateTimeComponents.Formats.dayMonthYearFormat()
                            )
                            append(date ?: "-")
                        }
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}