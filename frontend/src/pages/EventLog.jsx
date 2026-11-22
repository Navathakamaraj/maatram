import { useState } from "react";
import { api } from "../api/index.js";
import { Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, Pagination, Table, fmtDateTime, human, useAsync, useToast } from "../components/ui.jsx";

export default function EventLog() {
  const [f, setF] = useState({ aggregate_type: "", event_type: "" });
  const [page, setPage] = useState(1);
  const meta = useAsync(() => api.meta(), []);
  const events = useAsync(() => api.listDomainEvents({ ...f, page, pageSize: 25 }), [JSON.stringify(f), page]);
  const consumers = useAsync(() => api.listConsumers(), []);
  const [mode, setMode] = useState("bookmark");
  const [fromTime, setFromTime] = useState("");
  const [result, setResult] = useState(null);
  const [err, setErr] = useState("");
  const [adding, setAdding] = useState(false);
  const toast = useToast();
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setPage(1); };

  const replay = async (consumer) => {
    setErr("");
    if (mode === "time" && !fromTime) { setErr("Pick a start time for this replay"); return; }
    try {
      const r = await api.replay(consumer, mode, fromTime);
      setResult({ consumer, ...r });
      consumers.reload();
    } catch (e) { setErr(e.message); }
  };
  const late = async () => {
    try {
      const e = await api.simulateLateCommit();
      toast(`Event #${e.id} committed late, behind the bookmarks`);
      consumers.reload(); events.reload();
    } catch (e) { toast(e.message, "error"); }
  };

  return (
    <>
      <PageHeader title="Event log & replay" subtitle="Every change is saved as an append-only event in the same transaction. Workers replay events without sending anything twice." actions={<Button variant="primary" onClick={() => setAdding(true)}>Add new event</Button>} />
      <div className="grid grid-3">
        <Card title="Workers (consumers)" actions={<Button onClick={late}>Simulate out-of-order commit</Button>}>
          {consumers.loading && !consumers.data ? <Loading /> : consumers.error ? <ErrorBox error={consumers.error} /> : (
            <Table
              rows={consumers.data}
              columns={[
                { key: "consumer_name", label: "Worker", render: (c) => <span className="mono">{c.consumer_name}</span> },
                { key: "last_processed_event_id", label: "Bookmark", align: "right", render: (c) => `#${c.last_processed_event_id}` },
                { key: "pending", label: "Not processed yet", align: "right", render: (c) => <strong>{c.pending}</strong> },
                { key: "updated_at", label: "Last run", render: (c) => fmtDateTime(c.updated_at) },
                { key: "run", label: "", align: "right", render: (c) => <Button variant="primary" onClick={() => replay(c.consumer_name)}>Replay</Button> },
              ]}
            />
          )}
          {result && (
            <div className="result-box">
              <strong className="mono">{result.consumer}</strong>: scanned {result.scanned}, processed <strong>{result.processed}</strong>, skipped <strong>{result.skipped_duplicates}</strong> already-processed (no duplicate emails).
              {result.processed_ids.length > 0 && <div className="muted" style={{ marginTop: 4 }}>Processed ids: {result.processed_ids.join(", ")}{result.processed > result.processed_ids.length ? "…" : ""}</div>}
            </div>
          )}
        </Card>
        <Card title="Replay from">
          {[["bookmark", "Last bookmark", "Also re-checks 200 ids behind it, to catch events that committed late"], ["start", "The very start", "Re-runs everything; already-processed events are skipped"], ["time", "A given time", "Only events that happened after this time"]].map(([v, label, hint]) => (
            <label key={v} className="row gap-sm" style={{ alignItems: "flex-start", marginBottom: 10 }}>
              <input type="radio" name="mode" checked={mode === v} onChange={() => { setMode(v); setErr(""); }} style={{ marginTop: 4 }} />
              <span><strong style={{ fontWeight: 500 }}>{label}</strong><br /><span className="muted" style={{ fontSize: 12 }}>{hint}</span></span>
            </label>
          ))}
          {mode === "time" && <Field label="Start time" error={err}><input type="datetime-local" value={fromTime} onChange={(e) => { setFromTime(e.target.value); setErr(""); }} /></Field>}
          {mode !== "time" && err && <div className="field-error">{err}</div>}
        </Card>
      </div>

      <Card title="domain_events">
        <div className="filters">
          <select value={f.aggregate_type} onChange={set("aggregate_type")} aria-label="Aggregate type">
            <option value="">All aggregates</option>
            {meta.data?.aggregate_types.map((t) => <option key={t}>{t}</option>)}
          </select>
          <select value={f.event_type} onChange={set("event_type")} aria-label="Event type">
            <option value="">All event types</option>
            {meta.data?.event_types.map((t) => <option key={t} value={t}>{human(t)}</option>)}
          </select>
        </div>
        {events.loading && !events.data ? <Loading /> : events.error ? <ErrorBox error={events.error} /> : (
          <>
            <Table
              rows={events.data.items}
              columns={[
                { key: "id", label: "#", render: (e) => <span className="mono">{e.id}</span> },
                { key: "event_type", label: "Event", render: (e) => e.event_data?.event_name || human(e.event_type) },
                { key: "agg", label: "Aggregate", render: (e) => <span className="mono">{e.aggregate_type}:{e.aggregate_id}</span> },
                { key: "data", label: "Description / data", render: (e) => e.event_data?.description
                  ? <span className="muted">{e.event_data.description.slice(0, 90)}</span>
                  : <span className="mono muted">{JSON.stringify(e.event_data).slice(0, 70)}</span> },
                { key: "occurred_at", label: "Occurred", render: (e) => fmtDateTime(e.occurred_at) },
              ]}
            />
            <Pagination page={page} pageSize={25} total={events.data.total} onPage={setPage} />
          </>
        )}
      </Card>
      {adding && (
        <AddEventModal
          onClose={() => setAdding(false)}
          onSaved={(e) => { setAdding(false); toast(`Event #${e.id} added to the log`); events.reload(); consumers.reload(); meta.reload(); setPage(1); }}
        />
      )}
    </>
  );
}

