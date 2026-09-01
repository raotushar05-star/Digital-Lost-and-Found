import apiClient from "./apiClient";

export const userService = {
  getProfile: () => apiClient.get("/users/me").then((r) => r.data),
  updateProfile: (payload) => apiClient.put("/users/me", payload).then((r) => r.data),
  getMyLostItems: () => apiClient.get("/users/me/lost-items").then((r) => r.data),
  getMyFoundReports: () => apiClient.get("/users/me/found-reports").then((r) => r.data),
  getMyClaims: () => apiClient.get("/users/me/claims").then((r) => r.data),
  getMyCases: () => apiClient.get("/users/me/cases").then((r) => r.data)
};
