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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.subjects.SubjectsLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
class SubjectsRepositoryImpl(
    private val localDataSource: SubjectsLocalDataSource,
) : SubjectsRepository {

    override suspend fun addOrUpdateSubject(subject: Subject): UID {
        val updatedSubject = subject.copy(uid = subject.uid.ifBlank { randomUUID() })
        localDataSource.addOrUpdateSubject(updatedSubject.mapToLocalData())
        return updatedSubject.uid
    }

    override suspend fun addOrUpdateSubjectsGroup(subjects: List<Subject>) {
        val updatedSubjects = subjects.map { subject ->
            subject.copy(uid = subject.uid.ifBlank { randomUUID() })
        }
        localDataSource.addOrUpdateSubjects(updatedSubjects.map { it.mapToLocalData() })
    }

    override suspend fun fetchSubjectById(uid: UID): Flow<Subject?> {
        return localDataSource.fetchSubjectDetailsById(uid).map { subject -> subject?.mapToDomain() }
    }

    override suspend fun fetchAllSubjectsByOrganization(organizationId: UID): Flow<List<Subject>> {
        return localDataSource.fetchAllSubjectsDetailsByOrg(organizationId).map { subjects ->
            subjects.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchSubjectsByEmployee(employeeId: UID): Flow<List<Subject>> {
        return localDataSource.fetchSubjectsDetailsByEmployee(employeeId).map { subjects ->
            subjects.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchAllSubjectsByNames(names: List<UID>): List<Subject> {
        return localDataSource.fetchAllSubjectsDetailsByNames(names).map { it.mapToDomain() }
    }

    override suspend fun deleteSubject(targetId: UID) {
        localDataSource.deleteSubjectsByIds(listOf(targetId))
    }
}