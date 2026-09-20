import apiClient from "./apiClient";

export const foundReportService = {
  create: (payload, photoFile) => {
    const form = new FormData();
    form.append("request", new Blob([JSON.stringify(payload)], { type: "application/json" }));
    form.append("photo", photoFile);
    return apiClient
      .post("/found-reports", form, { headers: { "Content-Type": "multipart/form-data" } })
      .then((r) => r.data);
  },
  getById: (id) => apiClient.get(`/found-reports/${id}`).then((r) => r.data)
};