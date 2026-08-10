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

package ru.aleshin.studyassistant.analytics.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import org.kodein.di.multiton
import org.kodein.di.scoped
import ru.aleshin.studyassistant.analytics.api.AnalyticsContentProviderFactory
import ru.aleshin.studyassistant.analytics.impl.navigation.AnalyticsComponentDeps
import ru.aleshin.studyassistant.analytics.impl.navigation.DefaultAnalyticsContentProviderFactory
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.store.AnalyticsComposeStore
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.store.AnalyticsWorkProcessor
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.root.AnalyticsFeatureComponent
import ru.aleshin.studyassistant.core.common.di.scope.FeatureComponentScope

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal val presentationModule = DI.Module("AnalyticsPresentation") {
    bind<AnalyticsFeatureComponent>() with scoped(FeatureComponentScope).multiton {
        deps: AnalyticsComponentDeps ->
        AnalyticsFeatureComponent.Default(
            componentContext = context,
            startConfig = deps.startConfig,
            outputConsumer = deps.outputConsumer,
            storeFactory = instance(),
        )
    }
    bindSingleton<AnalyticsContentProviderFactory> { DefaultAnalyticsContentProviderFactory(di) }

    bindSingleton<AnalyticsWorkProcessor> { AnalyticsWorkProcessor.Base(instance()) }
    bindSingleton<AnalyticsComposeStore.Factory> { AnalyticsComposeStore.Factory(instance(), instance()) }
}
