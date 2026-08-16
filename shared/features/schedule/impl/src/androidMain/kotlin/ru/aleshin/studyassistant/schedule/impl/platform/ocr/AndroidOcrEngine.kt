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

package ru.aleshin.studyassistant.schedule.impl.platform.ocr

import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrEngine
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrLanguage
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrLine
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrRect
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrResult
import ru.aleshin.studyassistant.core.common.functional.ocr.OcrWord
import ru.aleshin.studyassistant.core.common.managers.AppDispatchers
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
internal class AndroidOcrEngine(
    private val dispatchers: AppDispatchers,
) : OcrEngine {

    override suspend fun recognize(
        image: ByteArray,
        languages: Set<OcrLanguage>
    ): OcrResult = withContext(dispatchers.default) {
        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        return@withContext suspendCancellableCoroutine { continuation ->
            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    val lines = visionText.textBlocks.flatMap { block ->
                        block.lines.map { line ->
                            OcrLine(
                                text = line.text,
                                confidence = null,
                                box = line.boundingBox?.toOcrRect(bitmap.width, bitmap.height) ?: OcrRect(0f, 0f, 0f, 0f),
                                words = line.elements.map { element ->
                                    OcrWord(
                                        text = element.text,
                                        confidence = null,
                                        box = element.boundingBox?.toOcrRect(bitmap.width, bitmap.height) ?: OcrRect(0f, 0f, 0f, 0f)
                                    )
                                }
                            )
                        }
                    }
                    continuation.resume(OcrResult(text = visionText.text, lines = lines))
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    private fun android.graphics.Rect.toOcrRect(width: Int, height: Int): OcrRect {
        return OcrRect(
            left = left.toFloat() / width,
            top = top.toFloat() / height,
            right = right.toFloat() / width,
            bottom = bottom.toFloat() / height
        )
    }
}
