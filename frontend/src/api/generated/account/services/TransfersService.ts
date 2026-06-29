/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { TransferRequest } from '../models/TransferRequest';
import type { TransferResponse } from '../models/TransferResponse';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class TransfersService {
    /**
     * Transfer funds
     * @returns TransferResponse Transfer posted
     * @throws ApiError
     */
    public static transferFunds({
        requestBody,
    }: {
        requestBody: TransferRequest,
    }): CancelablePromise<TransferResponse> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/transfers',
            body: requestBody,
            mediaType: 'application/json',
            errors: {
                400: `Transfer validation failure`,
                401: `Authentication required or invalid session`,
                403: `Authenticated caller is not authorized for target account`,
                409: `Transfer conflict (pairing or funds)`,
            },
        });
    }
}
