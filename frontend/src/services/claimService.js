import apiClient from "./apiClient";

export const claimService = {
  create: (foundItemId, payload) => apiClient.post(`/found-items/${foundItemId}/claims`, payload).then((r) => r.data),
  getById: (claimId) => apiClient.get(`/claims/${claimId}`).then((r) => r.data),
  addEvidence: (claimId, evidenceType, description, file) => {
    const form = new FormData();
    form.append("evidenceType", evidenceType);
    if (description) form.append("description", description);
    if (file) form.append("file", file);
    return apiClient.post(`/claims/${claimId}/evidence`, form, { headers: { "Content-Type": "multipart/form-data" } }).then((r) => r.data);
  },
  getEvidence: (claimId) => apiClient.get(`/claims/${claimId}/evidence`).then((r) => r.data)
};
