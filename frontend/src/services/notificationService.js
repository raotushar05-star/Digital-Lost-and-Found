import apiClient from "./apiClient";

export const notificationService = {
  getMyNotifications: (page = 0, size = 20) =>
    apiClient.get("/notifications", { params: { page, size } }).then((r) => r.data),
  markRead: (id) => apiClient.patch(`/notifications/${id}/read`).then((r) => r.data),
  markAllRead: () => apiClient.patch("/notifications/read-all").then((r) => r.data)
};
