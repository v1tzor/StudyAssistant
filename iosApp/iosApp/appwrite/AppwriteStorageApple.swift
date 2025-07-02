//
//  AppwriteRealtime 2.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 30.06.2025.
//  Copyright © 2025 orgName. All rights reserved.
//


import Foundation
import shared
import Appwrite
import AppwriteModels
import AppwriteEnums
import JSONCodable
import NIO

public class AppwriteStorageApple : RemoteAppwriteStorageApple {
    
    private var storage: Storage
    
    init(storage: Storage) {
        self.storage = storage
    }
    
    @nonobjc
    public override func __createFile(
        bucketId: String,
        fileId: String,
        fileBytes: KotlinByteArray,
        filename: String,
        mimeType: String,
        permissions: [String]?,
        onProgress: ((RemoteUploadProgress) -> Void)?,
        completionHandler: @escaping (RemoteFile?, (any Error)?) -> Void
    ) {
        Task {
            do {
                let result = try await __createFile(
                    bucketId: bucketId,
                    fileId: fileId,
                    fileBytes: fileBytes,
                    filename: filename,
                    mimeType: mimeType,
                    permissions: permissions,
                    onProgress: onProgress
                )
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __createFile(
        bucketId: String,
        fileId: String,
        fileBytes: KotlinByteArray,
        filename: String,
        mimeType: String,
        permissions: [String]?,
        onProgress: ((RemoteUploadProgress) -> Void)?
    ) async throws -> RemoteFile {
        let buffer = kotlinByteArrayToByteBufferWithIterator(fileBytes)
        let file = try await storage.createFile(
            bucketId: bucketId,
            fileId: fileId,
            file: InputFile.fromBuffer(buffer, filename: filename, mimeType: mimeType),
            permissions: permissions,
            onProgress: { progress in
                let remoteProgress = RemoteUploadProgress.init(
                    id: progress.id,
                    progress: progress.progress,
                    sizeUploaded: Int64(progress.sizeUploaded),
                    chunksTotal: Int32(progress.chunksTotal),
                    chunksUploaded: Int32(progress.chunksUploaded)
                )
                onProgress?(remoteProgress)
            }
        )
        return file.mapToRemote()
    }
    
    @nonobjc
    public override func __deleteFile(
        bucketId: String,
        fileId: String,
        completionHandler: @escaping (Any?, (any Error)?) -> Void
    ) {
        Task {
            do {
                let result = try await __deleteFile(bucketId: bucketId, fileId: fileId)
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __deleteFile(
        bucketId: String,
        fileId: String
    ) async throws -> Any {
        return try await storage.deleteFile(bucketId: bucketId, fileId: fileId)
    }
    
    @nonobjc
    public override func __getFile(
        bucketId: String,
        fileId: String,
        completionHandler: @escaping (RemoteFile?, (any Error)?) -> Void
    ) {
        Task {
            do {
                let result = try await __getFile(bucketId: bucketId, fileId: fileId)
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __getFile(
        bucketId: String,
        fileId: String
    ) async throws -> RemoteFile {
        let file = try await storage.getFile(bucketId: bucketId, fileId: fileId)
        return file.mapToRemote()
    }
    
    @nonobjc
    public override func __getFileDownload(
        bucketId: String,
        fileId: String,
        token: String?,
        completionHandler: @escaping (KotlinByteArray?, (any Error)?) -> Void
    ) {
        Task {
            do {
                let result = try await __getFileDownload(bucketId: bucketId, fileId: fileId, token: token)
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __getFileDownload(
        bucketId: String,
        fileId: String,
        token: String?
    ) async throws -> KotlinByteArray {
        let byteBuffer = try await storage.getFileDownload(bucketId: bucketId, fileId: fileId, token: token)
        return byteBufferToKotlinByteArray(byteBuffer)
    }
    
    @nonobjc
    public override func __getFilePreview(
        bucketId: String,
        fileId: String,
        width: KotlinLong?,
        height: KotlinLong?,
        gravity: shared.RemoteImageGravity?,
        quality: KotlinLong?,
        borderWidth: KotlinLong?,
        borderColor: String?,
        borderRadius: KotlinLong?,
        opacity: KotlinDouble?,
        rotation: KotlinLong?,
        background: String?,
        output: shared.RemoteImageFormat?,
        token: String?,
        completionHandler: @escaping (KotlinByteArray?, (any Error)?) -> Void
    ) {
        Task {
            do {
                let result = try await __getFilePreview(
                    bucketId: bucketId,
                    fileId: fileId,
                    width: width,
                    height: height,
                    gravity: gravity,
                    quality: quality,
                    borderWidth: borderWidth,
                    borderColor: borderColor,
                    borderRadius: borderRadius,
                    opacity: opacity,
                    rotation: rotation,
                    background: background,
                    output: output,
                    token: token
                )
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __getFilePreview(
        bucketId: String,
        fileId: String,
        width: KotlinLong?,
        height: KotlinLong?,
        gravity: shared.RemoteImageGravity?,
        quality: KotlinLong?,
        borderWidth: KotlinLong?,
        borderColor: String?,
        borderRadius: KotlinLong?,
        opacity: KotlinDouble?,
        rotation: KotlinLong?,
        background: String?,
        output: shared.RemoteImageFormat?,
        token: String?
    ) async throws -> KotlinByteArray {
        let byteBuffer = try await storage.getFilePreview(
            bucketId: bucketId,
            fileId: fileId,
            width: width?.intValue,
            height: height?.intValue,
            gravity: ImageGravity.init(rawValue: gravity?.name ?? ImageGravity.center.rawValue),
            quality: quality?.intValue,
            borderWidth: borderWidth?.intValue,
            borderColor: borderColor,
            borderRadius: borderRadius?.intValue,
            opacity: opacity?.doubleValue,
            rotation: rotation?.intValue,
            background: background,
            output: ImageFormat(rawValue: output?.name ?? ImageFormat.png.rawValue),
            token: token
        )
        return byteBufferToKotlinByteArray(byteBuffer)
    }
    
    @nonobjc
    public override func __getFileView(
        bucketId: String,
        fileId: String,
        token: String?,
        completionHandler: @escaping (KotlinByteArray?, (any Error)?
    ) -> Void) {
        Task {
            do {
                let result = try await __getFileView(
                    bucketId: bucketId,
                    fileId: fileId,
                    token: token
                )
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __getFileView(
        bucketId: String,
        fileId: String,
        token: String?
    ) async throws -> KotlinByteArray {
        let byteBuffer = try await storage.getFileView(bucketId: bucketId, fileId: fileId, token: token)
        return byteBufferToKotlinByteArray(byteBuffer)
    }
    
    @nonobjc
    public override func __listFiles(
        bucketId: String,
        queries: [String]?,
        search: String?,
        completionHandler: @escaping (RemoteFileList?, (any Error)?) -> Void
    ) {
        Task {
            do {
                let result = try await __listFiles(
                    bucketId: bucketId,
                    queries: queries,
                    search: search
                )
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __listFiles(
        bucketId: String,
        queries: [String]?,
        search: String?
    ) async throws -> RemoteFileList {
        let fileLists = try await storage.listFiles(bucketId: bucketId, queries: queries, search: search)
        return RemoteFileList(total: Int64(fileLists.total), files: fileLists.files.compactMap { $0.mapToRemote() })
    }
    
    @nonobjc
    public override func __updateFile(
        bucketId: String,
        fileId: String,
        name: String?,
        permissions: [String]?,
        completionHandler: @escaping (RemoteFile?, (any Error)?) -> Void
    ) {
        Task {
            do {
                let result = try await __updateFile(
                    bucketId: bucketId,
                    fileId: fileId,
                    name: name,
                    permissions: permissions
                )
                completionHandler(result, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
    
    public override func __updateFile(
        bucketId: String,
        fileId: String,
        name: String?,
        permissions: [String]?
    ) async throws -> RemoteFile {
        let file = try await storage.updateFile(bucketId: bucketId, fileId: fileId, name: name, permissions: permissions)
        return file.mapToRemote()
    }
}

func kotlinByteArrayToByteBufferWithIterator(_ kotlinArray: KotlinByteArray) -> ByteBuffer {
    let iterator = kotlinArray.iterator()
    var bytes = [Int8]()
    
    while iterator.hasNext() {
        bytes.append(iterator.nextByte())
    }
    
    return ByteBuffer(bytes: bytes.map { UInt8(bitPattern: $0) })
}

func byteBufferToKotlinByteArray(_ buffer: ByteBuffer) -> KotlinByteArray {
    let readableBytes = buffer.readableBytes
    guard let bytes = buffer.getBytes(at: buffer.readerIndex, length: readableBytes) else {
        return KotlinByteArray(size: 0)
    }
    
    let kotlinArray = KotlinByteArray(size: Int32(readableBytes))
    
    for (index, byte) in bytes.enumerated() {
        kotlinArray.set(index: Int32(index), value: Int8(bitPattern: byte))
    }
    
    return kotlinArray
}

extension AppwriteModels.File {
    func mapToRemote() -> RemoteFile {
        return RemoteFile(
            id: id,
            bucketId: bucketId,
            createdAt: createdAt,
            updatedAt: updatedAt,
            permissions: permissions,
            name: name,
            signature: signature,
            mimeType: mimeType,
            sizeOriginal: Int64(sizeOriginal),
            chunksTotal: Int64(chunksTotal),
            chunksUploaded: Int64(chunksUploaded)
        )
    }
}
