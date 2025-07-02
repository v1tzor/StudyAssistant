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

package ru.aleshin.studyassistant.core.remote.appwrite.utils

/**
 * @author Stanislav Aleshin on 26.06.2025.
 */
object Channels {

    /**
     * Subscribe to changes in the currently logged-in account.
     */
    fun account(): List<String> = listOf("account")

    /**
     * Subscribe to changes in all documents in all collections in all databases.
     */
    fun documents(): List<String> = listOf("documents")

    /**
     * Subscribe to changes in all documents in a specific collection.
     */
    fun documents(databaseId: String, collectionId: String): List<String> =
        listOf("databases.$databaseId.collections.$collectionId.documents")

    /**
     * Subscribe to changes in a specific document.
     */
    fun document(databaseId: String, collectionId: String, documentId: String): List<String> =
        listOf("databases.$databaseId.collections.$collectionId.documents.$documentId")

    /**
     * Subscribe to changes in all files in all buckets.
     */
    fun files(): List<String> = listOf("files")

    /**
     * Subscribe to changes in all files in a specific bucket.
     */
    fun files(bucketId: String): List<String> =
        listOf("buckets.$bucketId.files")

    /**
     * Subscribe to changes in a specific file in a bucket.
     */
    fun file(bucketId: String, fileId: String): List<String> =
        listOf("buckets.$bucketId.files.$fileId")

    /**
     * Subscribe to changes in all functions.
     */
    fun functions(): List<String> = listOf("functions")

    /**
     * Subscribe to changes in a specific function.
     */
    fun function(functionId: String): List<String> =
        listOf("functions.$functionId")

    /**
     * Subscribe to execution logs of a specific function.
     */
    fun executions(functionId: String): List<String> =
        listOf("functions.$functionId.executions")

    /**
     * Subscribe to changes in all teams.
     */
    fun teams(): List<String> = listOf("teams")

    /**
     * Subscribe to changes in a specific team.
     */
    fun team(teamId: String): List<String> =
        listOf("teams.$teamId")

    /**
     * Subscribe to changes in all memberships of a team.
     */
    fun memberships(teamId: String): List<String> =
        listOf("teams.$teamId.memberships")

    /**
     * Subscribe to changes in a specific membership of a team.
     */
    fun membership(teamId: String, membershipId: String): List<String> =
        listOf("teams.$teamId.memberships.$membershipId")

    /**
     * Subscribe to changes in all users.
     */
    fun users(): List<String> = listOf("users")

    /**
     * Subscribe to changes in a specific user.
     */
    fun user(userId: String): List<String> =
        listOf("users.$userId")

    /**
     * Subscribe to changes in all messaging-related resources.
     */
    fun messaging(): List<String> = listOf("messaging")

    /**
     * Subscribe to a specific messaging topic.
     */
    fun topic(topic: String): List<String> =
        listOf("topics.$topic")

    /**
     * Subscribe to changes in a specific messaging provider.
     */
    fun provider(providerId: String): List<String> =
        listOf("providers.$providerId")
}