import apiClient from "./apiClient";

export const caseService = {
  getById: (caseId) => apiClient.get(`/cases/${caseId}`).then((r) => r.data),
  getHistory: (caseId) => apiClient.get(`/cases/${caseId}/history`).then((r) => r.data)
};
