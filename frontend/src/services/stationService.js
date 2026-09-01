import apiClient from "./apiClient";

export const stationService = {
  getStations: () => apiClient.get("/stations").then((r) => r.data),
  getStation: (id) => apiClient.get(`/stations/${id}`).then((r) => r.data),
  create: (payload) => apiClient.post("/admin/stations", payload).then((r) => r.data),
  update: (id, payload) => apiClient.put(`/admin/stations/${id}`, payload).then((r) => r.data)
};
