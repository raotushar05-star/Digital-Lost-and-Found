import apiClient from "./apiClient";

export const caseService = {
  getById: (caseId) => apiClient.get(`/cases/${caseId}`).then((r) => r.data),
  getHistory: (caseId) => apiClient.get(`/cases/${caseId}/history`).then((r) => r.data),
  getFile: (caseId) => apiClient.get(`/cases/${caseId}/file`).then((r) => r.data),
  downloadFilePdf: (caseId) => apiClient.get(`/cases/${caseId}/file/pdf`, { responseType: "blob" })
};
