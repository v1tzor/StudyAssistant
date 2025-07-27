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

package ru.aleshin.studyassistant.auth.impl.presentation.mappers

import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.presentation.theme.tokens.AuthStrings

/**
 * @author Stanislav Aleshin on 16.04.2024.
 */
internal fun AuthFailures.mapToMessage(strings: AuthStrings): String = when (this) {
    is AuthFailures.AuthorizationError -> strings.authErrorMessage
    is AuthFailures.CredentialsError -> strings.credentialsErrorMessage
    is AuthFailures.NotFoundUserInfoError -> strings.userNotFoundErrorMessage
    is AuthFailures.TooManyRequestsError -> strings.tooManyRequestsErrorMessage
    is AuthFailures.UserAlreadyExistsError -> strings.userAlreadyExistsMessage
    is AuthFailures.EmailAlreadyUsedError -> strings.emailAlreadyUsedMessage
    is AuthFailures.PasswordRecentlyUsedError -> strings.passwordRecentlyUsedMessage
    is AuthFailures.PasswordPersonalDataError -> strings.passwordPersonalDataMessage
    is AuthFailures.PhoneNotFoundError -> strings.phoneNotFoundMessage
    is AuthFailures.MissingIdFromProviderError -> strings.missingIdFromProviderMessage
    is AuthFailures.OAuthBadRequestError -> strings.oauthBadRequestMessage
    is AuthFailures.JwtInvalidError -> strings.jwtInvalidMessage
    is AuthFailures.UserBlockedError -> strings.userBlockedMessage
    is AuthFailures.EmailNotWhitelistedError -> strings.emailNotWhitelistedMessage
    is AuthFailures.InvalidCodeError -> strings.invalidCodeMessage
    is AuthFailures.IpNotWhitelistedError -> strings.ipNotWhitelistedMessage
    is AuthFailures.AnonymousConsoleProhibitedError -> strings.anonymousConsoleProhibitedMessage
    is AuthFailures.SessionAlreadyExistsError -> strings.sessionAlreadyExistsMessage
    is AuthFailures.OAuthUnauthorizedError -> strings.oauthUnauthorizedMessage
    is AuthFailures.TeamInvalidSecretError -> strings.teamInvalidSecretMessage
    is AuthFailures.TeamInviteMismatchError -> strings.teamInviteMismatchMessage
    is AuthFailures.SessionNotFoundError -> strings.sessionNotFoundMessage
    is AuthFailures.IdentityNotFoundError -> strings.identityNotFoundMessage
    is AuthFailures.TeamNotFoundError -> strings.teamNotFoundMessage
    is AuthFailures.TeamInviteNotFoundError -> strings.teamInviteNotFoundMessage
    is AuthFailures.TeamMembershipMismatchError -> strings.teamMembershipMismatchMessage
    is AuthFailures.MembershipNotFoundError -> strings.membershipNotFoundMessage
    is AuthFailures.TeamInviteAlreadyExistsError -> strings.teamInviteAlreadyExistsMessage
    is AuthFailures.TeamAlreadyExistsError -> strings.teamAlreadyExistsMessage
    is AuthFailures.MembershipAlreadyConfirmedError -> strings.membershipAlreadyConfirmedMessage
    is AuthFailures.PasswordResetRequiredError -> strings.passwordResetRequiredMessage
    is AuthFailures.OAuthProviderError -> strings.oauthProviderErrorMessage
    is AuthFailures.UserCountExceededError -> strings.userCountExceededMessage
    is AuthFailures.AuthMethodUnsupportedError -> strings.authMethodUnsupportedMessage
    is AuthFailures.AccessDeniedError -> strings.accessDeniedMessage
    is AuthFailures.InternetError -> strings.networkErrorMessage
    is AuthFailures.OtherError -> strings.otherErrorMessage
}