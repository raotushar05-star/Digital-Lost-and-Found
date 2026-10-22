import apiClient from "./apiClient";

export const authService = {
  register: (payload) => apiClient.post("/auth/register", payload).then((r) => r.data),
  login: (payload) => apiClient.post("/auth/login", payload).then((r) => r.data),
  logout: () => apiClient.post("/auth/logout").then((r) => r.data),
  forgotPassword: (email) => apiClient.post("/auth/forgot-password", { email }).then((r) => r.data),
  resetPassword: (token, newPassword) =>
    apiClient.post("/auth/reset-password", { token, newPassword }).then((r) => r.data),
  changePassword: (currentPassword, newPassword) =>
    apiClient.post("/auth/change-password", { currentPassword, newPassword }).then((r) => r.data)
};