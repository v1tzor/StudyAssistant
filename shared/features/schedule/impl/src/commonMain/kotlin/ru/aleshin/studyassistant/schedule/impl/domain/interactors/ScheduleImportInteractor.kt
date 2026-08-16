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

package ru.aleshin.studyassistant.schedule.impl.domain.interactors

import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrEngine
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrLanguage
import ru.aleshin.studyassistant.core.common.functional.ocr.ScheduleOcrDocument
import ru.aleshin.studyassistant.core.common.functional.ocr.ScheduleTableParser
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.common.DayOfNumberedWeek
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportDraft
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportEntry
import ru.aleshin.studyassistant.core.domain.entities.schedules.importing.ScheduleImportRequest
import ru.aleshin.studyassistant.core.domain.entities.settings.LanguageType
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ScheduleImportRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleImportException
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleTextRecognitionException
import ru.aleshin.studyassistant.schedule.impl.domain.validation.ScheduleImportValidator

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface ScheduleImportInteractor {

    suspend fun recognizeText(imageBytes: ByteArray): DomainResult<ScheduleFailures, ScheduleOcrDocument>
    suspend fun extractDraft(document: ScheduleOcrDocument, numberOfWeeks: Int): DomainResult<ScheduleFailures, ScheduleImportDraft>
    suspend fun applyDraft(draft: ScheduleImportDraft): UnitDomainResult<ScheduleFailures>

    class Base(
        private val importRepository: ScheduleImportRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val organizationsRepository: OrganizationsRepository,
        private val subjectsRepository: SubjectsRepository,
        private val employeeRepository: EmployeeRepository,
        private val settingsRepository: GeneralSettingsRepository,
        private val ocrEngine: OcrEngine,
        private val tableParser: ScheduleTableParser,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val dateManager: DateManager,
        private val validator: ScheduleImportValidator,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : ScheduleImportInteractor {

        override suspend fun recognizeText(imageBytes: ByteArray) = eitherWrapper.wrap {
            if (imageBytes.isEmpty()) throw ScheduleTextRecognitionException.InvalidImage
            if (imageBytes.size > MAX_IMAGE_BYTES) {
                throw ScheduleTextRecognitionException.ImageTooLarge
            }
            val settings = settingsRepository.fetchSettings().first()
            val language = when (settings.languageType) {
                LanguageType.DEFAULT -> deviceInfoProvider.fetchDeviceLanguage()
                LanguageType.EN -> "en"
                LanguageType.RU -> "ru"
            }
            val ocrLanguages = if (language.startsWith("ru")) {
                setOf(OcrLanguage.RUSSIAN, OcrLanguage.ENGLISH)
            } else {
                setOf(OcrLanguage.ENGLISH, OcrLanguage.RUSSIAN)
            }

            val ocrResult = ocrEngine.recognize(imageBytes, ocrLanguages)
            if (!validator.isSourceTextValid(ocrResult.text)) {
                throw ScheduleTextRecognitionException.NoText
            }
            tableParser.parse(ocrResult)
        }

        override suspend fun extractDraft(document: ScheduleOcrDocument, numberOfWeeks: Int) = eitherWrapper.wrap {
            val draft = importRepository.extractDraft(
                ScheduleImportRequest(
                    requestId = randomUUID(),
                    rawText = document.rawText,
                    ocrDocument = document,
                    locale = deviceInfoProvider.fetchDeviceLanguage(),
                    timeZone = TimeZone.currentSystemDefault().id,
                    numberOfWeeks = numberOfWeeks,
                ),
            )

            val organizations = organizationsRepository.fetchAllOrganization().first()
            val allSubjects = organizations.flatMap { 
                subjectsRepository.fetchAllSubjectsByOrganization(it.uid).first()
            }
            val allEmployees = organizations.flatMap {
                employeeRepository.fetchAllEmployeeByOrganization(it.uid).first()
            }

            val linkedEntries = draft.entries.map { entry ->
                val matchedOrg = organizations.matchOrganization(entry.organization)
                val matchedSubject = allSubjects.filter { 
                    it.organizationId == matchedOrg?.uid || matchedOrg == null 
                }.fuzzyMatch(entry.subject) { it.name }
                
                val matchedTeacher = allEmployees.filter {
                    it.organizationId == matchedOrg?.uid || matchedOrg == null
                }.fuzzyMatch(entry.teacher) {
                    listOfNotNull(it.secondName, it.firstName, it.patronymic).joinToString(" ")
                }

                entry.copy(
                    organizationId = matchedOrg?.uid,
                    subjectId = matchedSubject?.uid,
                    teacherId = matchedTeacher?.uid,
                )
            }

            draft.copy(entries = linkedEntries)
        }

        override suspend fun applyDraft(draft: ScheduleImportDraft) = eitherWrapper.wrapUnit {
            if (!validator.isDraftValid(draft)) throw ScheduleImportException.InvalidDraft

            val organizations = organizationsRepository.fetchAllOrganization().first()
            val fallbackOrganization = organizations.firstOrNull(Organization::isMain)
                ?: organizations.firstOrNull()
                ?: throw ScheduleImportException.NoOrganization
            val currentTime = dateManager.fetchCurrentInstant()
            val schedules = draft.entries
                .filter(ScheduleImportEntry::included)
                .groupBy { entry -> entry.repeatWeek to entry.dayOfWeek }
                .map { (day, entries) ->
                    val scheduleId = randomUUID()
                    BaseSchedule.createActual(
                        currentDate = currentTime,
                        dayOfNumberedWeek = DayOfNumberedWeek(
                            dayOfWeek = DayOfWeek.entries[day.second - 1],
                            week = NumberOfRepeatWeek.valueOf(day.first),
                        ),
                        classes = entries.sortedBy { entry -> entry.startTime }.map { entry ->
                            val organization = entry.organizationId?.let { id -> 
                                organizations.find { it.uid == id } 
                            } ?: organizations.matchOrganization(entry.organization) ?: fallbackOrganization
                            
                            val subject = entry.subjectId?.let { id ->
                                subjectsRepository.fetchSubjectById(id).first()
                            }
                                
                            val teacher = entry.teacherId?.let { id ->
                                employeeRepository.fetchEmployeeById(id).first()
                            }
                                
                            val start = requireNotNull(validator.parseTime(entry.startTime))
                            val end = requireNotNull(validator.parseTime(entry.endTime))
                            Class(
                                uid = randomUUID(),
                                scheduleId = scheduleId,
                                organization = organization.convertToShort(),
                                eventType = entry.eventType?.name?.let { EventType.valueOf(it) }
                                    ?: subject?.eventType
                                    ?: EventType.CLASS,
                                subject = subject,
                                customData = entry.subject?.trim().takeIf { subject == null },
                                teacher = teacher ?: subject?.teacher,
                                office = entry.office?.trim().orEmpty(),
                                location = entry.location?.trim()?.takeIf(String::isNotEmpty)?.let {
                                    ContactInfo(value = it)
                                },
                                timeRange = TimeRange(
                                    from = currentTime.startThisDay().setHoursAndMinutes(start),
                                    to = currentTime.startThisDay().setHoursAndMinutes(end),
                                ),
                                number = entry.classNumber ?: 0,
                            )
                        },
                        createdAt = currentTime.toEpochMilliseconds(),
                    ).copy(uid = scheduleId)
                }
            baseScheduleRepository.addOrUpdateSchedulesGroup(schedules)
        }

        private fun List<Organization>.matchOrganization(name: String?): Organization? {
            val normalizedName = name.normalized()
            if (normalizedName.isEmpty()) return null

            return fuzzyMatch(name) { it.shortName } ?: fuzzyMatch(name) { it.fullName ?: "" }
        }

        private fun <T> List<T>.fuzzyMatch(query: String?, selector: (T) -> String): T? {
            val normalizedQuery = query?.normalized() ?: return null
            if (normalizedQuery.isEmpty()) return null

            val matches = map { item ->
                val name = selector(item).normalized()
                val distance = levenshteinDistance(name, normalizedQuery)
                val similarity = 1.0 - (distance.toDouble() / maxOf(name.length, normalizedQuery.length))
                item to similarity
            }

            return matches
                .filter { (item, similarity) -> 
                    val name = selector(item).normalized()
                    similarity >= FUZZY_THRESHOLD || name.contains(normalizedQuery) || normalizedQuery.contains(name)
                }
                .maxByOrNull { it.second }
                ?.first
        }

        private fun levenshteinDistance(s1: String, s2: String): Int {
            if (s1 == s2) return 0
            if (s1.isEmpty()) return s2.length
            if (s2.isEmpty()) return s1.length

            val dp = IntArray(s2.length + 1) { it }
            for (i in 1..s1.length) {
                var prev = i
                for (j in 1..s2.length) {
                    val current = if (s1[i - 1] == s2[j - 1]) dp[j - 1] else 1 + minOf(dp[j - 1], dp[j], prev)
                    dp[j - 1] = prev
                    prev = current
                }
                dp[s2.length] = prev
            }
            return dp[s2.length]
        }

        private fun String?.normalized(): String = orEmpty().trim().lowercase()

        private companion object {
            const val MAX_IMAGE_BYTES = 20 * 1024 * 1024
            const val FUZZY_THRESHOLD = 0.5
        }
    }
}
