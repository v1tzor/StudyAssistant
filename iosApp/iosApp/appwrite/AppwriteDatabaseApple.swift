//
//  AppwriteDatabaseApple.swift
//  iosApp
//
//  Created by Stanislav Aleshin on 26.06.2025.
//  Copyright © 2025 orgName. All rights reserved.
//
import Foundation
import shared
import Appwrite
import AppwriteModels
import JSONCodable


public class AppwriteDatabaseApple : RemoteAppwriteDatabaseApple {
    
    private var database: Databases
    
    init(database: Databases) {
        self.database = database
    }
    
    
    public override func __createDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any,
        permissions: [String]?
    ) async throws -> RemoteDocument<NSDictionary> {
        let result: AppwriteModels.Document<[String: AnyCodable]> = try await database.createDocument(
           databaseId: databaseId,
           collectionId: collectionId,
           documentId: documentId,
           data: data,
           permissions: permissions
        )
        return RemoteDocument.init(
            id: result.id as String,
            collectionId: result.collectionId,
            databaseId: result.databaseId,
            createdAt: result.createdAt,
            updatedAt: result.updatedAt,
            permissions: result.permissions,
            data: convertToKotlinCompatibleDictionary(result.data)
        )
       }

       public override func __deleteDocument(
            databaseId: String,
            collectionId: String,
            documentId: String
       ) async throws -> Any {
           return try await database.deleteDocument(
               databaseId: databaseId,
               collectionId: collectionId,
               documentId: documentId
           )
       }


       public override func __getDocument(
            databaseId: String,
            collectionId: String,
            documentId: String,
            queries: [String]?
       ) async throws -> RemoteDocument<NSDictionary> {
           let result: AppwriteModels.Document<[String: AnyCodable]> = try await database.getDocument(
               databaseId: databaseId,
               collectionId: collectionId,
               documentId: documentId,
               queries: queries
           )
           return RemoteDocument(
                id: result.id as String,
                collectionId: result.collectionId,
                databaseId: result.databaseId,
                createdAt: result.createdAt,
                updatedAt: result.updatedAt,
                permissions: result.permissions,
                data: convertToKotlinCompatibleDictionary(result.data)
           )
       }

       public override func __listDocuments(
            databaseId: String,
            collectionId: String,
            queries: [String]?
       ) async throws -> RemoteDocumentList<NSDictionary> {
           let result: AppwriteModels.DocumentList<[String: AnyCodable]> = try await database.listDocuments(
               databaseId: databaseId,
               collectionId: collectionId,
               queries: queries
           )
           let mapped = result.documents.map {
               RemoteDocument(
                    id: $0.id as String,
                    collectionId: $0.collectionId,
                    databaseId: $0.databaseId,
                    createdAt: $0.createdAt,
                    updatedAt: $0.updatedAt,
                    permissions: $0.permissions,
                    data: $0.data as NSDictionary
               )
           }
           return RemoteDocumentList(total: Int64(result.total), documents: mapped)
       }

       public override func __updateDocument(
            databaseId: String,
            collectionId: String,
            documentId: String,
            data: Any?,
            permissions: [String]?
       ) async throws -> RemoteDocument<NSDictionary> {
           let result: AppwriteModels.Document<[String: AnyCodable]> = try await database.updateDocument(
               databaseId: databaseId,
               collectionId: collectionId,
               documentId: documentId,
               data: data,
               permissions: permissions
           )
           return RemoteDocument(
                id: result.id as String,
                collectionId: result.collectionId,
                databaseId: result.databaseId,
                createdAt: result.createdAt,
                updatedAt: result.updatedAt,
                permissions: result.permissions,
                data: convertToKotlinCompatibleDictionary(result.data)
           )
       }

       public override func __upsertDocument(
            databaseId: String,
            collectionId: String,
            documentId: String,
            data: Any,
            permissions: [String]?
       ) async throws -> RemoteDocument<NSDictionary> {
           let result: AppwriteModels.Document<[String: AnyCodable]> = try await database.upsertDocument(
               databaseId: databaseId,
               collectionId: collectionId,
               documentId: documentId,
               data: data,
               permissions: permissions
           )
           return RemoteDocument(
                id: result.id as String,
                collectionId: result.collectionId,
                databaseId: result.databaseId,
                createdAt: result.createdAt,
                updatedAt: result.updatedAt,
                permissions: result.permissions,
                data: convertToKotlinCompatibleDictionary(result.data)
           )
       }
}
