import { useState } from "react";
import { api } from "../api/index.js";
import { Link } from "../router.jsx";
import { Badge, Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, Table, fmtDate, human, useAsync, useToast } from "../components/ui.jsx";

export default function AlumniDetail({ params }) {
  const a = useAsync(() => api.getAlumni(params.id), [params.id]);
  const [adding, setAdding] = useState(false);
  const toast = useToast();
  if (a.loading && !a.data) return <Loading />;
  if (a.error) return <ErrorBox error={a.error} onRetry={a.reload} />;
  const al = a.data;
  const current = al.careers.find((c) => c.is_current);

  return (
    <>
      <PageHeader
        title={`${al.person.first_name} ${al.person.last_name}`}
        subtitle={`${al.department} · Batch of ${al.graduation_year}`}
        actions={<><Link to="/alumni" className="btn">All alumni</Link><Link to={`/persons/${al.person_id}`} className="btn">Full timeline</Link><Button variant="primary" onClick={() => setAdding(true)}>Add job</Button></>}
      />
      <div className="stats">
        <div className="stat"><span className="stat-label">Current role</span><span className="stat-value" style={{ fontSize: 18 }}>{current?.job_title || "-"}</span><span className="stat-hint">{current?.company_name}</span></div>
        <div className="stat"><span className="stat-label">Jobs recorded</span><span className="stat-value">{al.careers.length}</span></div>
        <div className="stat"><span className="stat-label">Status</span><span className="stat-value" style={{ fontSize: 18 }}><Badge value={al.alumni_status} /></span></div>
      </div>
      <Card title="Career history" actions={<span className="muted" style={{ fontSize: 12 }}>Current profile = latest entry marked current</span>}>
        <Table
          rows={al.careers}
          columns={[
            { key: "job_title", label: "Role", render: (c) => <strong>{c.job_title}</strong> },
            { key: "company_name", label: "Company", render: (c) => <>{c.company_name}<div className="muted" style={{ fontSize: 12 }}>matched as “{c.canonical_name}”</div></> },
            { key: "employment_type", label: "Type", render: (c) => human(c.employment_type) },
            { key: "dates", label: "Dates", render: (c) => `${fmtDate(c.start_date)} → ${c.end_date ? fmtDate(c.end_date) : "present"}` },
            { key: "source", label: "Source", render: (c) => <Badge value={c.source} /> },
            { key: "is_current", label: "", render: (c) => c.is_current && <Badge value="ACTIVE" /> },
          ]}
        />
      </Card>
      {adding && <AddJob alumniId={al.id} onClose={() => setAdding(false)} onSaved={() => { setAdding(false); a.reload(); toast("Job added"); }} />}
    </>
  );
}

function AddJob({ alumniId, onClose, onSaved }) {
  const [f, setF] = useState({ company_name: "", job_title: "", employment_type: "FULL_TIME", start_date: "", end_date: "", is_current: true, source: "SELF_REPORTED" });
  const [errors, setErrors] = useState({});
  const [preview, setPreview] = useState("");
  const set = (k) => (e) => {
    const v = e.target.type === "checkbox" ? e.target.checked : e.target.value;
    setF({ ...f, [k]: v });
    setErrors({ ...errors, [k]: "" });
    if (k === "company_name") api.normalizePreview(v).then(setPreview);
  };
  const save = async () => {
    const er = {};
    if (!f.company_name.trim()) er.company_name = "Enter the company";
    if (!f.job_title.trim()) er.job_title = "Enter the role";
    if (!f.start_date) er.start_date = "Pick a start date";
    if (!f.is_current && f.end_date && f.end_date < f.start_date) er.end_date = "End date is before start date";
    setErrors(er);
    if (Object.keys(er).length) return;
    try { await api.addCareerEntry(alumniId, { ...f, end_date: f.is_current ? null : f.end_date }); onSaved(); } catch (e) { setErrors({ company_name: e.message }); }
  };
  return (
    <Modal title="Add a job" onClose={onClose} footer={<><Button onClick={onClose}>Cancel</Button><Button variant="primary" onClick={save}>Save</Button></>}>
      <Field label="Company" error={errors.company_name} hint={preview ? `Will be matched as “${preview}”` : "Any spelling is fine, it gets normalised"}>
        <input value={f.company_name} onChange={set("company_name")} />
      </Field>
      <Field label="Role" error={errors.job_title}><input value={f.job_title} onChange={set("job_title")} /></Field>
      <div className="grid grid-2" style={{ gap: 12 }}>
        <Field label="Type">
          <select value={f.employment_type} onChange={set("employment_type")}><option value="FULL_TIME">Full time</option><option value="INTERNSHIP">Internship</option><option value="CONTRACT">Contract</option></select>
        </Field>
        <Field label="Source">
          <select value={f.source} onChange={set("source")}><option value="SELF_REPORTED">Self-reported</option><option value="REFERRAL">Referral</option><option value="PLACEMENT">Placement</option></select>
        </Field>
        <Field label="Start date" error={errors.start_date}><input type="date" value={f.start_date} onChange={set("start_date")} /></Field>
        <Field label="End date" error={errors.end_date}><input type="date" value={f.end_date} onChange={set("end_date")} disabled={f.is_current} /></Field>
      </div>
      <label className="row gap-sm"><input type="checkbox" checked={f.is_current} onChange={set("is_current")} /> This is their current job</label>
    </Modal>
  );
}
