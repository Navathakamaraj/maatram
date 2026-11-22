import { useState } from "react";
import { api } from "../api/index.js";
import { navigate } from "../router.jsx";
import { Card, ErrorBox, Loading, PageHeader, Pagination, Table, fmtDate, human, useAsync } from "../components/ui.jsx";

export default function RejectedApplications() {
  const [f, setF] = useState({ cycle_id: "", search: "" });
  const [page, setPage] = useState(1);
  const cycles = useAsync(() => api.listCycles(), []);
  const list = useAsync(() => api.listRejected({ ...f, page, pageSize: 20 }), [JSON.stringify(f), page]);
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setPage(1); };
  const reasons = list.data ? Object.entries(list.data.reasons).sort((a, b) => b[1] - a[1]) : [];

  return (
    <>
      <PageHeader title="Rejected applications" subtitle="Every applicant who was rejected, with the reason and when it happened. Their records stay here for future cycles and audits." />
      {reasons.length > 0 && (
        <div className="stats">
          <div className="stat"><span className="stat-label">Rejected (this filter)</span><span className="stat-value">{list.data.total}</span></div>
          {reasons.slice(0, 3).map(([r, n]) => (
            <button key={r} type="button" className="stat clickable" onClick={() => { setF({ ...f, search: r }); setPage(1); }}>
              <span className="stat-label">{r}</span><span className="stat-value">{n}</span><span className="stat-hint">Click to filter</span>
            </button>
          ))}
        </div>
      )}
      <Card>
        <div className="filters">
          <input type="search" placeholder="Name, application no., email or reason" value={f.search} onChange={set("search")} aria-label="Search rejected applications" />
          <select value={f.cycle_id} onChange={set("cycle_id")} aria-label="Cycle">
            <option value="">All cycles</option>
            {cycles.data?.map((c) => <option key={c.id} value={c.id}>{c.cycle_name}</option>)}
          </select>
        </div>
        {list.loading && !list.data ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={list.reload} /> : (
          <>
            <Table
              rows={list.data.items}
              onRowClick={(a) => navigate(`/applications/${a.id}`)}
              empty="No rejected applications match"
              columns={[
                { key: "application_number", label: "App no.", render: (a) => <span className="mono">{a.application_number}</span> },
                { key: "applicant_name", label: "Applicant", render: (a) => <><div style={{ fontWeight: 500 }}>{a.applicant_name}</div><div className="muted" style={{ fontSize: 12 }}>{a.email} · {a.phone}</div></> },
                { key: "cycle_name", label: "Cycle" },
                { key: "district", label: "District", render: (a) => a.application_data.district },
                { key: "marks", label: "HSC %", align: "right", render: (a) => a.application_data.hsc_marks },
                { key: "rejected_from", label: "Rejected at stage", render: (a) => human(a.rejected_from) },
                { key: "reason", label: "Reason", render: (a) => <span className="t-reason" style={{ marginTop: 0 }}>{a.reason}</span> },
                { key: "rejected_at", label: "Date", render: (a) => fmtDate(a.rejected_at) },
              ]}
            />
            <Pagination page={page} pageSize={20} total={list.data.total} onPage={setPage} />
          </>
        )}
      </Card>
    </>
  );
}
