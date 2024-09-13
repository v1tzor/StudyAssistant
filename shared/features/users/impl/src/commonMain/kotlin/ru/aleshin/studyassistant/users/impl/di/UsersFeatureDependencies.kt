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

package ru.aleshin.studyassistant.users.impl.di

import ru.aleshin.studyassistant.core.common.inject.BaseFeatureDependencies
import ru.aleshin.studyassistant.core.common.inject.CrashlyticsService
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
public interface UsersFeatureDependencies : BaseFeatureDependencies {
    public val editorFeatureStarter: () -> EditorFeatureStarter
    public val subjectsRepository: SubjectsRepository
    public val employeeRepository: EmployeeRepository
    public val friendRequestsRepository: FriendRequestsRepository
    public val usersRepository: UsersRepository
    public val messageRepository: MessageRepository
    public val dateManager: DateManager
    public val coroutineManager: CoroutineManager
    public val crashlyticsService: CrashlyticsService
}