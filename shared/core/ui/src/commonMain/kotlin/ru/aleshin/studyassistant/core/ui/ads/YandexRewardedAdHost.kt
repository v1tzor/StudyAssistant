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

package ru.aleshin.studyassistant.core.ui.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import com.yandex.mobile.ads.kmp.common.AdError
import com.yandex.mobile.ads.kmp.common.AdRequest
import com.yandex.mobile.ads.kmp.common.ImpressionData
import com.yandex.mobile.ads.kmp.compose.rememberRewardedAdLoader
import com.yandex.mobile.ads.kmp.rewarded.Reward
import com.yandex.mobile.ads.kmp.rewarded.RewardedAdEventListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
@Composable
fun YandexRewardedAdHost(
    adUnitId: String,
    requestKey: String?,
    onRewarded: (String) -> Unit,
    onUnavailable: () -> Unit,
) {
    val currentOnRewarded by rememberUpdatedState(onRewarded)
    val currentOnUnavailable by rememberUpdatedState(onUnavailable)
    val coroutineScope = rememberCoroutineScope()
    var handledRequestKey by remember { mutableStateOf<String?>(null) }

    if (adUnitId.isBlank()) {
        LaunchedEffect(requestKey) {
            if (requestKey != null && requestKey != handledRequestKey) {
                handledRequestKey = requestKey
                currentOnUnavailable()
            }
        }
        return
    }
    val loader = rememberRewardedAdLoader()

    LaunchedEffect(requestKey, adUnitId) {
        if (requestKey == null || requestKey == handledRequestKey) return@LaunchedEffect
        handledRequestKey = requestKey
        runCatching {
            loader.loadAd(AdRequest(adUnitId = adUnitId))
        }.onSuccess { ad ->
            var isRewarded = false
            ad.setAdEventListener(
                object : RewardedAdEventListener {
                    override fun onAdShown() = Unit

                    override fun onAdFailedToShow(adError: AdError) {
                        currentOnUnavailable()
                    }

                    override fun onAdDismissed() {
                        coroutineScope.launch {
                            delay(REWARD_CALLBACK_GRACE_MS)
                            if (!isRewarded) currentOnUnavailable()
                        }
                    }

                    override fun onAdClicked() = Unit

                    override fun onAdImpression(impressionData: ImpressionData?) = Unit

                    override fun onRewarded(reward: Reward) {
                        if (!isRewarded) {
                            isRewarded = true
                            currentOnRewarded(requestKey)
                        }
                    }
                },
            )
            ad.show()
        }.onFailure {
            currentOnUnavailable()
        }
    }
}

private const val REWARD_CALLBACK_GRACE_MS = 400L
