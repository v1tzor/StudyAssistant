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

package ru.aleshin.studyassistant.core.ui.mappers

import io.github.vinceglb.filekit.core.PlatformFile
import ru.aleshin.studyassistant.core.common.functional.getMimeTypeFromFileName
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.ui.models.InputFileUi

/**
 * @author Stanislav Aleshin on 01.07.2025.
 */
fun InputFileUi.mapToDomain() = InputFile(
    uri = uri,
    filename = filename,
    mimeType = mimeType,
    fileBytes = fileBytes,
)

suspend fun PlatformFile.convertToInputFile() = InputFileUi(
    uri = path,
    filename = name,
    mimeType = getMimeTypeFromFileName(name),
    fileBytes = readBytes(),
)