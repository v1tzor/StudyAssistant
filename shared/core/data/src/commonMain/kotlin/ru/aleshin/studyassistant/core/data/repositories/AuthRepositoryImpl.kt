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

import dev.gitlive.firebase.auth.FirebaseUser
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteDataAuthException
import ru.aleshin.studyassistant.core.domain.entities.auth.AuthCredentials
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.remote.datasources.auth.AuthRemoteDataSourceOld

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
class AuthRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSourceOld,
) : AuthRepository {

    override suspend fun registerByEmail(credentials: AuthCredentials): FirebaseUser {
        val user = remoteDataSource.createUserWithEmail(
            email = credentials.email,
            password = credentials.password
        )
        return user ?: throw AppwriteDataAuthException()
    }

    override suspend fun signInWithEmail(credentials: AuthCredentials): FirebaseUser {
        val user = remoteDataSource.signInWithEmail(
            email = credentials.email,
            password = credentials.password
        )
        return user ?: throw AppwriteDataAuthException()
    }

    override suspend fun signInViaGoogle(idToken: String?): FirebaseUser {
        val user = remoteDataSource.signInViaGoogle(idToken)
        return user ?: throw AppwriteDataAuthException()
    }

    override suspend fun signOut() {
        remoteDataSource.signOut()
    }
}