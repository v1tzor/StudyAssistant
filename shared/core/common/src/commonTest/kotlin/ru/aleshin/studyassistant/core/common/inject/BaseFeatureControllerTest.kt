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

package ru.aleshin.studyassistant.core.common.inject

import org.kodein.di.DI
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import kotlin.test.Test
import kotlin.test.assertSame

class BaseFeatureControllerTest {

    @Test
    fun returnsApiFromFeatureGraph() {
        val api = TestFeatureApi.Base()
        val dependencies = TestFeatureDependencies(api)
        val controller = TestFeatureController(dependencies)

        assertSame(api, controller.fetchApi())
    }

    private interface TestFeatureApi : BaseFeatureApi {

        class Base : TestFeatureApi
    }

    private data class TestFeatureDependencies(
        val api: TestFeatureApi,
    ) : BaseFeatureDependencies

    private class TestFeatureController(
        dependencies: TestFeatureDependencies,
    ) : BaseFeatureController<TestFeatureApi, TestFeatureDependencies>(dependencies) {

        override fun fetchApi(): TestFeatureApi = directDI.instance<TestFeatureApi>()

        override fun DI.MainBuilder.buildDIGraph(dependencies: TestFeatureDependencies) {
            bindSingleton<TestFeatureApi> { dependencies.api }
        }
    }
}
