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

package ru.aleshin.studyassistant.profile.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.multiton
import org.kodein.di.scoped
import ru.aleshin.studyassistant.core.common.di.scope.FeatureComponentScope
import ru.aleshin.studyassistant.profile.api.ProfileContentProviderFactory
import ru.aleshin.studyassistant.profile.impl.navigation.DefaultProfileContentProviderFactory
import ru.aleshin.studyassistant.profile.impl.navigation.ProfileComponentDeps
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.ProfileComposeStore
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.ProfileFeatureComponent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.ProfileWorkProcessor

/**
 * @author Stanislav Aleshin on 21.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bind<ProfileFeatureComponent>() with scoped(FeatureComponentScope).multiton { deps: ProfileComponentDeps ->
        ProfileFeatureComponent.Default(
            componentContext = context,
            startConfig = deps.startConfig,
            outputConsumer = deps.outputConsumer,
            profileStoreFactory = instance(),
        )
    }
    bindSingleton<ProfileContentProviderFactory> { DefaultProfileContentProviderFactory(di) }

    bindSingleton<ProfileWorkProcessor> { ProfileWorkProcessor.Base(instance()) }
    bindSingleton<ProfileComposeStore.Factory> { ProfileComposeStore.Factory(instance(), instance()) }
}
