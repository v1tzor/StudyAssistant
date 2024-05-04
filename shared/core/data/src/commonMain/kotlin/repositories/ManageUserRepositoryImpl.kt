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

package repositories

import database.auth.AuthRemoteDataSource

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
class ManageUserRepositoryImpl(
    private val remoteDataSource: AuthRemoteDataSource
) : ManageUserRepository {

    override suspend fun sendPasswordResetEmail(email: String) {
        remoteDataSource.sendPasswordResetEmail(email)
    }

    override suspend fun sendVerifyEmail(email: String) {
        remoteDataSource.sendVerifyEmail(email)
    }

    override suspend fun updatePassword(password: String) {
        remoteDataSource.updatePasswordOrEmail(password = password)
    }

    override suspend fun updateEmail(email: String) {
        remoteDataSource.updatePasswordOrEmail(email = email)
    }
}