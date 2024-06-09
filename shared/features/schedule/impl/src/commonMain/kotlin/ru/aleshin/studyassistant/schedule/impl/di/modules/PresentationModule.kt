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
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleFeatureStarter
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleFeatureStarterImpl
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleScreenProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.screenmodel.DetailsEffectCommunicator
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.screenmodel.DetailsScreenModel
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.screenmodel.DetailsStateCommunicator
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.screenmodel.DetailsWorkProcessor
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.navigation.NavigationScreenModel

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<NavigationScreenModel> { NavigationScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<ScheduleFeatureStarter> { ScheduleFeatureStarterImpl(instance(), instance(), instance()) }
    bindProvider<ScheduleScreenProvider> { ScheduleScreenProvider.Base(instance<() -> EditorFeatureStarter>()) }

    bindProvider<DetailsStateCommunicator> { DetailsStateCommunicator.Base() }
    bindProvider<DetailsEffectCommunicator> { DetailsEffectCommunicator.Base() }
    bindProvider<DetailsWorkProcessor> { DetailsWorkProcessor.Base(instance()) }
    bindProvider<DetailsScreenModel> { DetailsScreenModel(instance(), instance(), instance(), instance(), instance()) }
}