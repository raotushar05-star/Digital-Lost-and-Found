import apiClient from "./apiClient";

export const searchService = {
  search: (params) => apiClient.get("/found-items", { params }).then((r) => r.data),
  nearby: (params) => apiClient.get("/found-items/nearby", { params }).then((r) => r.data),
  getPublicDetail: (id) => apiClient.get(`/found-items/${id}/public`).then((r) => r.data)
};
