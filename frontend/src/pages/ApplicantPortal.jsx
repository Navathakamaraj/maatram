import { useState } from "react";
import { api } from "../api/index.js";
import { Badge, Button, Card, ErrorBox, Field, Loading, fmtDate, fmtDateTime, human, useAsync, useToast } from "../components/ui.jsx";

// Applicant-only area (separate sign-in from staff): two tabs, the application form and the tracker.
const TRACK = ["SUBMITTED", "UNDER_REVIEW", "SHORTLISTED", "DECISION"];
const EMPTY = { first_name: "", last_name: "", phone: "", date_of_birth: "", gender: "", district: "", school: "", hsc_marks: "", family_income: "", preferred_course: "" };

export default function ApplicantPortal({ applicant, onLogout }) {
  const account = useAsync(() => api.applicantLookup(applicant.email), [applicant.email]);
  const [tab, setTab] = useState(applicant.hasApplied ? "tracker" : "form");
  const [done, setDone] = useState(null);

  return (
    <div className="applicant-shell">
      <header className="applicant-top">
        <div className="brand" style={{ padding: 0 }}>
          <div className="brand-mark">M</div>
          <div>
            <div className="brand-name" style={{ color: "var(--ink)" }}>Maatram</div>
            <div className="brand-sub">Applicant portal</div>
          </div>
        </div>
        <div className="spacer" />
        <span className="muted">{applicant.email}</span>
        <button className="btn" onClick={onLogout}>Sign out</button>
      </header>

      <main className="applicant-main">
        <div className="tabs" role="tablist">
          <button role="tab" aria-selected={tab === "form"} className={tab === "form" ? "on" : ""} onClick={() => setTab("form")}>Application form</button>
          <button role="tab" aria-selected={tab === "tracker"} className={tab === "tracker" ? "on" : ""} onClick={() => setTab("tracker")}>Application tracker</button>
        </div>

        {account.loading && !account.data ? <Loading /> : account.error ? <ErrorBox error={account.error} onRetry={account.reload} /> : (
          <>
            {tab === "form" && (
              done ? (
                <Card>
                  <div className="done-box">
                    <div className="muted">Application submitted. Your unique application ID is</div>
                    <div className="app-id">{done.application_number}</div>
                    <p className="muted" style={{ marginTop: 0 }}>Keep this ID. You can check your status any time in the tracker.</p>
                    <Button variant="primary" onClick={() => { setDone(null); setTab("tracker"); }}>Open application tracker</Button>
                  </div>
                </Card>
              ) : (
                <FormTab account={account.data} onDone={(r) => { setDone(r); account.reload(); }} onTrack={() => setTab("tracker")} />
              )
            )}
            {tab === "tracker" && <TrackerTab account={account.data} onApply={() => setTab("form")} />}
          </>
        )}
      </main>
    </div>
  );
}

function FormTab({ account, onDone, onTrack }) {
  if (account.canApply) return <ApplyForm account={account} onDone={onDone} />;
  const current = account.openCycle && account.applications.find((a) => a.cycle_id === account.openCycle.id);
  return (
    <Card title="Application form">
      <div className="empty">
        {current
          ? <>You have already applied for {account.openCycle.cycle_name} (ID <span className="mono">{current.application_number}</span>).</>
          : "Applications are closed right now. Please check back when the next cycle opens."}
      </div>
      {current && <div style={{ textAlign: "center" }}><Button variant="primary" onClick={onTrack}>See my status</Button></div>}
    </Card>
  );
}

function TrackerTab({ account, onApply }) {
  const name = account.person ? `${account.person.first_name} ${account.person.last_name}` : account.email;
  const st = account.student;
  return (
    <Card title={`Hello, ${name}`}>
      {st && (
        <div className="result-box" style={{ marginTop: 0, marginBottom: 12 }}>
          {st.enrollment_status === "ACTIVE"
            ? <>You are now an <strong>active Maatram student</strong> · student no. <span className="mono">{st.student_number}</span> · {st.department}</>
            : <>You have passed out and are part of the <strong>Maatram alumni</strong> network · student no. <span className="mono">{st.student_number}</span></>}
        </div>
      )}
      {account.applications.length === 0 ? (
        <div className="empty">
          You have not applied yet.
          {account.canApply && <div style={{ marginTop: 10 }}><Button variant="brand" onClick={onApply}>Fill the application form</Button></div>}
        </div>
      ) : account.applications.map((a) => <AppStatus key={a.id} app={a} />)}
    </Card>
  );
}

