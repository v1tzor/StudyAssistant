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

import ru.aleshin.studyassistant.core.common.navigation.DeepLinkUrl

class DeepLinkReceiver {
    private var consumer: ((DeepLinkUrl) -> Unit)? = null
    private var pendingUrl: DeepLinkUrl? = null

    fun open(url: String) {
        val deepLink = DeepLinkUrl.fromString(url)
        consumer?.invoke(deepLink) ?: run { pendingUrl = deepLink }
    }

    internal fun attach(consumer: (DeepLinkUrl) -> Unit) {
        this.consumer = consumer
        pendingUrl?.let(consumer)
        pendingUrl = null
    }
}
