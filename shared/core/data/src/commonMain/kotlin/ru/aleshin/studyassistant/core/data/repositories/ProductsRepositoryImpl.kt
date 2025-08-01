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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.domain.repositories.ProductsRepository
import ru.aleshin.studyassistant.core.remote.api.billing.ProductsRemoteApi

/**
 * @author Stanislav Aleshin on 18.06.2025.
 */
class ProductsRepositoryImpl(
    private val productsApi: ProductsRemoteApi,
) : ProductsRepository {

    override suspend fun fetchProducts(): Flow<List<String>> {
        return productsApi.fetchProducts().map { productsList ->
            productsList.map { product -> product.id }
        }
    }
}