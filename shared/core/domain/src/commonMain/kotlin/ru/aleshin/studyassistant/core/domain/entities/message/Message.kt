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
 * imitations under the License.
 */

package ru.aleshin.studyassistant.core.domain.entities.message

import ru.aleshin.studyassistant.core.common.messages.PushServiceType

/**
 * @author Stanislav Aleshin on 05.09.2024.
 */
sealed class Message {

    abstract val pushStrategy: PushStrategy

    data class Notification(
        override val pushStrategy: PushStrategy,
        val title: String,
        val body: String,
        val image: String = "",
        val data: Map<String, String> = mapOf(),
    ) : Message()

    data class Data(
        override val pushStrategy: PushStrategy,
        val data: Map<String, String>,
    ) : Message()
}

sealed class PushStrategy {
    data class Token(val values: List<PushValue>) : PushStrategy()
    data class Topic(val value: String) : PushStrategy()
}

data class PushValue(val value: String?, val pushServiceType: PushServiceType)