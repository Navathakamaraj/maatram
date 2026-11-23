import { useEffect, useState } from "react";
import { api, USE_MOCK } from "./api/index.js";
import { useHashPath, matchRoute, navigate, Link } from "./router.jsx";
import { ToastProvider } from "./components/ui.jsx";
import Login from "./pages/Login.jsx";
import Dashboard from "./pages/Dashboard.jsx";
import Cycles from "./pages/Cycles.jsx";
import Applications from "./pages/Applications.jsx";
import ApplicationDetail from "./pages/ApplicationDetail.jsx";
import Students from "./pages/Students.jsx";
import CollegeEvents from "./pages/CollegeEvents.jsx";
import Attendance from "./pages/Attendance.jsx";
import ApplicantPortal from "./pages/ApplicantPortal.jsx";
import RejectedApplications from "./pages/RejectedApplications.jsx";
import Alumni from "./pages/Alumni.jsx";
import AlumniDetail from "./pages/AlumniDetail.jsx";
import Companies from "./pages/Companies.jsx";
import Persons from "./pages/Persons.jsx";
import PersonTimeline from "./pages/PersonTimeline.jsx";
import Documents from "./pages/Documents.jsx";

// path pattern -> page. Order matters (specific before general).
const ROUTES = [
  ["/", Dashboard],
  ["/cycles", Cycles],
  ["/applications", Applications],
  ["/applications/:id", ApplicationDetail],
  ["/rejected", RejectedApplications],
  ["/students", Students],
  ["/events", CollegeEvents],
  ["/events/:id/attendance", Attendance],
  ["/alumni", Alumni],
  ["/alumni/:id", AlumniDetail],
  ["/companies", Companies],
  ["/persons", Persons],
  ["/persons/:id", PersonTimeline],
  ["/documents", Documents],
];

const NAV = [
  ["Overview", [["/", "Dashboard"], ["/persons", "People search"]]],
  ["Admissions", [["/cycles", "Admission cycles"], ["/applications", "Applications"], ["/rejected", "Rejected applications", "rejected"], ["/documents", "Documents"]]],
  ["Students", [["/students", "Active students"], ["/events", "Events & attendance"]]],
  ["Alumni", [["/alumni", "Alumni & careers"], ["/companies", "Companies"]]],
];

export default function App() {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(sessionStorage.getItem("maatram-user")); } catch { return null; }
  });
  const login = (u) => {
    try { sessionStorage.setItem("maatram-user", JSON.stringify(u)); } catch { /* ignore */ }
    setUser(u);
  };
  const logout = () => {
    const wasApplicant = user?.role === "APPLICANT";
    try { sessionStorage.removeItem("maatram-user"); } catch { /* ignore */ }
    setUser(null);
    navigate(wasApplicant ? "/apply" : "/");
  };
  // #/apply opens the applicant sign-in by default; staff use the root URL.
  const applicantLink = window.location.hash.startsWith("#/apply");
  return (
    <ToastProvider>
      {!user && <Login key={applicantLink ? "a" : "s"} onLogin={login} onApplicantLogin={login} initialMode={applicantLink ? "applicant" : "staff"} />}
      {user?.role === "APPLICANT" && <ApplicantPortal applicant={user} onLogout={logout} />}
      {user && user.role !== "APPLICANT" && <Shell user={user} onLogout={logout} />}
    </ToastProvider>
  );
}

function Shell({ user, onLogout }) {
  const path = useHashPath();
  const [pending, setPending] = useState(0);
  const [q, setQ] = useState("");

  useEffect(() => {
    api.getDashboard().then((d) => setPending(d.counts.rejectedOpenCycle)).catch(() => {});
  }, [path]);

  let Page = NotFound, params = {};
  for (const [pattern, Comp] of ROUTES) {
    const m = matchRoute(pattern, path);
    if (m) { Page = Comp; params = m; break; }
  }
  const isActive = (to) => {
    const clean = path.split("?")[0];
    if (to === "/") return clean === "/";
    if (to === "/alumni") return clean === "/alumni" || /^\/alumni\/\d+/.test(clean);
    return clean === to || clean.startsWith(to + "/");
  };

  return (
    <div className="shell">
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-mark">M</div>
          <div>
            <div className="brand-name">Maatram</div>
            <div className="brand-sub">Student lifecycle admin</div>
          </div>
        </div>
        <nav className="nav" aria-label="Main">
          {NAV.map(([group, links]) => (
            <div key={group}>
              <div className="nav-group">{group}</div>
              {links.map(([to, label, badge]) => (
                <Link key={to} to={to} className={isActive(to) ? "active" : ""}>
                  {label}
                  {badge === "rejected" && pending > 0 && <span className="count">{pending}</span>}
                </Link>
              ))}
            </div>
          ))}
        </nav>
      </aside>
      <main className="main">
        <div className="topbar">
          <form className="search" onSubmit={(e) => { e.preventDefault(); navigate(`/persons?q=${encodeURIComponent(q)}`); }}>
            <input type="search" placeholder="Search anyone by name, email or phone…" value={q} onChange={(e) => setQ(e.target.value)} style={{ width: "100%" }} aria-label="Search people" />
          </form>
          <div className="spacer" />
          {USE_MOCK && <span className="mock-pill" title="HackForge prototype using built-in synthetic data">Prototype · demo data</span>}
          <div className="user-chip">
            <div className="avatar">{user.name?.[0] || "A"}</div>
            <span>{user.email}</span>
          </div>
          <button className="btn" onClick={onLogout}>Log out</button>
        </div>
        <Page key={path.split("?")[0]} params={params} path={path} />
      </main>
    </div>
  );
}

const NotFound = () => (
  <div className="card">
    <h1>Page not found</h1>
    <p className="muted">That address doesn’t match any admin page. <Link to="/">Go to the dashboard</Link>.</p>
  </div>
);
