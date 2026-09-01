import apiClient from "./apiClient";

export const lostItemService = {
  create: (payload) => apiClient.post("/lost-items", payload).then((r) => r.data),
  getById: (id) => apiClient.get(`/lost-items/${id}`).then((r) => r.data),
  update: (id, payload) => apiClient.put(`/lost-items/${id}`, payload).then((r) => r.data),
  withdraw: (id) => apiClient.delete(`/lost-items/${id}`).then((r) => r.data),
  uploadPhoto: (id, file) => {
    const form = new FormData();
    form.append("file", file);
    return apiClient
      .post(`/lost-items/${id}/photos`, form, { headers: { "Content-Type": "multipart/form-data" } })
      .then((r) => r.data);
  }
};
