import { httpApi } from "./http.js";

export const USE_MOCK = false;

export const TRANSITIONS = {
  DRAFT: [],
  SUBMITTED: ["UNDER_REVIEW", "SHORTLISTED", "REJECTED"],
  UNDER_REVIEW: ["SHORTLISTED", "REJECTED"],
  SHORTLISTED: ["SELECTED", "REJECTED"],
  SELECTED: [],
  REJECTED: [],
};

export const api = httpApi;
export default api;