function AppStatus({ app }) {
  const final = ["SELECTED", "REJECTED"].includes(app.status);
  const reached = (s) => {
    if (s === "DECISION") return final;
    const order = ["SUBMITTED", "UNDER_REVIEW", "SHORTLISTED"];
    const cur = final ? (app.steps.some((x) => x.status === "SHORTLISTED") ? 2 : app.steps.some((x) => x.status === "UNDER_REVIEW") ? 1 : 0) : order.indexOf(app.status);
    return order.indexOf(s) <= cur;
  };
  const rejection = app.steps.find((s) => s.status === "REJECTED");
  const label = (s) => (s === "DECISION" ? (final ? human(app.status) : "Decision") : human(s));
  return (
    <div className="app-status">
      <div className="row gap wrap">
        <div>
          <div className="muted" style={{ fontSize: 12 }}>Application ID</div>
          <div className="app-id small">{app.application_number}</div>
        </div>
        <div className="spacer" />
        <div style={{ textAlign: "right" }}>
          <div className="muted" style={{ fontSize: 12 }}>{app.cycle_name} · submitted {fmtDate(app.submitted_at)}</div>
          <Badge value={app.status} />
        </div>
      </div>
      <ol className="tracker" aria-label="Application progress">
        {TRACK.map((s) => (
          <li key={s} className={`${reached(s) ? "on" : ""} ${s === "DECISION" && app.status === "REJECTED" ? "bad" : ""}`}>
            <span className="dot" />{label(s)}
          </li>
        ))}
      </ol>
      <ul className="timeline" style={{ marginTop: 12 }}>
        {app.steps.map((s, i) => (
          <li key={i} className="t-application">
            <div className="t-title">{human(s.status)}</div>
            <div className="t-meta">{fmtDateTime(s.at)}</div>
          </li>
        ))}
      </ul>
      {rejection?.reason && <div className="t-reason">Note from Maatram: {rejection.reason}</div>}
    </div>
  );
}

function ApplyForm({ account, onDone }) {
  const meta = useAsync(() => api.meta(), []);
  const [f, setF] = useState(EMPTY);
  const [errors, setErrors] = useState({});
  const [serverErr, setServerErr] = useState("");
  const [busy, setBusy] = useState(false);
  const toast = useToast();
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setErrors({ ...errors, [k]: "" }); setServerErr(""); };

  const submit = async (e) => {
    e.preventDefault();
    const er = {};
    if (!f.first_name.trim()) er.first_name = "Required";
    if (!f.last_name.trim()) er.last_name = "Required";
    if (!/^[6-9]\d{9}$/.test(f.phone)) er.phone = "Enter a 10-digit mobile number";
    if (!f.date_of_birth) er.date_of_birth = "Required";
    if (!f.district) er.district = "Choose your district";
    if (!f.school.trim()) er.school = "Required";
    if (f.hsc_marks === "" || +f.hsc_marks < 0 || +f.hsc_marks > 100) er.hsc_marks = "Enter a percentage from 0 to 100";
    if (f.family_income === "" || +f.family_income < 0) er.family_income = "Enter yearly family income in rupees";
    if (!f.preferred_course) er.preferred_course = "Choose a course";
    setErrors(er);
    if (Object.keys(er).length) { setServerErr("Please fix the highlighted fields."); return; }
    setBusy(true);
    try {
      const r = await api.applicantSubmit(account.email, f);
      toast(`Submitted: ${r.application_number}`);
      onDone(r);
    } catch (x) { setServerErr(x.message); }
    finally { setBusy(false); }
  };

  return (
    <Card title={`Application form · ${account.openCycle?.cycle_name || ""}`}>
      <form onSubmit={submit} noValidate>
        <div className="form-grid">
          <Field label="First name" error={errors.first_name}><input value={f.first_name} onChange={set("first_name")} /></Field>
          <Field label="Last name" error={errors.last_name}><input value={f.last_name} onChange={set("last_name")} /></Field>
          <Field label="Mobile number" error={errors.phone}><input inputMode="numeric" maxLength={10} value={f.phone} onChange={set("phone")} placeholder="9876543210" /></Field>
          <Field label="Date of birth" error={errors.date_of_birth}><input type="date" value={f.date_of_birth} onChange={set("date_of_birth")} /></Field>
          <Field label="Gender">
            <select value={f.gender} onChange={set("gender")}><option value="">Prefer not to say</option><option>Female</option><option>Male</option><option>Other</option></select>
          </Field>
          <Field label="District" error={errors.district}>
            <select value={f.district} onChange={set("district")}><option value="">Choose…</option>{meta.data?.districts.map((d) => <option key={d}>{d}</option>)}</select>
          </Field>
          <Field label="School" error={errors.school}><input value={f.school} onChange={set("school")} placeholder="Govt. Hr. Sec. School, …" /></Field>
          <Field label="HSC marks (%)" error={errors.hsc_marks}><input type="number" min="0" max="100" value={f.hsc_marks} onChange={set("hsc_marks")} /></Field>
          <Field label="Yearly family income (₹)" error={errors.family_income}><input type="number" min="0" value={f.family_income} onChange={set("family_income")} /></Field>
          <Field label="Preferred course" error={errors.preferred_course}>
            <select value={f.preferred_course} onChange={set("preferred_course")}><option value="">Choose…</option>{meta.data?.departments.map((d) => <option key={d}>{d}</option>)}</select>
          </Field>
        </div>
        {serverErr && <div className="field-error" style={{ marginBottom: 10 }}>{serverErr}</div>}
        <div className="row gap-sm" style={{ justifyContent: "flex-end" }}>
          <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? "Submitting…" : "Submit application"}</button>
        </div>
      </form>
    </Card>
  );
}
