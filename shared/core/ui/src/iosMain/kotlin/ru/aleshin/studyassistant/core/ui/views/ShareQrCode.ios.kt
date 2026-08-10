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

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package ru.aleshin.studyassistant.core.ui.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVAuthorizationStatusDenied
import platform.AVFoundation.AVAuthorizationStatusNotDetermined
import platform.AVFoundation.AVAuthorizationStatusRestricted
import platform.AVFoundation.AVCaptureConnection
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceDiscoverySession
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionBack
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureMetadataOutput
import platform.AVFoundation.AVCaptureMetadataOutputObjectsDelegateProtocol
import platform.AVFoundation.AVCaptureOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.AVMetadataMachineReadableCodeObject
import platform.AVFoundation.AVMetadataObjectTypeQRCode
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType
import platform.CoreGraphics.CGAffineTransformMakeScale
import platform.CoreImage.CIContext
import platform.CoreImage.CIFilter
import platform.CoreImage.createCGImage
import platform.CoreImage.filterWithName
import platform.Foundation.NSData
import platform.Foundation.dataWithBytes
import platform.Foundation.setValue
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.UIImage
import platform.UIKit.UIImageView
import platform.UIKit.UIView
import platform.UIKit.UIViewContentMode
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_main_queue

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
@Composable
actual fun ShareQrCode(content: String, modifier: Modifier) {
    val image = remember(content) { createQrImage(content) }
    UIKitView(
        factory = {
            UIImageView().apply {
                contentMode = UIViewContentMode.UIViewContentModeScaleAspectFit
                this.image = image
            }
        },
        modifier = modifier,
        update = { imageView -> imageView.image = image },
    )
}

@Composable
actual fun ShareCodeScanner(
    modifier: Modifier,
    onResult: (String) -> Unit,
    onPermissionDenied: () -> Unit,
) {
    var cameraAccess by remember { mutableStateOf(CameraAccess.UNDEFINED) }
    val currentOnResult by rememberUpdatedState(onResult)
    val currentOnPermissionDenied by rememberUpdatedState(onPermissionDenied)

    LaunchedEffect(Unit) {
        when (AVCaptureDevice.Companion.authorizationStatusForMediaType(AVMediaTypeVideo)) {
            AVAuthorizationStatusAuthorized -> cameraAccess = CameraAccess.AUTHORIZED
            AVAuthorizationStatusDenied, AVAuthorizationStatusRestricted -> {
                cameraAccess = CameraAccess.DENIED
                currentOnPermissionDenied()
            }

            AVAuthorizationStatusNotDetermined -> {
                AVCaptureDevice.Companion.requestAccessForMediaType(AVMediaTypeVideo) { granted: Boolean ->
                    dispatch_async(dispatch_get_main_queue()) {
                        cameraAccess = if (granted) CameraAccess.AUTHORIZED else CameraAccess.DENIED
                        if (!granted) currentOnPermissionDenied()
                    }
                }
            }
        }
    }

    if (cameraAccess == CameraAccess.AUTHORIZED) {
        AuthorizedShareCodeScanner(
            modifier = modifier,
            onResult = currentOnResult,
        )
    } else {
        Box(modifier = modifier.fillMaxSize())
    }
}

@Composable
private fun AuthorizedShareCodeScanner(
    modifier: Modifier,
    onResult: (String) -> Unit,
) {
    val camera = remember {
        AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
            deviceTypes = listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
            mediaType = AVMediaTypeVideo,
            position = AVCaptureDevicePositionBack,
        ).devices.firstOrNull() as? AVCaptureDevice
    }
    if (camera == null) {
        Box(modifier = modifier.fillMaxSize())
        return
    }

    val captureSession = remember(camera) {
        AVCaptureSession().apply {
            AVCaptureDeviceInput.deviceInputWithDevice(camera, null)?.let(::addInput)
        }
    }
    val delegate = remember(captureSession) {
        object : NSObject(), AVCaptureMetadataOutputObjectsDelegateProtocol {
            override fun captureOutput(
                output: AVCaptureOutput,
                didOutputMetadataObjects: List<*>,
                fromConnection: AVCaptureConnection,
            ) {
                val code = (didOutputMetadataObjects.firstOrNull()
                        as? AVMetadataMachineReadableCodeObject)?.stringValue
                if (!code.isNullOrBlank()) {
                    captureSession.stopRunning()
                    onResult(code)
                }
            }
        }
    }
    val previewLayer = remember(captureSession) {
        AVCaptureVideoPreviewLayer(session = captureSession).apply {
            videoGravity = AVLayerVideoGravityResizeAspectFill
        }
    }
    remember(captureSession, delegate) {
        AVCaptureMetadataOutput().also { output ->
            if (captureSession.canAddOutput(output)) {
                captureSession.addOutput(output)
                output.setMetadataObjectsDelegate(delegate, dispatch_get_main_queue())
                output.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
            }
        }
    }

    UIKitView(
        factory = {
            UIView().also { view ->
                view.layer.addSublayer(previewLayer)
                captureSession.startRunning()
            }
        },
        modifier = modifier.fillMaxSize(),
        update = { view ->
            CATransaction.begin()
            CATransaction.setValue(true, kCATransactionDisableActions)
            previewLayer.setFrame(view.bounds)
            CATransaction.commit()
        },
        onRelease = { captureSession.stopRunning() },
    )

    DisposableEffect(captureSession) {
        onDispose { captureSession.stopRunning() }
    }
}

private fun createQrImage(content: String): UIImage? {
    val bytes = content.encodeToByteArray()
    val data = bytes.usePinned { pinned ->
        NSData.Companion.dataWithBytes(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    val filter = CIFilter.Companion.filterWithName("CIQRCodeGenerator") ?: return null
    filter.setValue(data, forKey = "inputMessage")
    filter.setValue("M", forKey = "inputCorrectionLevel")
    val output = filter.outputImage ?: return null
    val scaled = output.imageByApplyingTransform(CGAffineTransformMakeScale(12.0, 12.0))
    val context = CIContext()
    val cgImage = context.createCGImage(scaled, scaled.extent) ?: return null
    return UIImage.Companion.imageWithCGImage(cgImage)
}

private enum class CameraAccess { UNDEFINED, DENIED, AUTHORIZED }
