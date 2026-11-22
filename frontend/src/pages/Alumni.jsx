import { useState } from "react";
import { api } from "../api/index.js";
import { navigate } from "../router.jsx";
import { Badge, Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, Table, useAsync, useToast } from "../components/ui.jsx";

export default function Alumni() {
  const [f, setF] = useState({ search: "", company: "", graduation_year: "" });
  const [typed, setTyped] = useState("");
  const [adding, setAdding] = useState(false);
  const toast = useToast();
  const list = useAsync(() => api.listAlumni(f), [JSON.stringify(f)]);
  const companies = useAsync(() => api.listCompanies(), []);
  const set = (k) => (e) => setF({ ...f, [k]: e.target.value });
  const all = useAsync(() => api.listAlumni({}), []);
  const years = [...new Set((all.data || []).map((a) => a.graduation_year))];

  return (
    <>
      <PageHeader title="Alumni & careers" subtitle="Ask “who works at …?”. Different spellings of the same company are matched by the merge rules." actions={<Button variant="primary" onClick={() => setAdding(true)}>Add alumni</Button>} />
      <Card>
        <form className="filters" onSubmit={(e) => { e.preventDefault(); setF({ ...f, company: typed }); }}>
          <input type="search" placeholder="Name or department" value={f.search} onChange={set("search")} aria-label="Search alumni" />
          <input list="company-list" placeholder="Works at… (try “TCS” or “infosys ltd”)" value={typed} onChange={(e) => setTyped(e.target.value)} aria-label="Company" style={{ minWidth: 260 }} />
          <datalist id="company-list">{companies.data?.map((c) => <option key={c.normalized_name} value={c.variants[0].company_name} />)}</datalist>
          <button className="btn btn-primary" type="submit">Find</button>
          {f.company && <button className="btn" type="button" onClick={() => { setTyped(""); setF({ ...f, company: "" }); }}>Clear company</button>}
          <select value={f.graduation_year} onChange={set("graduation_year")} aria-label="Graduation year">
            <option value="">Any year</option>
            {years.sort().map((y) => <option key={y}>{y}</option>)}
          </select>
        </form>
        {f.company && list.data && (
          <div className="result-box" style={{ marginTop: 0, marginBottom: 12 }}>
            <strong>{list.data.length}</strong> alumni currently work at <strong>{f.company}</strong> (matched as “{list.data[0]?.canonical_company || "no match"}”).
          </div>
        )}
        {list.loading && !list.data ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={list.reload} /> : (
          <Table
            rows={list.data}
            onRowClick={(a) => navigate(`/alumni/${a.id}`)}
            empty="No alumni match"
            columns={[
              { key: "student_number", label: "Student no.", render: (a) => <span className="mono">{a.student_number}</span> },
              { key: "name", label: "Name", render: (a) => <strong>{a.name}</strong> },
              { key: "department", label: "Department" },
              { key: "graduation_year", label: "Batch" },
              { key: "current_title", label: "Current role" },
              { key: "current_company", label: "Company (as entered)" },
              { key: "alumni_status", label: "Status", render: (a) => <Badge value={a.alumni_status} /> },
            ]}
          />
        )}
      </Card>
      {adding && <AddAlumni onClose={() => setAdding(false)} onSaved={(a) => { setAdding(false); toast("Alumnus added"); navigate(`/alumni/${a.id}`); }} />}
    </>
  );
}

function AddAlumni({ onClose, onSaved }) {
  const [q, setQ] = useState("");
  const [student, setStudent] = useState(null);
  const [f, setF] = useState({ graduation_year: String(new Date().getFullYear()), company_name: "", job_title: "", source: "SELF_REPORTED" });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const found = useAsync(() => api.listStudentsForAlumni(q), [q]);
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setErrors({ ...errors, [k]: "", server: "" }); };

  const save = async () => {
    const er = {};
    if (!student) er.student = "Pick the student this alumnus was";
    const y = Number(f.graduation_year);
    if (!y || y < 2000 || y > new Date().getFullYear() + 1) er.graduation_year = "Enter a valid year";
    if (f.company_name.trim() && !f.job_title.trim()) er.job_title = "Add the role, or leave the company empty";
    if (f.job_title.trim() && !f.company_name.trim()) er.company_name = "Add the company, or leave the role empty";
    setErrors(er);
    if (Object.keys(er).length) return;
    setBusy(true);
    try { onSaved(await api.addAlumniFromStudent({ student_id: student.id, ...f })); }
    catch (x) { setErrors({ server: x.message }); }
    finally { setBusy(false); }
  };

  return (
    <Modal title="Add alumni from a student record" onClose={onClose} footer={<><Button onClick={onClose}>Cancel</Button><Button variant="primary" disabled={busy} onClick={save}>{busy ? "Saving…" : "Add alumni"}</Button></>}>
      <p className="muted" style={{ marginTop: 0, fontSize: 13 }}>Every alumnus must reference an existing student, so their history stays on one person record.</p>
      {!student ? (
        <div className="field">
          <span className="field-label">Reference student</span>
          <input type="search" placeholder="Search by name or student number" value={q} onChange={(e) => setQ(e.target.value)} autoFocus aria-label="Search students" />
          {errors.student && <span className="field-error">{errors.student}</span>}
          <div className="pick-list">
            {found.data?.length === 0 && <div className="empty" style={{ padding: 12 }}>No matching students who aren’t alumni yet</div>}
            {found.data?.map((s) => (
              <button key={s.id} type="button" className="pick" onClick={() => { setStudent(s); setErrors({}); }}>
                <span><strong>{s.name}</strong><br /><span className="muted mono">{s.student_number}</span></span>
                <span className="muted">{s.department} · admitted {s.admission_date.slice(0, 4)}</span>
              </button>
            ))}
          </div>
        </div>
      ) : (
        <div className="result-box" style={{ marginTop: 0, marginBottom: 12, display: "flex", alignItems: "center", gap: 10 }}>
          <span><strong>{student.name}</strong> · <span className="mono">{student.student_number}</span> · {student.department}</span>
          <span className="spacer" />
          <Button onClick={() => setStudent(null)}>Change</Button>
        </div>
      )}
      <div className="grid grid-2" style={{ gap: 12 }}>
        <Field label="Graduation year" error={errors.graduation_year}><input inputMode="numeric" value={f.graduation_year} onChange={set("graduation_year")} /></Field>
        <Field label="Source">
          <select value={f.source} onChange={set("source")}><option value="SELF_REPORTED">Self-reported</option><option value="REFERRAL">Referral</option><option value="PLACEMENT">Placement</option></select>
        </Field>
        <Field label="Current company (optional)" error={errors.company_name}><input value={f.company_name} onChange={set("company_name")} /></Field>
        <Field label="Role (optional)" error={errors.job_title}><input value={f.job_title} onChange={set("job_title")} /></Field>
      </div>
      {errors.server && <div className="field-error">{errors.server}</div>}
    </Modal>
  );
}
