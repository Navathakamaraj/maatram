import { useState } from "react";
import { api } from "../api/index.js";
import { navigate } from "../router.jsx";
import { Badge, Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, Table, fmtDate, useAsync, useToast } from "../components/ui.jsx";
import { CycleBars } from "./Dashboard.jsx";

export default function Cycles() {
  const cycles = useAsync(() => api.listCycles(), []);
  const cmp = useAsync(() => api.compareCycles(), []);
  const [creating, setCreating] = useState(false);
  const toast = useToast();

  const setStatus = async (c, status) => {
    try {
      await api.updateCycleStatus(c.id, status);
      toast(`${c.cycle_name} is now ${status.toLowerCase()}`);
      cycles.reload(); cmp.reload();
    } catch (e) { toast(e.message, "error"); }
  };

  const stats = Object.fromEntries((cmp.data || []).map((r) => [r.cycle_id, r]));

  return (
    <>
      <PageHeader
        title="Admission cycles"
        subtitle="Each academic year is its own cycle. Only one can be open at a time."
        actions={<Button variant="primary" onClick={() => setCreating(true)}>New cycle</Button>}
      />
      <Card>
        {cycles.loading ? <Loading /> : cycles.error ? <ErrorBox error={cycles.error} onRetry={cycles.reload} /> : (
          <Table
            rows={cycles.data}
            onRowClick={(c) => navigate(`/applications?cycle_id=${c.id}`)}
            columns={[
              { key: "cycle_name", label: "Cycle", render: (c) => <strong>{c.cycle_name}</strong> },
              { key: "academic_year", label: "Academic year" },
              { key: "dates", label: "Window", render: (c) => `${fmtDate(c.start_date)} → ${fmtDate(c.end_date)}` },
              { key: "status", label: "Status", render: (c) => <Badge value={c.status} /> },
              { key: "apps", label: "Applications", align: "right", render: (c) => stats[c.id]?.total ?? "-" },
              { key: "sel", label: "Selected", align: "right", render: (c) => stats[c.id]?.selected ?? "-" },
              {
                key: "actions", label: "", align: "right",
                render: (c) => (
                  <span onClick={(e) => e.stopPropagation()}>
                    {c.status !== "OPEN" && <Button onClick={() => setStatus(c, "OPEN")}>Open</Button>}
                    {c.status === "OPEN" && <Button variant="danger" onClick={() => setStatus(c, "CLOSED")}>Close</Button>}
                  </span>
                ),
              },
            ]}
          />
        )}
      </Card>
      <Card title="Year-over-year comparison">
        {cmp.data ? (
          <>
            <CycleBars rows={cmp.data} />
            <div style={{ marginTop: 16 }}>
              <Table
                rowKey="cycle_id"
                rows={cmp.data}
                columns={[
                  { key: "academic_year", label: "Year" },
                  { key: "total", label: "Applications", align: "right" },
                  { key: "submitted", label: "Submitted", align: "right" },
                  { key: "shortlisted", label: "Shortlisted", align: "right" },
                  { key: "selected", label: "Selected", align: "right" },
                  { key: "rejected", label: "Rejected", align: "right" },
                  { key: "selection_rate", label: "Selection rate", align: "right", render: (r) => `${r.selection_rate}%` },
                ]}
              />
            </div>
          </>
        ) : <Loading />}
      </Card>
      {creating && <NewCycle onClose={() => setCreating(false)} onSaved={() => { setCreating(false); cycles.reload(); cmp.reload(); toast("Cycle created"); }} />}
    </>
  );
}

function NewCycle({ onClose, onSaved }) {
  const [f, setF] = useState({ cycle_name: "", academic_year: "", start_date: "", end_date: "" });
  const [errors, setErrors] = useState({});
  const [serverError, setServerError] = useState("");
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setErrors({ ...errors, [k]: "" }); setServerError(""); };

  const save = async () => {
    const er = {};
    if (!f.cycle_name.trim()) er.cycle_name = "Enter a name";
    if (!/^\d{4}-\d{2}$/.test(f.academic_year)) er.academic_year = "Use the format 2027-28";
    if (!f.start_date) er.start_date = "Pick a start date";
    if (!f.end_date) er.end_date = "Pick an end date";
    else if (f.start_date && f.end_date <= f.start_date) er.end_date = "Must be after the start date";
    setErrors(er);
    if (Object.keys(er).length) return;
    try { await api.createCycle(f); onSaved(); } catch (e) { setServerError(e.message); }
  };

  return (
    <Modal title="New admission cycle" onClose={onClose} footer={<><Button onClick={onClose}>Cancel</Button><Button variant="primary" onClick={save}>Create cycle</Button></>}>
      <Field label="Cycle name" error={errors.cycle_name}><input value={f.cycle_name} onChange={set("cycle_name")} placeholder="2027-28 Intake" /></Field>
      <Field label="Academic year" error={errors.academic_year}><input value={f.academic_year} onChange={set("academic_year")} placeholder="2027-28" /></Field>
      <div className="grid grid-2" style={{ gap: 12 }}>
        <Field label="Applications open" error={errors.start_date}><input type="date" value={f.start_date} onChange={set("start_date")} /></Field>
        <Field label="Applications close" error={errors.end_date}><input type="date" value={f.end_date} onChange={set("end_date")} /></Field>
      </div>
      {serverError && <div className="field-error">{serverError}</div>}
    </Modal>
  );
}
