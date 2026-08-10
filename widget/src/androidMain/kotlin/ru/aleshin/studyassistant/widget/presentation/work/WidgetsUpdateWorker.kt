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

package ru.aleshin.studyassistant.widget.presentation.work

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import org.kodein.di.DirectDIAware
import org.kodein.di.bindSingleton
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.functional.rightOrNull
import ru.aleshin.studyassistant.core.domain.entities.settings.LanguageType
import ru.aleshin.studyassistant.core.domain.entities.settings.ThemeType
import ru.aleshin.studyassistant.widget.di.WidgetWorkerDependencies
import ru.aleshin.studyassistant.widget.domain.entities.WidgetDisplaySettings
import ru.aleshin.studyassistant.widget.domain.interactors.WidgetInteractor
import ru.aleshin.studyassistant.widget.presentation.mappers.WidgetStateUiMapper
import ru.aleshin.studyassistant.widget.presentation.models.GoalsWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.models.HomeworksWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.models.ScheduleWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.models.TodoWidgetStateUi
import ru.aleshin.studyassistant.widget.presentation.state.WidgetContentStatusUi
import ru.aleshin.studyassistant.widget.presentation.state.WidgetStateCodec
import ru.aleshin.studyassistant.widget.presentation.state.WidgetStateKeys
import ru.aleshin.studyassistant.widget.presentation.ui.goals.GoalsWidget
import ru.aleshin.studyassistant.widget.presentation.ui.homeworks.HomeworksWidget
import ru.aleshin.studyassistant.widget.presentation.ui.schedule.ScheduleWidget
import ru.aleshin.studyassistant.widget.presentation.ui.todos.TodoWidget

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class WidgetsUpdateWorker(
    context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters), DirectDIAware {

    override val directDI = WidgetWorkerDependencies.create {
        bindSingleton<WidgetStateUiMapper> { WidgetStateUiMapper() }
    }
    private val widgetInteractor = instance<WidgetInteractor>()
    private val stateMapper = instance<WidgetStateUiMapper>()

    override suspend fun doWork(): Result {
        return try {
            val targets = fetchInstalledTargets()
            if (targets.isEmpty()) {
                WidgetsUpdateScheduler.cancelAll(applicationContext)
                return Result.success()
            }

            val settings = widgetInteractor.fetchDisplaySettings().rightOrNull()
                ?: WidgetDisplaySettings(ThemeType.DEFAULT, LanguageType.DEFAULT)
            val boundaries = mutableListOf<Long>()
            var successfulUpdates = 0

            targets.forEach { target ->
                successfulUpdates += when (target.widget) {
                    is ScheduleWidget -> updateSchedule(target, settings, boundaries)
                    is HomeworksWidget -> updateHomeworks(target, settings, boundaries)
                    is TodoWidget -> updateTodos(target, settings, boundaries)
                    is GoalsWidget -> updateGoals(target, settings, boundaries)
                    else -> 0
                }
            }
            boundaries.minOrNull()?.let { boundary ->
                WidgetsUpdateScheduler.scheduleBoundary(applicationContext, boundary)
            }

            if (successfulUpdates == 0 && runAttemptCount < MAX_RETRY_COUNT) {
                Result.retry()
            } else {
                Result.success()
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            if (runAttemptCount < MAX_RETRY_COUNT) Result.retry() else Result.failure()
        }
    }

    private suspend fun updateSchedule(
        target: WidgetTarget,
        settings: WidgetDisplaySettings,
        boundaries: MutableList<Long>,
    ): Int {
        val schedule = widgetInteractor.fetchSchedule().rightOrNull()
        schedule?.nextUpdateAt?.toEpochMilliseconds()?.let(boundaries::add)
        target.ids.forEach { id ->
            val state = schedule?.let(stateMapper::mapSchedule) ?: fetchScheduleFallback(id)
            updateTarget(target.widget, id, WidgetStateCodec.encode(state), settings)
        }
        return if (schedule != null) 1 else 0
    }

    private suspend fun updateHomeworks(
        target: WidgetTarget,
        settings: WidgetDisplaySettings,
        boundaries: MutableList<Long>,
    ): Int {
        val homeworks = widgetInteractor.fetchHomeworks().rightOrNull()
        homeworks?.nextUpdateAt?.toEpochMilliseconds()?.let(boundaries::add)
        target.ids.forEach { id ->
            val state = homeworks?.let(stateMapper::mapHomeworks) ?: fetchHomeworksFallback(id)
            updateTarget(target.widget, id, WidgetStateCodec.encode(state), settings)
        }
        return if (homeworks != null) 1 else 0
    }

    private suspend fun updateTodos(
        target: WidgetTarget,
        settings: WidgetDisplaySettings,
        boundaries: MutableList<Long>,
    ): Int {
        val todos = widgetInteractor.fetchTodos().rightOrNull()
        todos?.nextUpdateAt?.toEpochMilliseconds()?.let(boundaries::add)
        target.ids.forEach { id ->
            val state = todos?.let(stateMapper::mapTodos) ?: fetchTodosFallback(id)
            updateTarget(target.widget, id, WidgetStateCodec.encode(state), settings)
        }
        return if (todos != null) 1 else 0
    }

    private suspend fun updateGoals(
        target: WidgetTarget,
        settings: WidgetDisplaySettings,
        boundaries: MutableList<Long>,
    ): Int {
        val goals = widgetInteractor.fetchGoals().rightOrNull()
        goals?.nextUpdateAt?.toEpochMilliseconds()?.let(boundaries::add)
        target.ids.forEach { id ->
            val state = goals?.let(stateMapper::mapGoals) ?: fetchGoalsFallback(id)
            updateTarget(target.widget, id, WidgetStateCodec.encode(state), settings)
        }
        return if (goals != null) 1 else 0
    }

    private suspend fun fetchInstalledTargets(): List<WidgetTarget> {
        val manager = GlanceAppWidgetManager(applicationContext)
        return buildList {
            addTarget(manager, ScheduleWidget())
            addTarget(manager, HomeworksWidget())
            addTarget(manager, TodoWidget())
            addTarget(manager, GoalsWidget())
        }
    }

    private suspend fun MutableList<WidgetTarget>.addTarget(
        manager: GlanceAppWidgetManager,
        widget: GlanceAppWidget,
    ) {
        val ids = manager.getGlanceIds(widget.javaClass)
        if (ids.isNotEmpty()) add(WidgetTarget(widget, ids))
    }

    private suspend fun fetchScheduleFallback(id: GlanceId): ScheduleWidgetStateUi {
        val previous = WidgetStateCodec.decodeCurrentOrDefault(
            value = fetchPayload(id),
            version = ScheduleWidgetStateUi::version,
            defaultValue = ::ScheduleWidgetStateUi,
        )
        return if (previous.status == WidgetContentStatusUi.CONTENT) {
            previous.copy(isStale = true)
        } else {
            ScheduleWidgetStateUi(status = WidgetContentStatusUi.ERROR)
        }
    }

    private suspend fun fetchHomeworksFallback(id: GlanceId): HomeworksWidgetStateUi {
        val previous = WidgetStateCodec.decodeCurrentOrDefault(
            value = fetchPayload(id),
            version = HomeworksWidgetStateUi::version,
            defaultValue = ::HomeworksWidgetStateUi,
        )
        return if (previous.status == WidgetContentStatusUi.CONTENT) {
            previous.copy(isStale = true)
        } else {
            HomeworksWidgetStateUi(status = WidgetContentStatusUi.ERROR)
        }
    }

    private suspend fun fetchTodosFallback(id: GlanceId): TodoWidgetStateUi {
        val previous = WidgetStateCodec.decodeCurrentOrDefault(
            value = fetchPayload(id),
            version = TodoWidgetStateUi::version,
            defaultValue = ::TodoWidgetStateUi,
        )
        return if (previous.status == WidgetContentStatusUi.CONTENT) {
            previous.copy(isStale = true)
        } else {
            TodoWidgetStateUi(status = WidgetContentStatusUi.ERROR)
        }
    }

    private suspend fun fetchGoalsFallback(id: GlanceId): GoalsWidgetStateUi {
        val previous = WidgetStateCodec.decodeCurrentOrDefault(
            value = fetchPayload(id),
            version = GoalsWidgetStateUi::version,
            defaultValue = ::GoalsWidgetStateUi,
        )
        return if (previous.status == WidgetContentStatusUi.CONTENT) {
            previous.copy(isStale = true)
        } else {
            GoalsWidgetStateUi(status = WidgetContentStatusUi.ERROR)
        }
    }

    private suspend fun fetchPayload(id: GlanceId): String? {
        val preferences = getAppWidgetState<Preferences>(
            context = applicationContext,
            definition = PreferencesGlanceStateDefinition,
            glanceId = id,
        )
        return preferences[WidgetStateKeys.payload]
    }

    private suspend fun updateTarget(
        widget: GlanceAppWidget,
        id: GlanceId,
        payload: String,
        settings: WidgetDisplaySettings,
    ) {
        updateAppWidgetState(
            context = applicationContext,
            definition = PreferencesGlanceStateDefinition,
            glanceId = id,
        ) { preferences ->
            preferences.toMutablePreferences().apply {
                setupState(payload, settings)
            }
        }
        widget.update(applicationContext, id)
    }

    private fun MutablePreferences.setupState(
        payload: String,
        settings: WidgetDisplaySettings,
    ) {
        this[WidgetStateKeys.payload] = payload
        this[WidgetStateKeys.language] = settings.language.name
        this[WidgetStateKeys.theme] = settings.theme.name
    }

    private data class WidgetTarget(
        val widget: GlanceAppWidget,
        val ids: List<GlanceId>,
    )

    companion object {
        private const val MAX_RETRY_COUNT = 3
    }
}
