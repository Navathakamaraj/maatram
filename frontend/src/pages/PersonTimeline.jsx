import { api } from "../api/index.js";
import { Link } from "../router.jsx";
import { Badge, Card, ErrorBox, Loading, PageHeader, fmtDate, fmtDateTime, human, useAsync } from "../components/ui.jsx";

const STAGES = ["APPLICANT", "STUDENT", "ALUMNUS"];

function describe(e) {
  const d = e.event_data || {};
  switch (e.event_type) {
    case "APPLICATION_STATUS_CHANGED": return <>Application moved {human(d.from).toLowerCase()} → <Badge value={d.to} /></>;
    case "CAREER_ENTRY_ADDED": return <>Career entry added: {d.job_title} <Badge value={d.source} /></>;
    default: return human(e.event_type);
  }
}

export default function PersonTimeline({ params }) {
  const t = useAsync(() => api.getPersonTimeline(params.id), [params.id]);
  if (t.loading && !t.data) return <Loading />;
  if (t.error) return <ErrorBox error={t.error} onRetry={t.reload} />;
  const { person: p, stage, applications, student, attendance, alumni, careers, documents, events } = t.data;
  const reached = STAGES.indexOf(stage);
  const present = attendance.filter((a) => a.attendance_status === "PRESENT").length;

  return (
    <>
      <PageHeader
        title={`${p.first_name} ${p.last_name}`}
        subtitle={<><span className="mono">Person #{p.id}</span> · {p.email} · {p.phone}</>}
        actions={<Link to="/persons" className="btn">Back to search</Link>}
      />
      <div className="stage-track" aria-label="Journey stage">
        {STAGES.map((s, i) => <span key={s} className={`stage ${i <= reached ? "on" : ""}`}>{i + 1}. {human(s)}</span>)}
      </div>
      <div className="grid grid-3" style={{ marginTop: 16 }}>
        <div>
          <Card title="Applications">
            {applications.length === 0 ? <div className="empty">No applications</div> : applications.map((a) => (
              <div key={a.id} className="row gap" style={{ padding: "6px 0" }}>
                <Link to={`/applications/${a.id}`} className="mono">{a.application_number}</Link>
                <span className="muted">{a.cycle_name}</span>
                <span className="spacer" />
                <Badge value={a.status} />
              </div>
            ))}
          </Card>
          <Card title="Student record">
            {!student ? <div className="empty">Not a student yet</div> : (
              <dl className="kv">
                <dt>Student no.</dt><dd className="mono">{student.student_number}</dd>
                <dt>Department</dt><dd>{student.department}</dd>
                <dt>Admitted</dt><dd>{fmtDate(student.admission_date)}</dd>
                <dt>Status</dt><dd><Badge value={student.enrollment_status} /></dd>
                <dt>Events</dt><dd>{present} attended of {attendance.length} marked</dd>
              </dl>
            )}
          </Card>
          <Card title="Alumni & career" actions={alumni && <Link to={`/alumni/${alumni.id}`}>Open</Link>}>
            {!alumni ? <div className="empty">Not an alumnus yet</div> : careers.map((c) => (
              <div key={c.id} className="row gap" style={{ padding: "6px 0" }}>
                <div>
                  <div style={{ fontWeight: 500 }}>{c.job_title}</div>
                  <div className="muted" style={{ fontSize: 12 }}>{c.company_name} · {fmtDate(c.start_date)} → {c.end_date ? fmtDate(c.end_date) : "present"}</div>
                </div>
                <span className="spacer" />
                <Badge value={c.source} />
              </div>
            ))}
          </Card>
          <Card title="Documents">
            {documents.length === 0 ? <div className="empty">No documents</div> : documents.map((d) => (
              <div key={d.id} className="row gap" style={{ padding: "4px 0" }}><span>{d.file_name}</span><span className="spacer" /><Badge value={d.upload_status} /></div>
            ))}
          </Card>
        </div>
        <Card title={`Full history · ${events.length} events`}>
          <ul className="timeline">
            {events.map((e) => (
              <li key={e.id} className={`t-${e.aggregate_type}`}>
                <div className="t-title">{describe(e)}</div>
                <div className="t-meta">{fmtDateTime(e.occurred_at)} · event #{e.id}</div>
                {e.event_data?.reason && <div className="t-reason">Reason: {e.event_data.reason}</div>}
              </li>
            ))}
          </ul>
        </Card>
      </div>
    </>
  );
}
