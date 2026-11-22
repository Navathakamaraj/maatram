import { useEffect, useState } from "react";
import { api } from "../api/index.js";
import { navigate, queryParams } from "../router.jsx";
import ImportStatusModal from "./ImportStatusModal.jsx";
import { Badge, Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, Pagination, Table, fmtDate, human, useAsync, useToast } from "../components/ui.jsx";

const STATUSES = ["DRAFT", "SUBMITTED", "UNDER_REVIEW", "SHORTLISTED", "SELECTED", "REJECTED"];
const ACTIONS = [
  ["UNDER_REVIEW", "Mark under review", "default"],
  ["SHORTLISTED", "Shortlist", "default"],
  ["SELECTED", "Select", "brand"],
  ["REJECTED", "Reject", "default"],
];

export default function Applications({ path }) {
  const initial = queryParams(path);
  const [filters, setFilters] = useState({ cycle_id: initial.cycle_id || "", status: initial.status || "", district: "", search: "" });
  const [page, setPage] = useState(1);
  const [selected, setSelected] = useState(new Set());
  const [action, setAction] = useState(null);
  const [importing, setImporting] = useState(false);
  const meta = useAsync(() => api.meta(), []);
  const cycles = useAsync(() => api.listCycles(), []);
  const list = useAsync(() => api.listApplications({ ...filters, page, pageSize: 20 }), [JSON.stringify(filters), page]);

  // default to the open (or latest) cycle
  useEffect(() => {
    if (!filters.cycle_id && cycles.data?.length && !initial.cycle_id) {
      const open = cycles.data.find((c) => c.status === "OPEN") || cycles.data[0];
      setFilters((f) => ({ ...f, cycle_id: String(open.id) }));
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cycles.data]);

  const setFilter = (k) => (e) => { setFilters({ ...filters, [k]: e.target.value }); setPage(1); setSelected(new Set()); };
  const toggle = (id) => setSelected((s) => { const n = new Set(s); n.has(id) ? n.delete(id) : n.add(id); return n; });
  const toggleAll = (rows, on) => setSelected((s) => { const n = new Set(s); rows.forEach((r) => (on ? n.add(r.id) : n.delete(r.id))); return n; });
  const selectAllMatching = async () => setSelected(new Set(await api.listApplicationIds(filters)));

  const pageAllSelected = list.data?.items.length > 0 && list.data.items.every((r) => selected.has(r.id));

  return (
    <>
      <PageHeader title="Applications" subtitle="Review, shortlist and select applicants. Tick rows to act on many at once, or import a CSV." actions={<Button variant="primary" onClick={() => setImporting(true)}>Import file</Button>} />
      <Card>
        <div className="filters">
          <input type="search" placeholder="Name, application no., email, phone" value={filters.search} onChange={setFilter("search")} aria-label="Search applications" />
          <select value={filters.cycle_id} onChange={setFilter("cycle_id")} aria-label="Cycle">
            <option value="">All cycles</option>
            {cycles.data?.map((c) => <option key={c.id} value={c.id}>{c.cycle_name}</option>)}
          </select>
          <select value={filters.status} onChange={setFilter("status")} aria-label="Status">
            <option value="">All statuses</option>
            {STATUSES.map((s) => <option key={s} value={s}>{human(s)}</option>)}
          </select>
          <select value={filters.district} onChange={setFilter("district")} aria-label="District">
            <option value="">All districts</option>
            {meta.data?.districts.map((d) => <option key={d}>{d}</option>)}
          </select>
        </div>

        {pageAllSelected && list.data.total > selected.size && (
          <div className="result-box" style={{ marginTop: 0, marginBottom: 10 }}>
            All {selected.size} on this page are selected.{" "}
            <a href="#" onClick={(e) => { e.preventDefault(); selectAllMatching(); }}>Select all {list.data.total} matching this filter</a>
          </div>
        )}

        {list.loading && !list.data ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={list.reload} /> : (
          <>
            <Table
              rows={list.data.items}
              selectable selected={selected} onToggle={toggle} onToggleAll={toggleAll}
              onRowClick={(a) => navigate(`/applications/${a.id}`)}
              empty="No applications match these filters"
              columns={[
                { key: "application_number", label: "App no.", render: (a) => <span className="mono">{a.application_number}</span> },
                { key: "applicant_name", label: "Applicant", render: (a) => <><div style={{ fontWeight: 500 }}>{a.applicant_name}</div><div className="muted" style={{ fontSize: 12 }}>{a.email}</div></> },
                { key: "district", label: "District", render: (a) => a.application_data.district },
                { key: "marks", label: "HSC %", align: "right", render: (a) => a.application_data.hsc_marks },
                { key: "income", label: "Family income", align: "right", render: (a) => `₹${a.application_data.family_income.toLocaleString("en-IN")}` },
                { key: "submitted_at", label: "Submitted", render: (a) => fmtDate(a.submitted_at) },
                { key: "status", label: "Status", render: (a) => <Badge value={a.status} /> },
              ]}
            />
            <Pagination page={page} pageSize={20} total={list.data.total} onPage={setPage} />
          </>
        )}

        {selected.size > 0 && (
          <div className="bulk-bar">
            <strong>{selected.size} selected</strong>
            <span className="spacer" />
            {ACTIONS.map(([to, label, v]) => <Button key={to} variant={v} onClick={() => setAction(to)}>{label}</Button>)}
            <Button onClick={() => setSelected(new Set())}>Clear</Button>
          </div>
        )}
      </Card>

      {importing && <ImportStatusModal onClose={() => setImporting(false)} onDone={() => { setSelected(new Set()); list.reload(); }} />}

      {action && (
        <BulkModal
          ids={[...selected]}
          to={action}
          onClose={() => setAction(null)}
          onDone={() => { setSelected(new Set()); list.reload(); }}
        />
      )}
    </>
  );
}

function BulkModal({ ids: initialIds, to, onClose, onDone }) {
  const [ids] = useState(initialIds); // keep the count stable after the parent clears its selection
  const [reason, setReason] = useState("");
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const toast = useToast();
  const needsReason = to === "REJECTED";

  const run = async () => {
    if (needsReason && !reason.trim()) { setErr("Enter a reason. It is saved in the audit trail for every rejected applicant."); return; }
    setBusy(true);
    try {
      const r = await api.bulkUpdateStatus({ ids, to_status: to, reason: reason.trim() || null });
      setResult(r);
      toast(`${r.updated} updated${r.errors.length ? `, ${r.errors.length} could not be changed` : ""}`);
      onDone();
    } catch (e) { setErr(e.message); }
    finally { setBusy(false); }
  };

  return (
    <Modal
      title={`${human(to)}: ${ids.length} application${ids.length > 1 ? "s" : ""}`}
      onClose={onClose}
      footer={result
        ? <Button variant="primary" onClick={onClose}>Done</Button>
        : <><Button onClick={onClose}>Cancel</Button><Button variant={to === "REJECTED" ? "danger" : "primary"} disabled={busy} onClick={run}>{busy ? "Working…" : `Confirm ${human(to).toLowerCase()}`}</Button></>}
    >
      {!result ? (
        <>
          {to === "SELECTED" && <p className="muted" style={{ marginTop: 0 }}>Selecting creates a linked student record for each person (only once per person).</p>}
          <Field label={needsReason ? "Reason (required)" : "Reason (optional)"} error={err}>
            <textarea value={reason} onChange={(e) => { setReason(e.target.value); setErr(""); }} placeholder={needsReason ? "e.g. Family income above eligibility limit" : "Optional note for the audit trail"} />
          </Field>
          <p className="muted" style={{ fontSize: 12, marginBottom: 0 }}>Rows that can’t move to this status (for example, already rejected) are skipped and listed afterwards. The rest still go through.</p>
        </>
      ) : (
        <div>
          <div className="stats" style={{ marginBottom: 8 }}>
            <div className="stat"><span className="stat-label">Updated</span><span className="stat-value">{result.updated}</span></div>
            <div className="stat"><span className="stat-label">Already in status</span><span className="stat-value">{result.skipped}</span></div>
            <div className="stat"><span className="stat-label">Not allowed</span><span className="stat-value">{result.errors.length}</span></div>
          </div>
          {result.errors.length > 0 && (
            <div className="result-box">
              <strong>Not changed:</strong>
              <ul>{result.errors.slice(0, 8).map((e) => <li key={e.id}><span className="mono">{e.application_number || e.id}</span>: {e.error}</li>)}</ul>
              {result.errors.length > 8 && <div className="muted">…and {result.errors.length - 8} more</div>}
            </div>
          )}
        </div>
      )}
    </Modal>
  );
}
