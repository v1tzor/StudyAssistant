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

import io.ktor.client.HttpClient
import ru.aleshin.studyassistant.core.domain.entities.billing.SubscriptionIdentifier
import ru.aleshin.studyassistant.core.remote.models.billing.SubscriptionStatusPojo

/**
 * @author Stanislav Aleshin on 09.08.2025.
 */
interface AppGallerySubscriptionStatusProvider : SubscriptionStatusProvider<SubscriptionIdentifier.AppGallery> {

    class Base(
        private val httpClient: HttpClient,
    ) : AppGallerySubscriptionStatusProvider {

        override suspend fun fetchStatus(identifier: SubscriptionIdentifier.AppGallery): SubscriptionStatusPojo {
            TODO("Not yet implemented")
        }
    }
}