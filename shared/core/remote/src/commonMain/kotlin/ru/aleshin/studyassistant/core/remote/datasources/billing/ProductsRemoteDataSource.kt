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

package ru.aleshin.studyassistant.core.remote.datasources.billing

import dev.gitlive.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.extensions.snapshotListFlowGet
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite
import ru.aleshin.studyassistant.core.remote.models.billing.ProductPojo

/**
 * @author Stanislav Aleshin on 18.06.2025.
 */
interface ProductsRemoteDataSource {

    suspend fun fetchProducts(): Flow<List<ProductPojo>>

    class Base(
        private val database: FirebaseFirestore,
    ) : ProductsRemoteDataSource {

        override suspend fun fetchProducts(): Flow<List<ProductPojo>> {
            val reference = database.collection(StudyAssistantAppwrite.Products.ROOT)
            return reference.snapshotListFlowGet<ProductPojo>()
        }
    }
}