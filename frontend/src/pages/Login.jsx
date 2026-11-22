import { useState } from "react";
import { api, USE_MOCK } from "../api/index.js";
import { Field, human, useAsync } from "../components/ui.jsx";

// Two separate sign-ins: staff (email + password) and applicants (email only).
export default function Login({ onLogin, onApplicantLogin, initialMode = "staff" }) {
  const [mode, setMode] = useState(initialMode);
  return (
    <div className="login-wrap">
      <div className="login-card">
        <div className="brand">
          <div className="brand-mark">M</div>
          <div>
            <div className="brand-name">Maatram</div>
            <div className="brand-sub">{mode === "staff" ? "Staff sign in" : "Applicant sign in"}</div>
          </div>
        </div>
        <div className="tabs login-tabs" role="tablist">
          <button type="button" role="tab" aria-selected={mode === "applicant"} className={mode === "applicant" ? "on" : ""} onClick={() => setMode("applicant")}>Applicant</button>
          <button type="button" role="tab" aria-selected={mode === "staff"} className={mode === "staff" ? "on" : ""} onClick={() => setMode("staff")}>Staff</button>
        </div>
        {mode === "staff" ? <StaffLogin onLogin={onLogin} /> : <ApplicantLogin onLogin={onApplicantLogin} />}
      </div>
    </div>
  );
}

function StaffLogin({ onLogin }) {
  const [email, setEmail] = useState(USE_MOCK ? "admin@maatram.org" : "");
  const [password, setPassword] = useState(USE_MOCK ? "admin123" : "");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (!email.trim() || !password) { setError("Enter your email and password"); return; }
    setBusy(true);
    try {
      const r = await api.login(email, password);
      onLogin(r.user);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <form onSubmit={submit} noValidate>
      {USE_MOCK && <div className="demo-hint">HackForge MH26-05 prototype with synthetic data, not an official Maatram service. Nothing you enter leaves this browser. Demo login: admin@maatram.org / admin123</div>}
      <Field label="Email">
        <input type="email" value={email} onChange={(e) => { setEmail(e.target.value); setError(""); }} autoComplete="username" />
      </Field>
      <Field label="Password" error={error}>
        <input type="password" value={password} onChange={(e) => { setPassword(e.target.value); setError(""); }} autoComplete="current-password" />
      </Field>
      <button type="submit" className="btn btn-primary" style={{ width: "100%" }} disabled={busy}>
        {busy ? "Signing in…" : "Sign in"}
      </button>
    </form>
  );
}

function ApplicantLogin({ onLogin }) {
  const [email, setEmail] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setError("");
    if (!email.trim()) { setError("Enter your email address"); return; }
    setBusy(true);
    try {
      const r = await api.applicantLookup(email);
      onLogin({ role: "APPLICANT", email: r.email, hasApplied: r.applications.length > 0 });
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy(false);
    }
  };

  return (
    <form onSubmit={submit} noValidate>
      <Field label="Email address" error={error} hint="New applicants fill the form. Returning applicants can track their status.">
        <input type="email" value={email} onChange={(e) => { setEmail(e.target.value); setError(""); }} placeholder="you@example.com" autoComplete="email" autoFocus />
      </Field>
      <button type="submit" className="btn btn-primary" style={{ width: "100%" }} disabled={busy}>
        {busy ? "Signing in…" : "Continue"}
      </button>
      <p className="muted" style={{ fontSize: 12, marginBottom: 0 }}>Prototype: the real portal sends a one-time code to this email before signing in.</p>
      {USE_MOCK && <DemoEmails onPick={(v) => { setEmail(v); setError(""); }} />}
    </form>
  );
}

// Mock mode only: quick-fill sample applicants in different states.
function DemoEmails({ onPick }) {
  const list = useAsync(() => api.listApplications({ page: 1, pageSize: 400 }), []);
  if (!list.data) return null;
  const pick = (s) => list.data.items.find((a) => a.status === s);
  const samples = ["SUBMITTED", "SHORTLISTED", "SELECTED", "REJECTED"].map(pick).filter(Boolean);
  return (
    <div className="result-box">
      <strong>Try a demo applicant:</strong>
      <div className="row gap-sm wrap" style={{ marginTop: 6 }}>
        {samples.map((a) => <button key={a.id} type="button" className="btn" onClick={() => onPick(a.email)}>{human(a.status)}</button>)}
        <button type="button" className="btn" onClick={() => onPick(`new.applicant${Date.now() % 10000}@example.org`)}>New applicant</button>
      </div>
    </div>
  );
}
