import apiClient from "./apiClient";

export const duplicateService = {
  checkLost: (payload) => apiClient.post("/duplicate-check/lost", payload).then((r) => r.data),
  checkFound: (payload) => apiClient.post("/duplicate-check/found", payload).then((r) => r.data)
};