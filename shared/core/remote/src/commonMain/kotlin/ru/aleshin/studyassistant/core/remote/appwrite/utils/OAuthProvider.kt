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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class OAuthProvider(val value: String, val scopes: List<String> = emptyList()) {

    // ENABLED

    @SerialName("google")
    GOOGLE(
        value = "google",
        scopes = listOf(
            "https://www.googleapis.com/auth/userinfo.email",
            "https://www.googleapis.com/auth/userinfo.profile",
            "openid"
        )
    ),

    @SerialName("yandex")
    YANDEX(
        value = "yandex",
        scopes = listOf(
            "login:email",
            "login:info"
        )
    ),

    // NOT ENABLED

    @SerialName("amazon")
    AMAZON("amazon"),

    @SerialName("apple")
    APPLE("apple"),

    @SerialName("auth0")
    AUTH0("auth0"),

    @SerialName("authentik")
    AUTHENTIK("authentik"),

    @SerialName("autodesk")
    AUTODESK("autodesk"),

    @SerialName("bitbucket")
    BITBUCKET("bitbucket"),

    @SerialName("bitly")
    BITLY("bitly"),

    @SerialName("box")
    BOX("box"),

    @SerialName("dailymotion")
    DAILYMOTION("dailymotion"),

    @SerialName("discord")
    DISCORD("discord"),

    @SerialName("disqus")
    DISQUS("disqus"),

    @SerialName("dropbox")
    DROPBOX("dropbox"),

    @SerialName("etsy")
    ETSY("etsy"),

    @SerialName("facebook")
    FACEBOOK("facebook"),

    @SerialName("github")
    GITHUB("github"),

    @SerialName("gitlab")
    GITLAB("gitlab"),

    @SerialName("linkedin")
    LINKEDIN("linkedin"),

    @SerialName("microsoft")
    MICROSOFT("microsoft"),

    @SerialName("notion")
    NOTION("notion"),

    @SerialName("oidc")
    OIDC("oidc"),

    @SerialName("okta")
    OKTA("okta"),

    @SerialName("paypal")
    PAYPAL("paypal"),

    @SerialName("paypalSandbox")
    PAYPALSANDBOX("paypalSandbox"),

    @SerialName("podio")
    PODIO("podio"),

    @SerialName("salesforce")
    SALESFORCE("salesforce"),

    @SerialName("slack")
    SLACK("slack"),

    @SerialName("spotify")
    SPOTIFY("spotify"),

    @SerialName("stripe")
    STRIPE("stripe"),

    @SerialName("tradeshift")
    TRADESHIFT("tradeshift"),

    @SerialName("tradeshiftBox")
    TRADESHIFTBOX("tradeshiftBox"),

    @SerialName("twitch")
    TWITCH("twitch"),

    @SerialName("wordpress")
    WORDPRESS("wordpress"),

    @SerialName("yahoo")
    YAHOO("yahoo"),

    @SerialName("yammer")
    YAMMER("yammer"),

    @SerialName("zoho")
    ZOHO("zoho"),

    @SerialName("zoom")
    ZOOM("zoom"),

    @SerialName("mock")
    MOCK("mock"),
    ;

    override fun toString() = value
}