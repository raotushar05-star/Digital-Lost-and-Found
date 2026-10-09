import apiClient from "./apiClient";

export const feedbackService = {
  getMine: () => apiClient.get("/feedback").then((response) => response.data),
  create: (payload) => apiClient.post("/feedback", payload).then((response) => response.data),
  getAll: (params = {}) => apiClient.get("/admin/feedback", { params }).then((response) => response.data),
  respond: (id, payload) => apiClient.patch(`/admin/feedback/${id}`, payload).then((response) => response.data)
};