import { useState } from "react";
import { api } from "../api/index.js";
import { Badge, Card, ErrorBox, Loading, PageHeader, Pagination, Table, fmtDate, fmtSize, useAsync } from "../components/ui.jsx";

export default function Documents() {
  const [f, setF] = useState({ search: "", upload_status: "" });
  const [page, setPage] = useState(1);
  const list = useAsync(() => api.listDocuments({ ...f, page, pageSize: 20 }), [JSON.stringify(f), page]);
  const [check, setCheck] = useState(null);
  const set = (k) => (e) => { setF({ ...f, [k]: e.target.value }); setPage(1); };

  const tryFile = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      const r = await api.checkUpload(file);
      setCheck({ ok: true, msg: `${file.name} (${fmtSize(file.size)}) passed the checks. Signed upload link valid for ${r.expires_in / 60} minutes.` });
    } catch (err) {
      setCheck({ ok: false, msg: `${file.name}: ${err.message}` });
    }
    e.target.value = "";
  };

  return (
    <>
      <PageHeader title="Documents" subtitle="Files go straight from the browser to S3-compatible storage (MinIO) through short-lived signed links." />
      <Card title="Test the upload rules">
        <p className="muted" style={{ marginTop: 0 }}>Allowed: PDF, JPG, PNG up to 5 MB. The server checks type and size before it gives out a signed link.</p>
        <input type="file" onChange={tryFile} aria-label="Choose a file to test" />
        {check && <div className="result-box" style={{ background: check.ok ? "var(--green)" : "var(--red)", color: check.ok ? "var(--green-ink)" : "var(--red-ink)" }}>{check.msg}</div>}
      </Card>
      <Card>
        <div className="filters">
          <input type="search" placeholder="Owner, file name or application no." value={f.search} onChange={set("search")} aria-label="Search documents" />
          <select value={f.upload_status} onChange={set("upload_status")} aria-label="Upload status">
            <option value="">All</option>
            <option value="UPLOADED">Uploaded</option>
            <option value="REJECTED">Rejected</option>
          </select>
        </div>
        {list.loading && !list.data ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={list.reload} /> : (
          <>
            <Table
              rows={list.data.items}
              columns={[
                { key: "file_name", label: "File", render: (d) => <strong>{d.file_name}</strong> },
                { key: "owner", label: "Owner" },
                { key: "application_number", label: "Application", render: (d) => <span className="mono">{d.application_number || "-"}</span> },
                { key: "file_size", label: "Size", align: "right", render: (d) => fmtSize(d.file_size) },
                { key: "storage_key", label: "Storage key", render: (d) => <span className="mono muted">{d.storage_key}</span> },
                { key: "uploaded_at", label: "Uploaded", render: (d) => fmtDate(d.uploaded_at) },
                { key: "upload_status", label: "Status", render: (d) => <Badge value={d.upload_status} /> },
              ]}
            />
            <Pagination page={page} pageSize={20} total={list.data.total} onPage={setPage} />
          </>
        )}
      </Card>
    </>
  );
}
