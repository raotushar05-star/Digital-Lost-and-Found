import apiClient from "./apiClient";

export const foundReportService = {
  create: (payload) => apiClient.post("/found-reports", payload).then((r) => r.data),
  getById: (id) => apiClient.get(`/found-reports/${id}`).then((r) => r.data)
};
