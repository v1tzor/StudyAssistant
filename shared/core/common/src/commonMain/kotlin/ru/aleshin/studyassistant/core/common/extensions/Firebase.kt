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

package ru.aleshin.studyassistant.core.common.extensions

import dev.gitlive.firebase.firestore.CollectionReference
import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.FirebaseFirestore
import dev.gitlive.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.serializer
import ru.aleshin.studyassistant.core.common.functional.UID

/**
 * @author Stanislav Aleshin on 15.06.2024.
 */
suspend inline fun DocumentReference.snapshotGet() = snapshots.first()

inline fun <reified T : Any?> DocumentReference.snapshotFlowGet(): Flow<T?> = snapshots.map { snapshot ->
    snapshot.data(serializer<T?>())
}

suspend fun Query.snapshotGet() = snapshots.first().documents

suspend inline fun <reified T : Any> Query.snapshotListGet(): List<T> = snapshotGet().map { snapshot ->
    snapshot.data(serializer<T>())
}

inline fun <reified T : Any> Query.snapshotListFlowGet(): Flow<List<T>> = snapshots.map { snapshot ->
    snapshot.documents.map { it.data(serializer<T>()) }
}

suspend fun DocumentReference.exists() = snapshotGet().exists

inline fun <reified T : Any> CollectionReference.observeCollectionMapByField(
    ids: Set<UID>,
    fieldName: String = "uid",
    crossinline associateKey: (T) -> UID,
): Flow<Map<UID, T>> {
    if (ids.isEmpty()) return flowOf(emptyMap())

    return this
        .where { fieldName inArray ids.toList() }
        .snapshotListFlowGet<T>()
        .map { items -> items.associateBy(associateKey) }
}

suspend fun FirebaseFirestore.deleteAll(collectionReference: CollectionReference) {
    val deletableReferences = collectionReference.snapshotGet().map { snapshot -> snapshot.reference }
    batch().apply {
        deletableReferences.forEach { reference -> delete(reference) }
        return@apply commit()
    }
}