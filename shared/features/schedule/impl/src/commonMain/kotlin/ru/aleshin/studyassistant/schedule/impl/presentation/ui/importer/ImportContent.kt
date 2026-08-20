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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.backhandler.BackHandler
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitCameraFacing
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberCameraPickerLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.ui.ads.LocalAdsConfiguration
import ru.aleshin.studyassistant.core.ui.ads.YandexRewardedAdHost
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.schedule.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.contract.ImportEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.store.ImportComponent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportBottomActionBar
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportClassEditorSheet
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportSubjectEditorSheet
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportTeacherEditorSheet
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views.ImportTopBar
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_new_subject_name
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_new_teacher_name

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportContent(
    modifier: Modifier = Modifier,
    component: ImportComponent,
) {
    val store = component.store
    val state by store.stateAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val adsConfiguration = LocalAdsConfiguration.current
    var editingClassId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingSubjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var editingEmployeeId by rememberSaveable { mutableStateOf<String?>(null) }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchImportLayoutMode()
    val isExpanded = layoutMode == ImportLayoutMode.EXPANDED
    val newSubjectName = stringResource(Res.string.schedule_import_new_subject_name)
    val newTeacherName = stringResource(Res.string.schedule_import_new_teacher_name)

    val galleryLauncher = rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = { file -> handlePickedFile(file, coroutineScope, store::dispatchEvent) },
    )
    val cameraLauncher = rememberCameraPickerLauncher(
        onResult = { file -> handlePickedFile(file, coroutineScope, store::dispatchEvent) },
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            ImportTopBar(
                isExpanded = isExpanded,
                canNavigateBack = !state.isAnalysisInProgress,
                onBackClick = { store.dispatchEvent(ImportEvent.ClickBack) },
            )
        },
        bottomBar = {
            if (state.session != null && !state.isApplied) {
                ImportBottomActionBar(
                    enabled = !state.isRewardInProgress && !state.isAnalysisInProgress,
                    isLoadingAccept = state.isRewardInProgress || state.isAnalysisInProgress,
                    contentMaxWidth = if (isExpanded) {
                        AdaptiveLayoutDefaults.MediumContentMaxWidth
                    } else {
                        null
                    },
                    horizontalPadding = if (isExpanded) {
                        AdaptiveLayoutDefaults.ExpandedHorizontalPadding
                    } else {
                        AdaptiveLayoutDefaults.CompactHorizontalPadding
                    },
                    onSaveClick = { store.dispatchEvent(ImportEvent.ApplySession) },
                    onEditSourceClick = { store.dispatchEvent(ImportEvent.EditSource) },
                )
            }
        },
        contentWindowInsets = WindowInsets(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData -> ErrorSnackbar(snackbarData) },
            )
        },
    ) { contentPadding ->
        ImportLayout(
            modifier = Modifier.padding(contentPadding),
            state = state,
            layoutMode = layoutMode,
            onSelectPhoto = { galleryLauncher.launch() },
            onTakePhoto = { cameraLauncher.launch(cameraFacing = FileKitCameraFacing.Back) },
            onNoteChanged = { note -> store.dispatchEvent(ImportEvent.UpdateNote(note)) },
            onOrganizationSelect = { organization ->
                store.dispatchEvent(ImportEvent.SelectOrganization(organization))
            },
            onAddOrganization = { store.dispatchEvent(ImportEvent.ClickAddOrganization) },
            onExtract = { store.dispatchEvent(ImportEvent.ExtractDraft) },
            onCancelExtract = { store.dispatchEvent(ImportEvent.CancelExtract) },
            onClassClick = { classId -> editingClassId = classId },
            onSubjectClick = { subjectId -> editingSubjectId = subjectId },
            onTeacherClick = { employeeId -> editingEmployeeId = employeeId },
            onAddSubject = {
                val uid = randomUUID()
                store.dispatchEvent(ImportEvent.AddSubject(name = newSubjectName, uid = uid))
                editingSubjectId = uid
            },
            onAddTeacher = {
                val uid = randomUUID()
                store.dispatchEvent(ImportEvent.AddEmployee(firstName = newTeacherName, uid = uid))
                editingEmployeeId = uid
            },
            onAddClass = { dayOfWeek, repeatWeek ->
                store.dispatchEvent(ImportEvent.AddClass(dayOfWeek, repeatWeek))
            },
            onUpdateStartOfDay = { repeatWeek, dayOfWeek, startTime ->
                store.dispatchEvent(ImportEvent.UpdateStartOfDay(repeatWeek, dayOfWeek, startTime))
            },
            onUpdateClassesDuration = { repeatWeek, dayOfWeek, duration, specificDurations ->
                store.dispatchEvent(
                    ImportEvent.UpdateClassesDuration(repeatWeek, dayOfWeek, duration, specificDurations),
                )
            },
            onUpdateBreaksDuration = { repeatWeek, dayOfWeek, duration, specificDurations ->
                store.dispatchEvent(
                    ImportEvent.UpdateBreaksDuration(repeatWeek, dayOfWeek, duration, specificDurations),
                )
            },
            onSwapDays = { repeatWeek, firstDay, secondDay ->
                store.dispatchEvent(ImportEvent.SwapDays(repeatWeek, firstDay, secondDay))
            },
            onDone = { store.dispatchEvent(ImportEvent.ClickBack) },
        )
    }

    val editingClass = state.session?.classes?.firstOrNull { classModel -> classModel.uid == editingClassId }
    if (editingClass != null) {
        ImportClassEditorSheet(
            classModel = editingClass,
            subjects = state.session?.subjects.orEmpty(),
            employees = state.session?.employees.orEmpty(),
            originalSubjectIds = state.session?.originalSubjectIds.orEmpty(),
            originalEmployeeIds = state.session?.originalEmployeeIds.orEmpty(),
            onDismiss = { editingClassId = null },
            onConfirm = { classModel ->
                store.dispatchEvent(ImportEvent.UpdateClass(classModel))
                editingClassId = null
            },
            onDelete = {
                store.dispatchEvent(ImportEvent.DeleteClass(editingClass.uid))
                editingClassId = null
            },
            onAddSubject = { name -> store.dispatchEvent(ImportEvent.AddSubject(name)) },
            onAddEmployee = { name -> store.dispatchEvent(ImportEvent.AddEmployee(name)) },
        )
    }
    val editingSubject = state.session?.subjects?.firstOrNull { subject ->
        subject.uid == editingSubjectId
    }
    if (editingSubject != null) {
        ImportSubjectEditorSheet(
            subject = editingSubject,
            employees = state.session?.employees.orEmpty(),
            originalEmployeeIds = state.session?.originalEmployeeIds.orEmpty(),
            onDismiss = { editingSubjectId = null },
            onConfirm = { subject ->
                store.dispatchEvent(ImportEvent.UpdateSubject(subject))
                editingSubjectId = null
            },
            onDelete = {
                store.dispatchEvent(ImportEvent.DeleteSubject(editingSubject.uid))
                editingSubjectId = null
            },
            onAddEmployee = { name -> store.dispatchEvent(ImportEvent.AddEmployee(name)) },
        )
    }
    val editingEmployee = state.session?.employees?.firstOrNull { employee ->
        employee.uid == editingEmployeeId
    }
    if (editingEmployee != null) {
        ImportTeacherEditorSheet(
            employee = editingEmployee,
            onDismiss = { editingEmployeeId = null },
            onConfirm = { employee ->
                store.dispatchEvent(ImportEvent.UpdateEmployee(employee))
                editingEmployeeId = null
            },
            onDelete = {
                store.dispatchEvent(ImportEvent.DeleteEmployee(editingEmployee.uid))
                editingEmployeeId = null
            },
        )
    }

    ImportBackLock(
        backHandler = component.backHandler,
        isLocked = state.isAnalysisInProgress,
    )

    YandexRewardedAdHost(
        adUnitId = adsConfiguration?.aiScheduleAnalysisRewardedId.orEmpty(),
        requestKey = state.rewardChallengeId,
        onRewarded = { challengeId -> store.dispatchEvent(ImportEvent.RewardedAdGranted(challengeId)) },
        onUnavailable = { store.dispatchEvent(ImportEvent.RewardedAdUnavailable) },
    )

    LaunchedEffect(state.organizations) {
        if (state.organizations.size == 1 && state.selectedOrganization == null) {
            val organization = state.organizations.getOrNull(0) ?: return@LaunchedEffect
            store.dispatchEvent(ImportEvent.SelectOrganization(organization = organization))
        }
    }

    store.handleEffects { effect ->
        when (effect) {
            is ImportEffect.ShowError -> snackbarHostState.showSnackbar(
                message = effect.failure.mapToMessage(),
                withDismissAction = true,
            )
        }
    }
}

@Composable
private fun ImportBackLock(
    backHandler: BackHandler,
    isLocked: Boolean,
) {
    val callback = remember {
        BackCallback(isEnabled = false) { }
    }
    callback.isEnabled = isLocked
    DisposableEffect(backHandler) {
        backHandler.register(callback)
        onDispose { backHandler.unregister(callback) }
    }
}

private fun handlePickedFile(
    file: PlatformFile?,
    coroutineScope: CoroutineScope,
    dispatch: (ImportEvent) -> Unit,
) {
    if (file == null) return
    coroutineScope.launch {
        runCatching { file.readBytes() }.fold(
            onSuccess = { bytes -> dispatch(ImportEvent.SelectedPhoto(bytes)) },
            onFailure = { dispatch(ImportEvent.ImageSelectionFailed) },
        )
    }
}
