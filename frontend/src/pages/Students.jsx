import { useState } from "react";
import { api } from "../api/index.js";
import { navigate } from "../router.jsx";
import { Badge, Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, Pagination, Table, fmtDate, useAsync, useToast } from "../components/ui.jsx";

// Selected applicants land here as ACTIVE students. When they pass out, staff mark them
// and they move to Alumni with the same person, student number and department.
export default function Students() {
  const [f, setF] = useState({ search: "", enrollment_status: "ACTIVE", department: "" });
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState(new Set());
  const [passing, setPassing] = useState(false);
  const toast = useToast();
  const meta = useAsync(() => api.meta(), []);
  const list = useAsync(() => api.listStudents({ ...f, page, pageSize: 20 }), [JSON.stringify(f), page]);
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setPage(1); setSelected(new Set()); };
  const canSelect = f.enrollment_status === "ACTIVE";

  const toggle = (id) => setSelected((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });
  const toggleAll = (rows, on) => setSelected((s) => { const n = new Set(s); rows.forEach((r) => (on ? n.add(r.id) : n.delete(r.id))); return n; });

  return (
    <>
      <PageHeader
        title={f.enrollment_status === "ACTIVE" ? "Active students" : "Students"}
        subtitle="Applicants appear here automatically once they are selected. When a student passes out, mark them and they move to Alumni with the same details."
      />
      <Card>
        <div className="filters">
          <input type="search" placeholder="Name or student number" value={f.search} onChange={set("search")} aria-label="Search students" />
          <select value={f.enrollment_status} onChange={set("enrollment_status")} aria-label="Enrollment status">
            <option value="ACTIVE">Active</option>
            <option value="GRADUATED">Passed out</option>
            <option value="">All statuses</option>
          </select>
          <select value={f.department} onChange={set("department")} aria-label="Department">
            <option value="">All departments</option>
            {meta.data?.departments.map((d) => <option key={d}>{d}</option>)}
          </select>
        </div>
        {list.loading && !list.data ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={list.reload} /> : (
          <>
            <Table
              rows={list.data.items}
              onRowClick={(s) => navigate(`/persons/${s.person_id}`)}
              empty="No students match"
              selectable={canSelect} selected={selected} onToggle={toggle} onToggleAll={toggleAll}
              columns={[
                { key: "student_number", label: "Student no.", render: (s) => <span className="mono">{s.student_number}</span> },
                { key: "name", label: "Name", render: (s) => <strong>{s.name}</strong> },
                { key: "department", label: "Department" },
                { key: "admission_date", label: "Admitted", render: (s) => fmtDate(s.admission_date) },
                { key: "attended", label: "Events attended", align: "right" },
                { key: "enrollment_status", label: "Status", render: (s) => <Badge value={s.enrollment_status} /> },
              ]}
            />
            <Pagination page={page} pageSize={20} total={list.data.total} onPage={setPage} />
          </>
        )}
        {canSelect && selected.size > 0 && (
          <div className="bulk-bar">
            <strong>{selected.size} selected</strong>
            <div className="spacer" />
            <Button onClick={() => setSelected(new Set())}>Clear</Button>
            <Button variant="brand" onClick={() => setPassing(true)}>Mark as passed out → Alumni</Button>
          </div>
        )}
      </Card>
      {passing && (
        <PassOutModal
          ids={[...selected]}
          onClose={() => setPassing(false)}
          onDone={(r) => {
            setPassing(false);
            setSelected(new Set());
            list.reload();
            toast(`${r.moved} moved to alumni${r.skipped ? `, ${r.skipped} already alumni` : ""}${r.errors.length ? `, ${r.errors.length} failed` : ""}`, r.errors.length ? "error" : "ok");
          }}
        />
      )}
    </>
  );
}

function PassOutModal({ ids, onClose, onDone }) {
  const [year, setYear] = useState(String(new Date().getFullYear()));
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);
  const save = async () => {
    const y = Number(year);
    if (!y || y < 2000 || y > new Date().getFullYear() + 1) { setErr("Enter a valid year"); return; }
    setBusy(true);
    try { onDone(await api.graduateStudents({ ids, graduation_year: y })); }
    catch (x) { setErr(x.message); }
    finally { setBusy(false); }
  };
  return (
    <Modal title="Mark as passed out" onClose={onClose} footer={<><Button onClick={onClose}>Cancel</Button><Button variant="primary" disabled={busy} onClick={save}>{busy ? "Moving…" : `Move ${ids.length} to alumni`}</Button></>}>
      <p className="muted" style={{ marginTop: 0, fontSize: 13 }}>
        {ids.length} student{ids.length > 1 ? "s" : ""} will leave the active list and be added to Alumni with the same name, student number and department.
      </p>
      <Field label="Pass-out year" error={err}>
        <input inputMode="numeric" value={year} onChange={(e) => { setYear(e.target.value); setErr(""); }} autoFocus />
      </Field>
    </Modal>
  );
}
