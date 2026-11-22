import { useState } from "react";
import { api } from "../api/index.js";
import { navigate } from "../router.jsx";
import { Badge, Button, Card, ErrorBox, Field, Loading, Modal, PageHeader, Table, fmtDate, useAsync, useToast } from "../components/ui.jsx";

export default function AlumniRegistrations() {
  const [status, setStatus] = useState("PENDING");
  const list = useAsync(() => api.listAlumniRegistrations({ registration_status: status }), [status]);
  const [rejecting, setRejecting] = useState(null);
  const toast = useToast();

  const approve = async (r) => {
    try {
      await api.reviewAlumniRegistration(r.id, "APPROVED");
      toast(`${r.name} is now an alumnus`);
      list.reload();
    } catch (e) { toast(e.message, "error"); }
  };

  return (
    <>
      <PageHeader title="Alumni requests" subtitle="Graduates ask to join the alumni network. Approving links them to their student record and starts their career history." />
      <Card>
        <div className="filters">
          <div className="seg" role="tablist">
            {["PENDING", "APPROVED", "REJECTED", ""].map((s) => (
              <button key={s || "all"} type="button" className={status === s ? "on-PRESENT" : ""} onClick={() => setStatus(s)}>{s ? s[0] + s.slice(1).toLowerCase() : "All"}</button>
            ))}
          </div>
        </div>
        {list.loading && !list.data ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={list.reload} /> : (
          <Table
            rows={list.data}
            empty={status === "PENDING" ? "No requests waiting. All caught up." : "Nothing here"}
            onRowClick={(r) => navigate(`/persons/${r.person_id}`)}
            columns={[
              { key: "name", label: "Name", render: (r) => <strong>{r.name}</strong> },
              { key: "student_number", label: "Student no.", render: (r) => <span className="mono">{r.student_number || "-"}</span> },
              { key: "dept", label: "Dept / year", render: (r) => `${r.department} · ${r.graduation_year}` },
              { key: "job", label: "Says they work at", render: (r) => <>{r.current_job_title}<div className="muted" style={{ fontSize: 12 }}>{r.current_company}</div></> },
              { key: "registered_at", label: "Requested", render: (r) => fmtDate(r.registered_at) },
              { key: "registration_status", label: "Status", render: (r) => <Badge value={r.registration_status} /> },
              {
                key: "act", label: "", align: "right",
                render: (r) => r.registration_status === "PENDING" && (
                  <span className="row gap-sm" style={{ justifyContent: "flex-end" }} onClick={(e) => e.stopPropagation()}>
                    <Button variant="success" onClick={() => approve(r)}>Approve</Button>
                    <Button variant="danger" onClick={() => setRejecting(r)}>Reject</Button>
                  </span>
                ),
              },
            ]}
          />
        )}
      </Card>
      {rejecting && <RejectModal reg={rejecting} onClose={() => setRejecting(null)} onDone={() => { setRejecting(null); list.reload(); toast("Request rejected"); }} />}
    </>
  );
}

function RejectModal({ reg, onClose, onDone }) {
  const [reason, setReason] = useState("");
  const [err, setErr] = useState("");
  const run = async () => {
    if (!reason.trim()) { setErr("Enter a reason"); return; }
    try { await api.reviewAlumniRegistration(reg.id, "REJECTED", reason.trim()); onDone(); } catch (e) { setErr(e.message); }
  };
  return (
    <Modal title={`Reject ${reg.name}`} onClose={onClose} footer={<><Button onClick={onClose}>Cancel</Button><Button variant="danger" onClick={run}>Reject request</Button></>}>
      <Field label="Reason (required)" error={err}>
        <textarea value={reason} onChange={(e) => { setReason(e.target.value); setErr(""); }} placeholder="e.g. Could not verify graduation record" />
      </Field>
    </Modal>
  );
}
