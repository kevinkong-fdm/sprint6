import { apiRequest } from "../../../api/client";

export type AddressInput = {
  addressType: "HOME" | "MAILING" | "WORK";
  line1: string;
  line2?: string;
  city: string;
  region?: string;
  postalCode: string;
  countryCode: string;
};

export type ContactPreferenceInput = {
  channel: "EMAIL" | "SMS" | "PUSH";
  optIn: boolean;
};

export type CustomerUpdateRequest = {
  givenName?: string;
  familyName?: string;
  phoneNumber?: string;
  preferredLanguage?: string;
  addresses?: AddressInput[];
  contactPreferences?: ContactPreferenceInput[];
};

export type CustomerResponse = {
  customerId: string;
  email: string;
  givenName: string;
  familyName: string;
  phoneNumber?: string;
  dateOfBirth?: string;
  preferredLanguage?: string;
  addresses?: Array<{
    addressId: string;
    addressType: "HOME" | "MAILING" | "WORK";
    line1: string;
    line2?: string;
    city: string;
    region?: string;
    postalCode: string;
    countryCode: string;
  }>;
  contactPreferences?: Array<{
    preferenceId: string;
    channel: "EMAIL" | "SMS" | "PUSH";
    optIn: boolean;
  }>;
  createdAt: string;
  updatedAt: string;
};

export async function getCustomer(accessToken: string, customerId: string) {
  return apiRequest<CustomerResponse>(`/customers/${customerId}`, {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}

export async function updateCustomer(accessToken: string, customerId: string, payload: CustomerUpdateRequest) {
  return apiRequest<CustomerResponse>(`/customers/${customerId}`, {
    method: "PATCH",
    body: JSON.stringify(payload),
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}

export async function deleteCustomer(accessToken: string, customerId: string) {
  return apiRequest<void>(`/customers/${customerId}`, {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
}
