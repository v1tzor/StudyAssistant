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

package ru.aleshin.studyassistant.core.remote.api.billing

import ru.aleshin.studyassistant.core.domain.entities.billing.SubscriptionIdentifier

/**
 * @author Stanislav Aleshin on 08.08.2025.
 */
interface SubscriptionStatusProviderFactory {

    fun <I : SubscriptionIdentifier> createProvider(identifier: I): SubscriptionStatusProvider<I>

    class Base(
        private val rustore: RuStoreSubscriptionStatusProvider,
        private val appGallery: AppGallerySubscriptionStatusProvider,
        private val googlePlay: GooglePlaySubscriptionStatusProvider,
        private val appStore: AppStoreSubscriptionStatusProvider,
    ) : SubscriptionStatusProviderFactory {

        @Suppress("UNCHECKED_CAST")
        override fun <I : SubscriptionIdentifier> createProvider(identifier: I): SubscriptionStatusProvider<I> {
            return when (identifier) {
                is SubscriptionIdentifier.RuStore -> rustore as SubscriptionStatusProvider<I>
                is SubscriptionIdentifier.AppGallery -> appGallery as SubscriptionStatusProvider<I>
                is SubscriptionIdentifier.AppStore -> appStore as SubscriptionStatusProvider<I>
                is SubscriptionIdentifier.GooglePlay -> googlePlay as SubscriptionStatusProvider<I>
            }
        }
    }
}