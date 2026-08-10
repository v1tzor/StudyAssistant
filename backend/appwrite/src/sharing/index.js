import { createServices } from '../shared/appwrite.js';
import { PublicError, publicFailure, requestBody } from '../shared/http.js';
import { cleanupTemporaryContent } from './cleanup.js';
import { createHomeworkShare, fetchHomeworkShare } from './homework.js';
import {
  claimScheduleShare,
  confirmScheduleShare,
  createScheduleShare,
  releaseScheduleShare,
} from './schedule.js';

export default async ({ req, res, error: logError }) => {
  try {
    if (req.headers['x-appwrite-trigger'] === 'schedule') {
      return res.json(await cleanupTemporaryContent(createServices(req)));
    }
    const body = requestBody(req);
    const services = createServices(req);
    switch (body.operation) {
      case 'homework.create':
        return res.json(await createHomeworkShare(services, body));
      case 'homework.fetch':
        return res.json(await fetchHomeworkShare(services, body));
      case 'schedule.create':
        return res.json(await createScheduleShare(services, body));
      case 'schedule.claim':
        return res.json(await claimScheduleShare(services, body));
      case 'schedule.confirm':
        return res.json(await confirmScheduleShare(services, body));
      case 'schedule.release':
        return res.json(await releaseScheduleShare(services, body));
      default:
        throw new PublicError('invalid', 400);
    }
  } catch (error) {
    if (!(error instanceof PublicError)) {
      logError(`${error?.name || 'Error'}:${error?.code || ''}:${error?.message || ''}`);
    }
    const failure = publicFailure(error);
    return res.json(
      { errorCode: failure.publicCode, retryAt: failure.retryAt },
      failure.status,
    );
  }
};
