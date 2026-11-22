// Real backend client. Same method names as mockApi.js.
// The paths below are the contract the FastAPI team should implement.
const BASE = import.meta.env.VITE_API_URL || "http://localhost:8000";
let token = null;

async function req(method, path, body, query) {
  const url = new URL(BASE + path);
  if (query) Object.entries(query).forEach(([k, v]) => v !== undefined && v !== "" && v !== null && url.searchParams.set(k, v));
  const res = await fetch(url, {
    method,
    headers: { "Content-Type": "application/json", ...(token ? { Authorization: `Bearer ${token}` } : {}) },
    body: body ? JSON.stringify(body) : undefined,
  });
  const data = res.status === 204 ? null : await res.json().catch(() => null);
  if (!res.ok) {
    const err = new Error(data?.detail?.toString() || `Request failed (${res.status})`);
    err.status = res.status;
    throw err;
  }
  return data;
}
const get = (p, q) => req("GET", p, null, q);
const post = (p, b) => req("POST", p, b);
const patch = (p, b) => req("PATCH", p, b);

export const httpApi = {
  resetDemoData: () => post("/admin/dev/reset"),
  login: async (email, password) => {
    const r = await post("/auth/login", { email, password });
    token = r.token;
    return r;
  },
  getDashboard: () => get("/admin/dashboard"),

  listCycles: () => get("/admin/cycles"),
  createCycle: (data) => post("/admin/cycles", data),
  updateCycleStatus: (id, status) => patch(`/admin/cycles/${id}`, { status }),
  compareCycles: () => get("/admin/cycles/compare"),

  listApplications: (q) => get("/admin/applications", q),
  listApplicationIds: (q) => get("/admin/applications/ids", q),
  getApplication: (id) => get(`/admin/applications/${id}`),
  bulkUpdateStatus: (body) => post("/admin/applications/bulk-status", body),

  listStudents: (q) => get("/admin/students", q),
  graduateStudents: (body) => post("/admin/students/graduate", body),

  searchPersons: (q) => get("/admin/persons", { q }),
  getPersonTimeline: (id) => get(`/admin/persons/${id}/timeline`),

  listCollegeEvents: () => get("/admin/college-events"),
  createCollegeEvent: (data) => post("/admin/college-events", data),
  getAttendance: (id) => get(`/admin/college-events/${id}/attendance`),
  markAttendance: (id, marks) => post(`/admin/college-events/${id}/attendance`, { marks }),

  listAlumniRegistrations: (q) => get("/admin/alumni-registrations", q),
  reviewAlumniRegistration: (id, decision, reason) => post(`/admin/alumni-registrations/${id}/review`, { decision, reason }),

  listAlumni: (q) => get("/admin/alumni", q),
  getAlumni: (id) => get(`/admin/alumni/${id}`),
  addCareerEntry: (id, data) => post(`/admin/alumni/${id}/careers`, data),
  listCompanies: () => get("/admin/companies"),
  mergeCompanies: (normalized_name) => post("/admin/companies/merge", { normalized_name }),
  normalizePreview: (name) => get("/admin/companies/normalize", { name }).then((r) => r.normalized_name),

  listDocuments: (q) => get("/admin/documents", q),
  checkUpload: (file) => post("/uploads/presign", { file_name: file.name, content_type: file.type, file_size: file.size }),

  listDomainEvents: (q) => get("/admin/events", q),
  listConsumers: () => get("/admin/events/consumers"),
  simulateLateCommit: () => post("/admin/dev/late-commit"),
  replay: (consumer_name, mode, from_time) => post("/admin/events/replay", { consumer_name, mode, from_time }),

  appendEvent: (body) => post("/admin/events", body),

  listStudentsForAlumni: (q) => get("/admin/alumni/candidates", { q }),
  addAlumniFromStudent: (body) => post("/admin/alumni", body),

  // public applicant portal (no staff token needed)
  applicantLookup: (email) => post("/public/applicant/lookup", { email }),
  applicantSubmit: (email, form) => post("/public/applications", { email, ...form }),

  listRejected: (q) => get("/admin/applications/rejected", q),
  importStatus: (body) => post("/admin/applications/import-status", body),

  meta: () => get("/admin/meta"),
};
