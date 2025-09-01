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

package ru.aleshin.studyassistant.schedule.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.schedule.api.ScheduleFeatureComponentFactory
import ru.aleshin.studyassistant.schedule.impl.navigation.DefaultScheduleComponentFactory
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.store.DetailsComposeStore
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.store.DetailsWorkProcessor
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.store.OverviewComposeStore
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.store.OverviewWorkProcessor
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store.ShareComposeStore
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.store.ShareWorkProcessor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<ScheduleFeatureComponentFactory> { DefaultScheduleComponentFactory(instance(), instance(), instance()) }

    bindSingleton<OverviewWorkProcessor> { OverviewWorkProcessor.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<OverviewComposeStore.Factory> { OverviewComposeStore.Factory(instance(), instance(), instance()) }

    bindSingleton<DetailsWorkProcessor> { DetailsWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<DetailsComposeStore.Factory> { DetailsComposeStore.Factory(instance(), instance(), instance()) }

    bindSingleton<ShareWorkProcessor> { ShareWorkProcessor.Base(instance(), instance(), instance()) }
    bindSingleton<ShareComposeStore.Factory> { ShareComposeStore.Factory(instance(), instance(), instance()) }
}