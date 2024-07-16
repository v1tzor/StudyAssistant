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

package ru.aleshin.studyassistant.core.ui.mappers

import ru.aleshin.studyassistant.core.domain.entities.users.SocialNetworkType
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantIcons
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings

/**
 * @author Stanislav Aleshin on 16.07.2024.
 */
fun SocialNetworkType.mapToString(strings: StudyAssistantStrings) = when (this) {
    SocialNetworkType.TELEGRAM -> strings.telegramSocialNetwork
    SocialNetworkType.FACEBOOK -> strings.facebookSocialNetwork
    SocialNetworkType.X -> strings.XSocialNetwork
    SocialNetworkType.INSTAGRAM -> strings.instagramSocialNetwork
    SocialNetworkType.TIKTOK -> strings.tikTokSocialNetwork
    SocialNetworkType.WHATSAPP -> strings.whatsAppSocialNetwork
    SocialNetworkType.YOUTUBE -> strings.youtubeSocialNetwork
    SocialNetworkType.DISCORD -> strings.discordSocialNetwork
    SocialNetworkType.VK -> strings.vkSocialNetwork
    SocialNetworkType.OTHER -> strings.otherTitle
}

fun SocialNetworkType.mapToIcon(icons: StudyAssistantIcons) = when (this) {
    SocialNetworkType.TELEGRAM -> icons.telegramSocialNetwork
    SocialNetworkType.FACEBOOK -> icons.facebookSocialNetwork
    SocialNetworkType.X -> icons.XSocialNetwork
    SocialNetworkType.INSTAGRAM -> icons.instagramSocialNetwork
    SocialNetworkType.TIKTOK -> icons.tikTokSocialNetwork
    SocialNetworkType.WHATSAPP -> icons.whatsAppSocialNetwork
    SocialNetworkType.YOUTUBE -> icons.youtubeSocialNetwork
    SocialNetworkType.DISCORD -> icons.discordSocialNetwork
    SocialNetworkType.VK -> icons.vkSocialNetwork
    SocialNetworkType.OTHER -> icons.otherSocialNetwork
}