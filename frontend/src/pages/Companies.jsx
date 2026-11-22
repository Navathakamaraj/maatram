import { useState } from "react";
import { api } from "../api/index.js";
import { navigate } from "../router.jsx";
import { Button, Card, ErrorBox, Loading, PageHeader, useAsync, useToast } from "../components/ui.jsx";

export default function Companies() {
  const list = useAsync(() => api.listCompanies(), []);
  const [test, setTest] = useState("");
  const [out, setOut] = useState("");
  const toast = useToast();

  const merge = async (g) => {
    try {
      const r = await api.mergeCompanies(g.normalized_name);
      toast(`Merged ${r.merged} spelling(s) into “${r.kept.company_name}”`);
      list.reload();
    } catch (e) { toast(e.message, "error"); }
  };

  const dupes = (list.data || []).filter((g) => g.variants.length > 1).length;

  return (
    <>
      <PageHeader title="Companies" subtitle="Different spellings of the same employer are grouped by a predictable rule, so career questions give one answer." />
      <div className="grid grid-3">
        <Card title={`Employers ${dupes ? `· ${dupes} with duplicate spellings` : ""}`}>
          {list.loading && !list.data ? <Loading /> : list.error ? <ErrorBox error={list.error} onRetry={list.reload} /> : (
            <div className="table-wrap">
              <table>
                <thead><tr><th>Matched as</th><th>Spellings in data</th><th style={{ textAlign: "right" }}>Alumni there now</th><th /></tr></thead>
                <tbody>
                  {list.data.map((g) => (
                    <tr key={g.normalized_name}>
                      <td className="mono">{g.normalized_name}</td>
                      <td>{g.variants.map((v) => v.company_name).join(" · ")}</td>
                      <td style={{ textAlign: "right" }}>
                        <a href={`#/alumni`} onClick={(e) => { e.preventDefault(); navigate("/alumni"); }}>{g.alumni_count}</a>
                      </td>
                      <td style={{ textAlign: "right" }}>{g.variants.length > 1 && <Button onClick={() => merge(g)}>Merge {g.variants.length}</Button>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
        <Card title="Merge rules">
          <ol style={{ margin: "0 0 12px", paddingLeft: 18, color: "var(--ink-2)" }}>
            <li>Lowercase and trim</li>
            <li>Remove dots and commas</li>
            <li>Drop suffixes: pvt, private, ltd, limited, inc, llp</li>
            <li>Collapse extra spaces</li>
            <li>Apply the alias list (tcs → tata consultancy services)</li>
          </ol>
          <label className="field">
            <span className="field-label">Try a spelling</span>
            <input value={test} onChange={(e) => { setTest(e.target.value); api.normalizePreview(e.target.value).then(setOut); }} placeholder="Zoho Corporation Pvt. Ltd." />
          </label>
          {test && <div className="result-box">Matched as <strong className="mono">{out || "(empty)"}</strong></div>}
        </Card>
      </div>
    </>
  );
}
