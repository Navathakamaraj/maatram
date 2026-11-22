import { useState } from "react";
import { api } from "../api/index.js";
import { navigate, queryParams } from "../router.jsx";
import { Badge, Card, ErrorBox, Loading, PageHeader, Table, useAsync } from "../components/ui.jsx";

export default function Persons({ path }) {
  const [q, setQ] = useState(queryParams(path).q || "");
  const res = useAsync(() => api.searchPersons(q), [q]);
  return (
    <>
      <PageHeader title="People search" subtitle="Every applicant, student and alumnus is one person with one ID. Open anyone to see their whole journey." />
      <Card>
        <div className="filters">
          <input type="search" autoFocus placeholder="Name, email or phone" value={q} onChange={(e) => setQ(e.target.value)} style={{ minWidth: 320 }} aria-label="Search people" />
        </div>
        {res.loading && !res.data ? <Loading /> : res.error ? <ErrorBox error={res.error} /> : (
          <Table
            rows={res.data}
            onRowClick={(p) => navigate(`/persons/${p.id}`)}
            empty="No one matches that search"
            columns={[
              { key: "id", label: "Person ID", render: (p) => <span className="mono">#{p.id}</span> },
              { key: "name", label: "Name", render: (p) => <strong>{p.first_name} {p.last_name}</strong> },
              { key: "email", label: "Email" },
              { key: "phone", label: "Phone" },
              { key: "stage", label: "Stage", render: (p) => <Badge value={p.stage} /> },
            ]}
          />
        )}
        {res.data?.length === 25 && <p className="muted" style={{ fontSize: 12 }}>Showing the first 25 matches. Type more to narrow down.</p>}
      </Card>
    </>
  );
}
