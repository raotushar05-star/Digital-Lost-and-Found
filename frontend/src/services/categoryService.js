import apiClient from "./apiClient";

export const categoryService = {
  getCategories: () => apiClient.get("/categories").then((r) => r.data),
  create: (payload) => apiClient.post("/admin/categories", payload).then((r) => r.data),
  update: (id, payload) => apiClient.put(`/admin/categories/${id}`, payload).then((r) => r.data)
};
