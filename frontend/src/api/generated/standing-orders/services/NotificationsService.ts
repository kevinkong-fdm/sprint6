/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { NotificationListResponse } from '../models/NotificationListResponse';
import type { NotificationPreferenceUpdateRequest } from '../models/NotificationPreferenceUpdateRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class NotificationsService {
    /**
     * Retrieve standing-order notifications
     * Returns notifications ordered by `createdAt` descending then `notificationEventId` descending.
     * @returns NotificationListResponse Notifications listed
     * @throws ApiError
     */
    public static listStandingOrderNotifications({
        eventType,
        dispatchStatus,
        page = 1,
        size = 20,
    }: {
        eventType?: 'LIFECYCLE_UPDATED' | 'EXECUTION_SUCCESS' | 'EXECUTION_FAILURE' | 'EXECUTION_SKIPPED',
        dispatchStatus?: 'PENDING' | 'SENT' | 'FAILED',
        page?: number,
        size?: number,
    }): CancelablePromise<NotificationListResponse> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/notifications/standing-orders',
            query: {
                'eventType': eventType,
                'dispatchStatus': dispatchStatus,
                'page': page,
                'size': size,
            },
            errors: {
                401: `Authentication required or session invalid`,
                500: `Notification dispatch subsystem error`,
            },
        });
    }
    /**
     * Update notification preferences (unsupported)
     * Notifications are fixed default-on in this version. Preference updates are intentionally unsupported.
     * @returns void
     * @throws ApiError
     */
    public static updateNotificationPreferences({
        requestBody,
    }: {
        requestBody: NotificationPreferenceUpdateRequest,
    }): CancelablePromise<void> {
        return __request(OpenAPI, {
            method: 'PATCH',
            url: '/notifications/preferences',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                401: `Authentication required or session invalid`,
                409: `Preference update not supported`,
                500: `Unexpected feature service error`,
            },
        });
    }
}
