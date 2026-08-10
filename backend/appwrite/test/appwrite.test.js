import test from 'node:test';
import assert from 'node:assert/strict';

import { downloadBytes } from '../src/shared/appwrite.js';

test('download bytes supports JSON decoded by the Appwrite SDK', async () => {
  const decoded = Object.assign(Object.create(null), { value: 'payload' });
  const storage = {
    getFileDownload: async () => decoded,
  };

  const result = await downloadBytes(storage, 'file-id');

  assert.deepEqual(JSON.parse(result.toString('utf8')), { value: 'payload' });
});
