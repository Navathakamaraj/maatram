import { useEffect, useMemo, useState } from "react";
import { api } from "../api/index.js";
import { Link } from "../router.jsx";
import { Button, Card, ErrorBox, Loading, PageHeader, fmtDate, useAsync, useToast } from "../components/ui.jsx";

const OPTIONS = ["PRESENT", "ABSENT", "EXCUSED"];

export default function Attendance({ params }) {
  const data = useAsync(() => api.getAttendance(params.id), [params.id]);
  const [marks, setMarks] = useState({});
  const [search, setSearch] = useState("");
  const [busy, setBusy] = useState(false);
  const toast = useToast();

  useEffect(() => {
    if (data.data) setMarks(Object.fromEntries(data.data.rows.map((r) => [r.student_id, r.attendance_status])));
  }, [data.data]);

  const rows = useMemo(() => (data.data?.rows || []).filter((r) => !search || `${r.name} ${r.student_number}`.toLowerCase().includes(search.toLowerCase())), [data.data, search]);
  const changed = (data.data?.rows || []).filter((r) => marks[r.student_id] && marks[r.student_id] !== r.attendance_status);
  const counts = OPTIONS.map((o) => [o, Object.values(marks).filter((m) => m === o).length]);

  if (data.loading && !data.data) return <Loading />;
  if (data.error) return <ErrorBox error={data.error} onRetry={data.reload} />;
  const ev = data.data.event;

  const markAll = (status) => setMarks((m) => { const n = { ...m }; rows.forEach((r) => (n[r.student_id] = status)); return n; });
  const save = async () => {
    setBusy(true);
    try {
      const r = await api.markAttendance(ev.id, changed.map((x) => ({ student_id: x.student_id, attendance_status: marks[x.student_id] })));
      toast(`Saved ${r.changed} attendance change${r.changed === 1 ? "" : "s"}`);
      data.reload();
    } catch (e) { toast(e.message, "error"); }
    finally { setBusy(false); }
  };

  return (
    <>
      <PageHeader
        title={ev.event_name}
        subtitle={`${fmtDate(ev.event_date)} · ${ev.location || "No location"}`}
        actions={<><Link to="/events" className="btn">All events</Link><Button variant="primary" disabled={!changed.length || busy} onClick={save}>{busy ? "Saving…" : `Save ${changed.length || ""} change${changed.length === 1 ? "" : "s"}`}</Button></>}
      />
      <div className="stats">
        {counts.map(([o, n]) => <div key={o} className="stat"><span className="stat-label">{o.toLowerCase()}</span><span className="stat-value">{n}</span></div>)}
        <div className="stat"><span className="stat-label">not marked</span><span className="stat-value">{data.data.rows.length - Object.values(marks).filter(Boolean).length}</span></div>
      </div>
      <Card>
        <div className="filters">
          <input type="search" placeholder="Find a student" value={search} onChange={(e) => setSearch(e.target.value)} aria-label="Find student" />
          <span className="spacer" />
          <Button onClick={() => markAll("PRESENT")}>Mark all shown present</Button>
        </div>
        <div className="table-wrap">
          <table>
            <thead><tr><th>Student no.</th><th>Name</th><th>Department</th><th>Attendance</th></tr></thead>
            <tbody>
              {rows.map((r) => (
                <tr key={r.student_id}>
                  <td className="mono">{r.student_number}</td>
                  <td>{r.name}</td>
                  <td>{r.department}</td>
                  <td>
                    <div className="seg" role="radiogroup" aria-label={`Attendance for ${r.name}`}>
                      {OPTIONS.map((o) => (
                        <button key={o} type="button" role="radio" aria-checked={marks[r.student_id] === o} className={marks[r.student_id] === o ? `on-${o}` : ""} onClick={() => setMarks({ ...marks, [r.student_id]: o })}>
                          {o[0] + o.slice(1).toLowerCase()}
                        </button>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>
    </>
  );
}
