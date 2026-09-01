import apiClient from "./apiClient";

export const locationService = {
  create: (payload) => apiClient.post("/locations", payload).then((r) => r.data),
  getById: (id) => apiClient.get(`/locations/${id}`).then((r) => r.data)
};
