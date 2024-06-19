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

package remote.auth

import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseUser
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.firestore.FirebaseFirestore
import exceptions.FirebaseUserException

/**
 * @author Stanislav Aleshin on 22.04.2024.
 */
interface AuthRemoteDataSource {

    suspend fun createUserWithEmail(email: String, password: String): FirebaseUser?

    suspend fun fetchCurrentUser(): FirebaseUser?

    suspend fun signInWithEmail(email: String, password: String): FirebaseUser?

    suspend fun signInViaGoogle(idToken: String?): FirebaseUser?

    suspend fun sendPasswordResetEmail(email: String)

    suspend fun sendVerifyEmail(email: String)

    suspend fun updatePasswordOrEmail(email: String? = null, password: String? = null)

    suspend fun signOut()

    class Base(
        private val firebaseAuth: FirebaseAuth,
        private val firestore: FirebaseFirestore,
    ) : AuthRemoteDataSource {

        override suspend fun createUserWithEmail(email: String, password: String): FirebaseUser? {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password)
            return authResult.user
        }

        override suspend fun fetchCurrentUser(): FirebaseUser? {
            return firebaseAuth.currentUser
        }

        override suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password)
            return authResult.user
        }

        override suspend fun signInViaGoogle(idToken: String?): FirebaseUser? {
            val credential = GoogleAuthProvider.credential(idToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential)
            return authResult.user
        }

        override suspend fun sendPasswordResetEmail(email: String) {
            firebaseAuth.sendPasswordResetEmail(email)
        }

        override suspend fun sendVerifyEmail(email: String) {
            val currentUser = firebaseAuth.currentUser ?: throw FirebaseUserException()
            currentUser.sendEmailVerification()
        }

        override suspend fun updatePasswordOrEmail(email: String?, password: String?) {
            val currentUser = firebaseAuth.currentUser ?: throw FirebaseUserException()
            if (email != null) currentUser.updateEmail(email)
            if (password != null) currentUser.updatePassword(password)
        }

        override suspend fun signOut() {
            firebaseAuth.signOut()
        }
    }
}