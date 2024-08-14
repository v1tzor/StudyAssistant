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

package ru.aleshin.studyassistant.core.common.messages

/**
 * @author Stanislav Aleshin on 07.08.2024.
 */
interface PushServiceAuthTokenFactory {

    fun fetchTokenProvider(serviceType: PushServiceType): PushServiceAuthTokenProvider

    class Base(
        private val firebaseTokenProvider: PushServiceAuthTokenProvider.Firebase,
        private val huaweiTokenProvider: PushServiceAuthTokenProvider.Huawei,
        private val rustoreTokenProvider: PushServiceAuthTokenProvider.RuStore,
    ) : PushServiceAuthTokenFactory {

        override fun fetchTokenProvider(serviceType: PushServiceType) = when (serviceType) {
            PushServiceType.FCM -> firebaseTokenProvider
            PushServiceType.HMS -> huaweiTokenProvider
            PushServiceType.RUSTORE -> rustoreTokenProvider
            PushServiceType.APNS -> firebaseTokenProvider
            PushServiceType.NONE -> PushServiceAuthTokenProvider.None
        }
    }
}