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
package ru.aleshin.studyassistant.presentation.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import functional.Constants
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import theme.StudyAssistantRes
import theme.material.onSplash
import theme.material.splash

/**
 * @author Stanislav Aleshin on 09.02.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
fun SplashContent(
    modifier: Modifier = Modifier,
) {
    var isVisibleText by remember { mutableStateOf(false) }
    var isVisibleLogo by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize().background(splash),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnimatedVisibility(visible = isVisibleLogo, enter = fadeIn()) {
                Image(
                    modifier = Modifier.size(100.dp),
                    painter = painterResource(StudyAssistantRes.icons.logo),
                    contentDescription = StudyAssistantRes.strings.appName,
                )
            }
            AnimatedVisibility(visible = isVisibleText, enter = expandHorizontally()) {
                Text(
                    text = Constants.App.SPLASH_NAME,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Black,
                        lineHeight = 40.sp,
                    ),
                    color = onSplash,
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        delay(Constants.Delay.SPLASH_LOGO)
        isVisibleLogo = true
        delay(Constants.Delay.SPLASH_TEXT)
        isVisibleText = true
    }
}