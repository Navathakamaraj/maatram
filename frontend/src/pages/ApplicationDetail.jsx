import { useState } from "react";
import { api } from "../api/index.js";
import { Link } from "../router.jsx";
import { Badge, Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, fmtDate, fmtDateTime, fmtSize, human, useAsync, useToast } from "../components/ui.jsx";

export default function ApplicationDetail({ params }) {
  const app = useAsync(() => api.getApplication(params.id), [params.id]);
  const [to, setTo] = useState(null);
  if (app.loading && !app.data) return <Loading />;
  if (app.error) return <ErrorBox error={app.error} onRetry={app.reload} />;
  const a = app.data;
  const d = a.application_data;

  return (
    <>
      <PageHeader
        title={`${a.person.first_name} ${a.person.last_name}`}
        subtitle={<><span className="mono">{a.application_number}</span> · {a.cycle.cycle_name} · <Badge value={a.status} /></>}
        actions={
          <>
            <Link to="/applications" className="btn">Back to list</Link>
            {a.allowed.map((s) => (
              <Button key={s} variant={s === "SELECTED" ? "brand" : s === "REJECTED" ? "danger" : "default"} onClick={() => setTo(s)}>
                {s === "REJECTED" ? "Reject" : s === "SELECTED" ? "Select" : human(s)}
              </Button>
            ))}
          </>
        }
      />
      <div className="grid grid-3">
        <div>
          <Card title="Applicant">
            <dl className="kv">
              <dt>Email</dt><dd>{a.person.email}</dd>
              <dt>Phone</dt><dd>{a.person.phone}</dd>
              <dt>Date of birth</dt><dd>{fmtDate(a.person.date_of_birth)}</dd>
              <dt>Gender</dt><dd>{a.person.gender}</dd>
              <dt>Full record</dt><dd><Link to={`/persons/${a.person.id}`}>Open person timeline →</Link></dd>
            </dl>
          </Card>
          <Card title="Application form">
            <dl className="kv">
              <dt>School</dt><dd>{d.school}</dd>
              <dt>District</dt><dd>{d.district}</dd>
              <dt>HSC marks</dt><dd>{d.hsc_marks}%</dd>
              <dt>Family income</dt><dd>₹{d.family_income.toLocaleString("en-IN")} / year</dd>
              <dt>Preferred course</dt><dd>{d.preferred_course}</dd>
              <dt>Submitted</dt><dd>{fmtDateTime(a.submitted_at)}</dd>
              <dt>Reviewed by</dt><dd>{a.reviewer || "-"}</dd>
            </dl>
          </Card>
          <Card title="Documents">
            {a.documents.length === 0 ? <div className="empty">No documents uploaded</div> : (
              <table>
                <tbody>
                  {a.documents.map((doc) => (
                    <tr key={doc.id}>
                      <td>{doc.file_name}</td>
                      <td className="muted">{fmtSize(doc.file_size)}</td>
                      <td><Badge value={doc.upload_status} /></td>
                      <td className="mono muted">{doc.storage_key}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </Card>
          {a.student && (
            <Card title="Linked student record">
              <dl className="kv">
                <dt>Student no.</dt><dd className="mono">{a.student.student_number}</dd>
                <dt>Department</dt><dd>{a.student.department}</dd>
                <dt>Status</dt><dd><Badge value={a.student.enrollment_status} /></dd>
              </dl>
            </Card>
          )}
        </div>
        <Card title="Audit trail">
          <ul className="timeline">
            {a.history.map((e) => (
              <li key={e.id} className="t-application">
                <div className="t-title">
                  {e.event_type === "APPLICATION_STATUS_CHANGED" ? <>{human(e.event_data.from)} → <Badge value={e.event_data.to} /></> : human(e.event_type)}
                </div>
                <div className="t-meta">{fmtDateTime(e.occurred_at)}{e.event_data.changed_by ? ` · staff #${e.event_data.changed_by}` : ""}</div>
                {e.event_data.reason && <div className="t-reason">Reason: {e.event_data.reason}</div>}
              </li>
            ))}
          </ul>
        </Card>
      </div>
      {to && <SingleStatusModal app={a} to={to} onClose={() => setTo(null)} onDone={() => { setTo(null); app.reload(); }} />}
    </>
  );
}

function SingleStatusModal({ app, to, onClose, onDone }) {
  const [reason, setReason] = useState("");
  const [err, setErr] = useState("");
  const toast = useToast();
  const run = async () => {
    if (to === "REJECTED" && !reason.trim()) { setErr("Enter a reason for the rejection"); return; }
    try {
      const r = await api.bulkUpdateStatus({ ids: [app.id], to_status: to, reason: reason.trim() || null });
      if (r.errors.length) { setErr(r.errors[0].error); return; }
      toast(`Moved to ${human(to).toLowerCase()}`);
      onDone();
    } catch (e) { setErr(e.message); }
  };
  return (
    <Modal title={`${human(to)}: ${app.application_number}`} onClose={onClose} footer={<><Button onClick={onClose}>Cancel</Button><Button variant={to === "REJECTED" ? "danger" : "primary"} onClick={run}>Confirm</Button></>}>
      <Field label={to === "REJECTED" ? "Reason (required)" : "Reason (optional)"} error={err}>
        <textarea value={reason} onChange={(e) => { setReason(e.target.value); setErr(""); }} />
      </Field>
    </Modal>
  );
}
