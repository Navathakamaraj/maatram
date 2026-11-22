import { api } from "../api/index.js";
import { navigate, Link } from "../router.jsx";
import { Badge, Card, ErrorBox, Loading, PageHeader, Stat, fmtDate, fmtDateTime, human, useAsync } from "../components/ui.jsx";

const ORDER = ["DRAFT", "SUBMITTED", "UNDER_REVIEW", "SHORTLISTED", "SELECTED", "REJECTED"];

export default function Dashboard() {
  const dash = useAsync(() => api.getDashboard(), []);
  const cmp = useAsync(() => api.compareCycles(), []);
  if (dash.loading) return <Loading />;
  if (dash.error) return <ErrorBox error={dash.error} onRetry={dash.reload} />;
  const { counts, openCycle, byStatus, recentEvents } = dash.data;

  return (
    <>
      <PageHeader
        title="Dashboard"
        subtitle={openCycle ? `${openCycle.cycle_name} is open until ${fmtDate(openCycle.end_date)}` : "No admission cycle is open right now"}
      />
      <div className="stats">
        <Stat label="Applications (open cycle)" value={counts.applicationsOpenCycle} onClick={() => navigate(`/applications?cycle_id=${openCycle?.id || ""}`)} />
        <Stat label="Active students" value={counts.activeStudents} onClick={() => navigate("/students")} />
        <Stat label="Alumni" value={counts.alumni} onClick={() => navigate("/alumni")} />
        <Stat label="Rejected (open cycle)" value={counts.rejectedOpenCycle} onClick={() => navigate("/rejected")} />
        <Stat label="People on record" value={counts.persons} hint="One record per person" onClick={() => navigate("/persons")} />
        <Stat label="Events in log" value={counts.events} onClick={() => navigate("/event-log")} />
      </div>

      <div className="grid grid-3">
        <Card title="Year over year" actions={<Link to="/cycles">All cycles</Link>}>
          {cmp.loading ? <Loading /> : cmp.error ? <ErrorBox error={cmp.error} /> : <CycleBars rows={cmp.data} />}
        </Card>
        <Card title="Open cycle pipeline">
          {openCycle ? (
            <table>
              <tbody>
                {ORDER.map((s) => (
                  <tr key={s} className="clickable" onClick={() => navigate(`/applications?cycle_id=${openCycle.id}&status=${s}`)}>
                    <td><Badge value={s} /></td>
                    <td style={{ textAlign: "right", fontWeight: 600 }}>{byStatus[s] || 0}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <div className="empty">Open a cycle to start taking applications.</div>}
        </Card>
      </div>

      <Card title="Latest activity" actions={<Link to="/event-log">Open event log</Link>}>
        <ul className="timeline">
          {recentEvents.map((e) => (
            <li key={e.id} className={`t-${e.aggregate_type}`}>
              <div className="t-title">{human(e.event_type)}</div>
              <div className="t-meta">#{e.id} · {e.aggregate_type} {e.aggregate_id} · {fmtDateTime(e.occurred_at)}</div>
            </li>
          ))}
        </ul>
      </Card>
    </>
  );
}

export function CycleBars({ rows }) {
  const max = Math.max(1, ...rows.map((r) => r.total));
  const parts = [
    ["selected", "#2f9e5b", "Selected"],
    ["shortlisted", "#e0a100", "Shortlisted"],
    ["rejected", "#d16060", "Rejected"],
  ];
  return (
    <>
      <div className="bars">
        {rows.map((r) => {
          const other = r.total - r.selected - r.shortlisted - r.rejected;
          return (
            <div className="bar-row" key={r.cycle_id}>
              <div>
                <div style={{ fontWeight: 500 }}>{r.academic_year}</div>
                <div className="muted" style={{ fontSize: 12 }}>{r.total} apps · {r.selection_rate}% selected</div>
              </div>
              <div className="bar-stack" style={{ width: `${(r.total / max) * 100}%` }} title={`${r.total} applications`}>
                {parts.map(([k, color, label]) => r[k] > 0 && (
                  <span key={k} style={{ width: `${(r[k] / r.total) * 100}%`, background: color }} title={`${label}: ${r[k]}`} />
                ))}
                {other > 0 && <span style={{ width: `${(other / r.total) * 100}%`, background: "#9db4e8" }} title={`In progress: ${other}`} />}
              </div>
            </div>
          );
        })}
      </div>
      <div className="legend">
        {parts.map(([k, color, label]) => <span key={k}><i style={{ background: color }} />{label}</span>)}
        <span><i style={{ background: "#9db4e8" }} />In progress</span>
      </div>
    </>
  );
}
