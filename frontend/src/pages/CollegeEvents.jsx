import { useState } from "react";
import { api } from "../api/index.js";
import { navigate } from "../router.jsx";
import { Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, Table, fmtDate, useAsync, useToast } from "../components/ui.jsx";

export default function CollegeEvents() {
  const list = useAsync(() => api.listCollegeEvents(), []);
  const [creating, setCreating] = useState(false);
  const toast = useToast();
  return (
    <>
      <PageHeader title="Events & attendance" subtitle="Workshops, meets and camps. Attendance is marked against the student record." actions={<Button variant="primary" onClick={() => setCreating(true)}>New event</Button>} />
      <Card>
        {list.loading ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={list.reload} /> : (
          <Table
            rows={list.data}
            onRowClick={(e) => navigate(`/events/${e.id}/attendance`)}
            columns={[
              { key: "event_name", label: "Event", render: (e) => <><div style={{ fontWeight: 500 }}>{e.event_name}</div><div className="muted" style={{ fontSize: 12 }}>{e.description}</div></> },
              { key: "event_date", label: "Date", render: (e) => fmtDate(e.event_date) },
              { key: "location", label: "Location" },
              { key: "present", label: "Present", align: "right", render: (e) => (e.marked ? `${e.present} / ${e.marked}` : <span className="muted">Not marked</span>) },
              { key: "go", label: "", align: "right", render: () => <span className="btn">Mark attendance</span> },
            ]}
          />
        )}
      </Card>
      {creating && <NewEvent onClose={() => setCreating(false)} onSaved={() => { setCreating(false); list.reload(); toast("Event created"); }} />}
    </>
  );
}

function NewEvent({ onClose, onSaved }) {
  const [f, setF] = useState({ event_name: "", event_date: "", location: "", description: "" });
  const [errors, setErrors] = useState({});
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setErrors({ ...errors, [k]: "" }); };
  const save = async () => {
    const er = {};
    if (!f.event_name.trim()) er.event_name = "Enter the event name";
    if (!f.event_date) er.event_date = "Pick a date";
    setErrors(er);
    if (Object.keys(er).length) return;
    try { await api.createCollegeEvent(f); onSaved(); } catch (e) { setErrors({ event_name: e.message }); }
  };
  return (
    <Modal title="New event" onClose={onClose} footer={<><Button onClick={onClose}>Cancel</Button><Button variant="primary" onClick={save}>Create event</Button></>}>
      <Field label="Event name" error={errors.event_name}><input value={f.event_name} onChange={set("event_name")} /></Field>
      <div className="grid grid-2" style={{ gap: 12 }}>
        <Field label="Date" error={errors.event_date}><input type="date" value={f.event_date} onChange={set("event_date")} /></Field>
        <Field label="Location"><input value={f.location} onChange={set("location")} placeholder="Chennai HQ / Online" /></Field>
      </div>
      <Field label="Description"><textarea value={f.description} onChange={set("description")} /></Field>
    </Modal>
  );
}
