/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.widget.presentation.navigation

import android.content.Context
import android.content.Intent
import android.net.Uri
import ru.aleshin.studyassistant.core.common.navigation.WidgetDeepLinkDestination

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
object WidgetDeepLinkFactory {

    fun createIntent(
        context: Context,
        destination: WidgetDeepLinkDestination,
    ): Intent = Intent(Intent.ACTION_VIEW, Uri.parse(destination.toUrl())).apply {
        setPackage(context.packageName)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
}
