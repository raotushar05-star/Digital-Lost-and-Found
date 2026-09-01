import apiClient from "./apiClient";

export const matchService = {
  getMyMatches: () => apiClient.get("/matches/my").then((r) => r.data),
  getMatchesForLostItem: (lostItemId) => apiClient.get(`/lost-items/${lostItemId}/matches`).then((r) => r.data),
  generate: (lostItemId) =>
    apiClient.post("/matches/generate", null, { params: lostItemId ? { lostItemId } : {} }).then((r) => r.data)
};
