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

package ru.aleshin.studyassistant.preview.impl.di.modules

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.preview.api.PreviewFeatureComponentFactory
import ru.aleshin.studyassistant.preview.impl.navigation.DefaultPreviewComponentFactory
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.store.IntroComposeStore
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store.SetupComposeStore
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.store.SetupWorkProcessor

/**
 * @author Stanislav Aleshin on 14.04.2024.
 */
internal val presentationModule = DI.Module("Presentation") {
    bindSingleton<PreviewFeatureComponentFactory> { DefaultPreviewComponentFactory(instance(), instance()) }

    bindSingleton<IntroComposeStore.Factory> { IntroComposeStore.Factory(instance()) }

    bindSingleton<SetupWorkProcessor> { SetupWorkProcessor.Base(instance(), instance(), instance(), instance()) }
    bindSingleton<SetupComposeStore.Factory> { SetupComposeStore.Factory(instance(), instance()) }
}