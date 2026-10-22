import apiClient from "./apiClient";

export const adminService = {
  getAuditLogs: (params) => apiClient.get("/admin/audit-logs", { params }).then((r) => r.data),
  getUsers: (params) => apiClient.get("/admin/users", { params }).then((r) => r.data),
  setUserActive: (id, active) => apiClient.patch(`/admin/users/${id}/active`, { active }).then((r) => r.data),
  unlockUser: (id) => apiClient.patch(`/admin/users/${id}/unlock`).then((r) => r.data)
};