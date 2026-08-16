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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.store.ImportComponent
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_back_description
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_title

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportContent(
    component: ImportComponent,
    modifier: Modifier = Modifier,
) {
    val store = component.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    val onImageSelected: (PlatformFile?) -> Unit = remember(store, coroutineScope) {
        { file ->
            if (file != null) {
                coroutineScope.launch {
                    runCatching { file.readBytes() }.fold(
                        onSuccess = { bytes ->
                            store.dispatchEvent(ImportEvent.RecognizeImage(bytes))
                        },
                        onFailure = {
                            store.dispatchEvent(ImportEvent.ImageSelectionFailed)
                        },
                    )
                }
            }
        }
    }
    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = onImageSelected,
    )
    val cameraLauncher = rememberCameraPickerLauncher(
        onResult = onImageSelected,
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.schedule_import_title)) },
                navigationIcon = {
                    IconButton(onClick = { store.dispatchEvent(ImportEvent.ClickBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(Res.string.schedule_import_back_description),
                        )
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    ) { paddingValues ->
        ImportLayout(
            modifier = Modifier.padding(paddingValues),
            state = state,
            onSourceTextChanged = { store.dispatchEvent(ImportEvent.UpdateSourceText(it)) },
            onNumberOfWeeksChanged = { store.dispatchEvent(ImportEvent.UpdateNumberOfWeeks(it)) },
            onSelectPhoto = { galleryLauncher.launch() },
            onTakePhoto = { cameraLauncher.launch(cameraFacing = FileKitCameraFacing.Back) },
            onExtract = { store.dispatchEvent(ImportEvent.ExtractDraft) },
            onToggleEntry = { store.dispatchEvent(ImportEvent.ToggleEntry(it)) },
            onUpdateEntry = { store.dispatchEvent(ImportEvent.UpdateEntry(it)) },
            onApply = { store.dispatchEvent(ImportEvent.ApplyDraft) },
            onEditSource = { store.dispatchEvent(ImportEvent.EditSource) },
            onDone = { store.dispatchEvent(ImportEvent.ClickBack) },
        )
    }

    store.handleEffects { effect ->
        when (effect) {
            is ImportEffect.ShowError -> snackbarState.showSnackbar(effect.failure.mapToMessage())
        }
    }
}
