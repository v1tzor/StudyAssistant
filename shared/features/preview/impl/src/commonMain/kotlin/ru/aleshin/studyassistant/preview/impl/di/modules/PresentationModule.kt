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

package ru.aleshin.studyassistant.preview.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.multiton
import org.kodein.di.scoped
import ru.aleshin.studyassistant.core.common.di.scope.FeatureComponentScope
import ru.aleshin.studyassistant.preview.api.PreviewContentProviderFactory
import ru.aleshin.studyassistant.preview.impl.navigation.DefaultPreviewContentProviderFactory
import ru.aleshin.studyassistant.preview.impl.navigation.PreviewComponentDeps
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.store.IntroComposeStore
import ru.aleshin.studyassistant.preview.impl.presentation.ui.root.PreviewFeatureComponent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store.SetupComposeStore
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store.SetupWorkProcessor

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bind<PreviewFeatureComponent>() with scoped(FeatureComponentScope).multiton { deps: PreviewComponentDeps ->
        PreviewFeatureComponent.Default(
            componentContext = context,
            startConfig = deps.startConfig,
            outputConsumer = deps.outputConsumer,
            introStoreFactory = instance(),
            setupStoreFactory = instance(),
        )
    }
    bindSingleton<PreviewContentProviderFactory> { DefaultPreviewContentProviderFactory(di) }

    bindSingleton<IntroComposeStore.Factory> { IntroComposeStore.Factory(instance()) }

    bindSingleton<SetupWorkProcessor> {
        SetupWorkProcessor.Base(instance(), instance(), instance(), instance())
    }
    bindSingleton<SetupComposeStore.Factory> { SetupComposeStore.Factory(instance(), instance()) }
}
