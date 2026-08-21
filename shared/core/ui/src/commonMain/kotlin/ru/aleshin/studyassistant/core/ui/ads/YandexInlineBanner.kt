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

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.yandex.mobile.ads.kmp.banner.Banner
import com.yandex.mobile.ads.kmp.banner.BannerAdSize
import com.yandex.mobile.ads.kmp.banner.BannerEvents
import com.yandex.mobile.ads.kmp.banner.rememberBannerAdState
import com.yandex.mobile.ads.kmp.common.AdRequest
import com.yandex.mobile.ads.kmp.common.AdTheme
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
@Composable
fun YandexInlineBanner(
    modifier: Modifier = Modifier,
    placement: AdPlacement,
) {
    val configuration = LocalAdsConfiguration.current ?: return
    val adUnitId = when (placement) {
        AdPlacement.TASKS_OVERVIEW -> configuration.tasksOverviewBannerId
        AdPlacement.INFO_ORGANIZATIONS -> configuration.infoOrganizationsBannerId
        AdPlacement.SHARE_IMPORT -> configuration.shareImportBannerId
        AdPlacement.SHARE_PREVIEW -> configuration.sharePreviewBannerId
        AdPlacement.AI_IMPORTER -> configuration.aiImporterBannerId
        AdPlacement.HOMEWORK_RECEIVE -> configuration.homeworkReceiveBannerId
        AdPlacement.ANALYTICS -> configuration.analyticsBannerId
    }

    if (adUnitId.isBlank()) return

    val bannerMaxHeight = if (placement == AdPlacement.ANALYTICS || placement == AdPlacement.HOMEWORK_RECEIVE) {
        BANNER_ANALYTICS_HEIGHT
    } else {
        BANNER_DEFAULT_HEIGHT
    }

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val bannerWidth = if (maxWidth > 0.dp) maxWidth else BANNER_FALLBACK_WIDTH

        var loadState by remember(adUnitId) { mutableStateOf(AdLoadState.Loading) }

        val bannerState = rememberBannerAdState(
            adSize = BannerAdSize.Inline(
                width = bannerWidth,
                maxHeight = bannerMaxHeight,
            ),
            events = BannerEvents(
                onAdFailedToLoad = { loadState = AdLoadState.Failed },
                onAdLoaded = { loadState = AdLoadState.Loaded }
            ),
        )

        val isDarkTheme = StudyAssistantRes.colors.isDark

        LaunchedEffect(adUnitId, bannerWidth) {
            loadState = AdLoadState.Loading
            val theme = if (isDarkTheme) AdTheme.DARK else AdTheme.LIGHT
            bannerState.loadAd(AdRequest(adUnitId = adUnitId, preferredTheme = theme))
        }

        if (loadState != AdLoadState.Failed) {
            Banner(
                state = bannerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(bannerMaxHeight),
            )
        }
    }
}

private enum class AdLoadState {
    Loading,
    Loaded,
    Failed
}

private val BANNER_DEFAULT_HEIGHT = 50.dp
private val BANNER_ANALYTICS_HEIGHT = 100.dp
private val BANNER_FALLBACK_WIDTH = 320.dp