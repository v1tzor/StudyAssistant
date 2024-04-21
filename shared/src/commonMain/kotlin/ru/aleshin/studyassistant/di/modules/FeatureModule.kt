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

package ru.aleshin.studyassistant.di.modules

import managers.CoroutineManager
import org.kodein.di.DI
import org.kodein.di.bindEagerSingleton
import org.kodein.di.bindProvider
import org.kodein.di.instance
import org.kodein.di.provider
import ru.aleshin.studyassistant.auth.api.navigation.AuthFeatureStarter
import ru.aleshin.studyassistant.auth.impl.di.AuthFeatureDependencies
import ru.aleshin.studyassistant.auth.impl.di.holder.AuthFeatureDIHolder
import ru.aleshin.studyassistant.navigation.api.navigation.NavigationFeatureStarter
import ru.aleshin.studyassistant.navigation.impl.di.NavigationFeatureDependencies
import ru.aleshin.studyassistant.navigation.impl.di.holder.NavigationFeatureDIHolder
import ru.aleshin.studyassistant.preview.api.navigation.PreviewFeatureStarter
import ru.aleshin.studyassistant.preview.impl.di.PreviewFeatureDependencies
import ru.aleshin.studyassistant.preview.impl.di.holder.PreviewFeatureDIHolder
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.impl.di.ScheduleFeatureDependencies
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureDIHolder

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
val featureModule = DI.Module("Feature") {
    bindEagerSingleton<NavigationFeatureDependencies> {
        object : NavigationFeatureDependencies {
            override val scheduleFeatureStarter = provider<ScheduleFeatureStarter>()
            override val coroutineManager = instance<CoroutineManager>()
        }
    }
    bindProvider<NavigationFeatureStarter> {
        with(NavigationFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<PreviewFeatureDependencies> {
        object : PreviewFeatureDependencies {
            override val navigationFeatureStarter = provider<NavigationFeatureStarter>()
            override val authFeatureStarter = provider<AuthFeatureStarter>()
            override val coroutineManager = instance<CoroutineManager>()
        }
    }
    bindProvider<PreviewFeatureStarter> {
        with(PreviewFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<AuthFeatureDependencies> {
        object : AuthFeatureDependencies {
            override val navigationFeatureStarter = provider<NavigationFeatureStarter>()
            override val previewFeatureStarter = provider<PreviewFeatureStarter>()
            override val coroutineManager = instance<CoroutineManager>()
        }
    }
    bindProvider<AuthFeatureStarter> {
        with(AuthFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }

    bindEagerSingleton<ScheduleFeatureDependencies> {
        object : ScheduleFeatureDependencies {
            override val coroutineManager = instance<CoroutineManager>()
        }
    }
    bindProvider<ScheduleFeatureStarter> {
        with(ScheduleFeatureDIHolder) {
            init(instance())
            fetchApi().fetchStarter()
        }
    }
}