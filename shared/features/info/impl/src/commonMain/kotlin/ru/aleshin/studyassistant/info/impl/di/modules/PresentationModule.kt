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

package ru.aleshin.studyassistant.info.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindProvider
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorFeatureStarter
import ru.aleshin.studyassistant.info.api.navigation.InfoFeatureStarter
import ru.aleshin.studyassistant.info.impl.navigation.InfoFeatureStarterImpl
import ru.aleshin.studyassistant.info.impl.navigation.InfoScreenProvider
import ru.aleshin.studyassistant.info.impl.presentation.ui.navigation.NavigationScreen
import ru.aleshin.studyassistant.info.impl.presentation.ui.navigation.NavigationScreenModel
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.screenmodel.OrganizationsEffectCommunicator
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.screenmodel.OrganizationsScreenModel
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.screenmodel.OrganizationsStateCommunicator
import ru.aleshin.studyassistant.info.impl.presentation.ui.organizations.screenmodel.OrganizationsWorkProcessor

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<InfoFeatureStarter> { InfoFeatureStarterImpl(instance(), instance(), instance()) }
    bindSingleton<InfoScreenProvider> { InfoScreenProvider.Base(instance<() -> EditorFeatureStarter>()) }

    bindSingleton<NavigationScreenModel> { NavigationScreenModel() }
    bindSingleton<NavigationScreen> { NavigationScreen() }

    bindProvider<OrganizationsStateCommunicator> { OrganizationsStateCommunicator.Base() }
    bindProvider<OrganizationsEffectCommunicator> { OrganizationsEffectCommunicator.Base() }
    bindProvider<OrganizationsWorkProcessor> { OrganizationsWorkProcessor.Base(instance(), instance()) }
    bindProvider<OrganizationsScreenModel> { OrganizationsScreenModel(instance(), instance(), instance(), instance(), instance()) }
}