// Manual events: just a name, when it happened, and what happened.
function AddEventModal({ onClose, onSaved }) {
  const pad = (n) => String(n).padStart(2, "0");
  const d = new Date();
  const nowLocal = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  const [f, setF] = useState({ event_name: "", occurred_at: nowLocal, description: "" });
  const [errors, setErrors] = useState({});
  const [busy, setBusy] = useState(false);
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setErrors({ ...errors, [k]: "", server: "" }); };

  const save = async () => {
    const er = {};
    if (!f.event_name.trim()) er.event_name = "Enter the event name";
    if (!f.occurred_at || Number.isNaN(new Date(f.occurred_at).getTime())) er.occurred_at = "Pick the date and time";
    if (!f.description.trim()) er.description = "Describe what happened";
    setErrors(er);
    if (Object.keys(er).length) return;
    setBusy(true);
    try {
      const e = await api.appendEvent({ event_name: f.event_name.trim(), occurred_at: new Date(f.occurred_at).toISOString(), description: f.description.trim() });
      onSaved(e);
    } catch (x) { setErrors({ server: x.message }); }
    finally { setBusy(false); }
  };

  return (
    <Modal title="Add new event" onClose={onClose} footer={<><Button onClick={onClose}>Cancel</Button><Button variant="primary" disabled={busy} onClick={save}>{busy ? "Adding…" : "Add to log"}</Button></>}>
      <p className="muted" style={{ marginTop: 0, fontSize: 13 }}>Events are append-only: once added they can’t be edited or deleted.</p>
      <Field label="Event name" error={errors.event_name}>
        <input value={f.event_name} onChange={set("event_name")} placeholder="e.g. Interview scheduled" autoFocus />
      </Field>
      <Field label="Date & time" error={errors.occurred_at}>
        <input type="datetime-local" value={f.occurred_at} onChange={set("occurred_at")} />
      </Field>
      <Field label="Description" error={errors.description}>
        <textarea rows={4} value={f.description} onChange={set("description")} placeholder="What happened?" />
      </Field>
      {errors.server && <div className="field-error">{errors.server}</div>}
    </Modal>
  );
}
