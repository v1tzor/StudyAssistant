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

package ru.aleshin.studyassistant.navigation.impl.di

import inject.BaseFeatureDependencies
import managers.CoroutineManager
import ru.aleshin.studyassistant.info.api.navigation.InfoFeatureStarter
import ru.aleshin.studyassistant.profile.api.navigation.ProfileFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.tasks.api.navigation.TasksFeatureStarter

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
interface NavigationFeatureDependencies : BaseFeatureDependencies {
    val scheduleFeatureStarter: () -> ScheduleFeatureStarter
    val tasksFeatureStarter: () -> TasksFeatureStarter
    val infoFeatureStarter: () -> InfoFeatureStarter
    val profileFeatureStarter: () -> ProfileFeatureStarter
    val coroutineManager: CoroutineManager
}