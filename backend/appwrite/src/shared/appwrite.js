import {
  Client,
  ID,
  Query,
  Storage,
  TablesDB,
} from 'node-appwrite';
import { InputFile } from 'node-appwrite/file';

import { config } from './config.js';

export function createServices(req) {
  const client = new Client()
    .setEndpoint(process.env.APPWRITE_FUNCTION_API_ENDPOINT)
    .setProject(process.env.APPWRITE_FUNCTION_PROJECT_ID)
    .setKey(req.headers['x-appwrite-key']);

  return {
    storage: new Storage(client),
    tables: new TablesDB(client),
  };
}

export async function findRow(tables, tableId, queries, transactionId) {
  const result = await tables.listRows({
    databaseId: config.databaseId,
    tableId,
    queries: [...queries, Query.limit(1)],
    transactionId,
    ttl: 0,
  });
  return result.rows[0] || null;
}

export async function listRows(tables, tableId, queries = [], transactionId) {
  const rows = [];
  let cursor;
  do {
    const pageQueries = [...queries, Query.limit(100)];
    if (cursor) pageQueries.push(Query.cursorAfter(cursor));
    const result = await tables.listRows({
      databaseId: config.databaseId,
      tableId,
      queries: pageQueries,
      transactionId,
      ttl: 0,
    });
    rows.push(...result.rows);
    cursor = result.rows.length === 100 ? result.rows.at(-1)?.$id : undefined;
  } while (cursor);
  return rows;
}

export async function uploadBytes(storage, bytes, filename) {
  const file = await storage.createFile({
    bucketId: config.bucketId,
    fileId: ID.unique(),
    file: InputFile.fromBuffer(bytes, filename),
    permissions: [],
  });
  return file.$id;
}

export async function downloadBytes(storage, fileId) {
  const result = await storage.getFileDownload({
    bucketId: config.bucketId,
    fileId,
  });
  if (result instanceof ArrayBuffer) return Buffer.from(result);
  if (ArrayBuffer.isView(result)) {
    return Buffer.from(result.buffer, result.byteOffset, result.byteLength);
  }
  if (typeof result === 'string') return Buffer.from(result, 'utf8');
  if (result && typeof result === 'object') {
    return Buffer.from(JSON.stringify(result), 'utf8');
  }
  throw new TypeError('Unsupported Appwrite file response');
}

export async function deleteFile(storage, fileId) {
  if (!fileId) return;
  try {
    await storage.deleteFile({ bucketId: config.bucketId, fileId });
  } catch (error) {
    if (error?.code !== 404) throw error;
  }
}

export { ID, Query };
