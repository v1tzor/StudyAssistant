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

package ru.aleshin.studyassistant.core.api.utils

object Permission {

    /**
     * Generate read permission string for the provided role.
     *
     * @param role The role for which to generate the permission.
     * @returns The read permission string.
     */
    fun read(role: String): String {
        return "read(\"${role}\")"
    }

    /**
     * Generate write permission string for the provided role.
     *
     * This is an alias of update, delete, and possibly create.
     * Don't use write in combination with update, delete, or create.
     *
     * @param role The role for which to generate the permission.
     * @returns The write permission string.
     */
    fun write(role: String): String {
        return "write(\"${role}\")"
    }

    /**
     * Generate create permission string for the provided role.
     *
     * @param role The role for which to generate the permission.
     * @returns The create permission string.
     */
    fun create(role: String): String {
        return "create(\"${role}\")"
    }

    /**
     * Generate update permission string for the provided role.
     *
     * @param role The role for which to generate the permission.
     * @returns The update permission string.
     */
    fun update(role: String): String {
        return "update(\"${role}\")"
    }

    /**
     * Generate delete permission string for the provided role.
     *
     * @param role The role for which to generate the permission.
     * @returns The delete permission string.
     */
    fun delete(role: String): String {
        return "delete(\"${role}\")"
    }

    fun onlyUserData(userId: String) = listOf(
        read(Role.user(userId)),
        update(Role.user(userId)),
        delete(Role.user(userId))
    )

    fun onlyUsersVisibleData(userId: String) = listOf(
        read(Role.users()),
        update(Role.user(userId)),
        delete(Role.user(userId))
    )

    fun avatarData(userId: String) = listOf(
        read(Role.any()),
        update(Role.user(userId)),
        delete(Role.user(userId))
    )
}

object Role {

    /**
     * Grants access to anyone.
     *
     * This includes authenticated and unauthenticated users.
     */
    fun any(): String = "any"

    /**
     * Grants access to a specific user by user ID.
     *
     * You can optionally pass verified or unverified for
     * [status] to target specific types of users.
     */
    fun user(id: String, status: String = ""): String = if (status.isEmpty()) {
        "user:$id"
    } else {
        "user:$id/$status"
    }

    /**
     * Grants access to any authenticated or anonymous user.
     *
     * You can optionally pass verified or unverified for
     * [status] to target specific types of users.
     */
    fun users(status: String = ""): String = if (status.isEmpty()) {
        "users"
    } else {
        "users/$status"
    }

    /**
     * Grants access to any guest user without a session.
     *
     * Authenticated users don't have access to this role.
     */
    fun guests(): String = "guests"

    /**
     * Grants access to a team by team ID.
     *
     * You can optionally pass a role for [role] to target
     * team members with the specified role.
     */
    fun team(id: String, role: String = ""): String = if (role.isEmpty()) {
        "team:$id"
    } else {
        "team:$id/$role"
    }

    /**
     * Grants access to a specific member of a team.
     *
     * When the member is removed from the team, they will
     * no longer have access.
     */
    fun member(id: String): String = "member:$id"

    /**
     * Grants access to a user with the specified label.
     */
    fun label(name: String): String = "label:$name"
}