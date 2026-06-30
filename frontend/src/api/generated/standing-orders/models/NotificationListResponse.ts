/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { NotificationEvent } from './NotificationEvent';
export type NotificationListResponse = {
    correlationId: string;
    timestamp: string;
    data: {
        items: Array<NotificationEvent>;
        page: number;
        size: number;
        total: number;
    };
};

