import apiClient from "./apiClient";

export const policeService = {
  intakeFoundItem: (payload) => apiClient.post("/police/found-items", payload).then((r) => r.data),
  getFoundItemDetail: (id) => apiClient.get(`/police/found-items/${id}`).then((r) => r.data),
  getStationInventory: (stationId) => apiClient.get(`/police/stations/${stationId}/found-items`).then((r) => r.data),
  uploadFoundItemPhoto: (id, file, visibility) => {
    const form = new FormData();
    form.append("file", file);
    if (visibility) form.append("visibility", visibility);
    return apiClient
      .post(`/police/found-items/${id}/photos`, form, { headers: { "Content-Type": "multipart/form-data" } })
      .then((r) => r.data);
  },
  verifyFoundItem: (id, payload) => apiClient.post(`/police/found-items/${id}/verify`, payload).then((r) => r.data),
  getClaimsForFoundItem: (id) => apiClient.get(`/police/found-items/${id}/claims`).then((r) => r.data),
  verifyClaim: (claimId, payload) => apiClient.post(`/police/claims/${claimId}/verify`, payload).then((r) => r.data),
  getDisputes: (foundItemId) => apiClient.get(`/police/found-items/${foundItemId}/disputes`).then((r) => r.data),
  raiseDispute: (foundItemId, payload) => apiClient.post(`/police/found-items/${foundItemId}/disputes`, payload).then((r) => r.data),
  updateDispute: (disputeId, payload) => apiClient.put(`/police/disputes/${disputeId}`, payload).then((r) => r.data),
  recordHandover: (claimId, payload) => apiClient.post(`/police/claims/${claimId}/handover`, payload).then((r) => r.data),
  getDashboard: () => apiClient.get("/police/dashboard").then((r) => r.data),
  getReportsSummary: (params) => apiClient.get("/police/reports/summary", { params }).then((r) => r.data),
  getStationReports: (params) => apiClient.get("/police/reports/stations", { params }).then((r) => r.data)
};
