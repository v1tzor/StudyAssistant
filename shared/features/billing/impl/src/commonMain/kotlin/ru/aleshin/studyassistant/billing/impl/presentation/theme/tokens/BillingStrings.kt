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

import androidx.compose.runtime.staticCompositionLocalOf
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantLanguage

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class BillingStrings(
    val subscriptionTopBarTitle: String,
    val subscribeButtonTitle: String,
    val subscribeTermsAndConditionsBody: String,
    val privacyPolicyLabel: String,
    val subscribePremiumTitle: String,
    val subscribePremiumBody: String,
    val premiumFunctionsTitle: String,
    val chooseSubscriptionPlanTitle: String,
    val subscriptionPaymentDescription: String,
    val otherErrorMessage: String,
    val premiumFeatureAiAssistant: String,
    val premiumFeatureDailyGoals: String,
    val premiumFeatureCloudSync: String,
    val premiumFeatureReceiveHomework: String,
    val premiumFeatureAdvancedNotifications: String,
    val premiumFeatureProfilePersonalization: String,
    val premiumFeatureDetailedAnalytics: String,
    val premiumFeatureFileAttachments: String,
    val premiumFeatureMultipleOrganizations: String,
    val successPaymentTitle: String,
    val successPaymentBody: String,
    val restartAppTitle: String,
    val premiumFeatureSupportDevelopment: String,
) {
    companion object Companion {
        val RUSSIAN = BillingStrings(
            subscriptionTopBarTitle = "Premium +",
            subscribeButtonTitle = "Оформить подписку",
            subscribeTermsAndConditionsBody = "Нажимая кнопку, вы соглашаетесь с ",
            subscribePremiumTitle = "Раскройте весь ваш потенциал!",
            subscribePremiumBody = "Получите доступ ко всем премиум функциям и улучшите свои результаты благодаря Premium +",
            privacyPolicyLabel = "Политикой конфиденциальности",
            premiumFunctionsTitle = "Premium функции:",
            chooseSubscriptionPlanTitle = "Выберите план:",
            subscriptionPaymentDescription = """
                Подписка автоматически продлевается, если автоматическое продление не будет отключено по крайней мере за 24 часа до окончания текущего периода.

                Плата за продление будет снята с учетной записи в течение 24 часов до окончания текущего периода.

                Вы можете управлять подписками и отменять их, перейдя в настройки своей учетной записи в RuStore/AppGallery после
                покупки.
            """.trimIndent(),
            otherErrorMessage = "Ошибка! Обратитесь к разработчику!",
            premiumFeatureAiAssistant = "Нейросетевой ассистент с голосовым управлением",
            premiumFeatureDailyGoals = "Дневные цели для планирования выполнения домашних заданий и TODO",
            premiumFeatureCloudSync = "Облачная синхронизация данных на всех ваших устройствах",
            premiumFeatureReceiveHomework = "Получение домашних заданий от других пользователей",
            premiumFeatureAdvancedNotifications = "Расширенные уведомления и напоминания о дедлайнах",
            premiumFeatureProfilePersonalization = "Особая персонализация профиля",
            premiumFeatureDetailedAnalytics = "Подробная аналитика для каждой организации",
            premiumFeatureFileAttachments = "Вложения файлов для домашних заданий и TODO",
            premiumFeatureMultipleOrganizations = "Создание более 2 учебных организаций",
            successPaymentTitle = "Подписка оформлена!",
            successPaymentBody = "Поздравляем! Теперь у вас есть доступ ко всем премиум-функциям. Наслаждайтесь новыми возможностями!",
            restartAppTitle = "Перезапустить",
            premiumFeatureSupportDevelopment = "Поддержка разработки и улучшений приложения",
        )
        val ENGLISH = BillingStrings(
            subscriptionTopBarTitle = "Premium +",
            subscribeButtonTitle = "Subscribe",
            subscribeTermsAndConditionsBody = "By clicking the button, you agree to the ",
            privacyPolicyLabel = "Privacy Policy",
            subscribePremiumTitle = "Unlock Your Full Study Potential!",
            premiumFunctionsTitle = "Premium features:",
            chooseSubscriptionPlanTitle = "Choose a plan:",
            subscribePremiumBody = "Get access to all the premium features and see your results improve with Premium +",
            subscriptionPaymentDescription = """
                The subscription will renew automatically unless you disable auto-renew at least 24 hours before the end of the current period. 

                If you do not cancel, a renewal fee will be charged to your account within 24 hours of the end of your current period. 

                You can manage your subscriptions and cancel them by going to your account settings in the RuStore or AppGallery after purchase.
            """.trimIndent(),
            otherErrorMessage = "Error! Contact the developer!",
            premiumFeatureAiAssistant = "AI study assistant with voice control",
            premiumFeatureDailyGoals = "Daily goals for homework and TODOs",
            premiumFeatureCloudSync = "Cloud data synchronization on all your devices",
            premiumFeatureReceiveHomework = "Receive homework from other users",
            premiumFeatureAdvancedNotifications = "Advanced notifications and deadline reminders",
            premiumFeatureProfilePersonalization = "Special profile personalization",
            premiumFeatureDetailedAnalytics = "Detailed analytics for each organization",
            premiumFeatureFileAttachments = "File attachments for homework and TODOs",
            premiumFeatureMultipleOrganizations = "Create more than 2 study organizations",
            successPaymentTitle = "Subscription Successful!",
            successPaymentBody = "Congratulations! You now have access to all premium features. Enjoy the new possibilities!",
            restartAppTitle = "Restart",
            premiumFeatureSupportDevelopment = "Support app development and improvements",
        )
    }
}

internal val LocalBillingStrings = staticCompositionLocalOf<BillingStrings> {
    error("Editor Strings is not provided")
}

internal fun fetchBillingStrings(language: StudyAssistantLanguage) = when (language) {
    StudyAssistantLanguage.EN -> BillingStrings.ENGLISH
    StudyAssistantLanguage.RU -> BillingStrings.RUSSIAN
}