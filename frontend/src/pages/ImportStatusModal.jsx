import { useState } from "react";
import { api } from "../api/index.js";
import { Badge, Button, Field, Modal, human, useToast } from "../components/ui.jsx";

const STATUSES = ["SHORTLISTED", "SELECTED", "REJECTED", "UNDER_REVIEW"];
const MAX_BYTES = 1024 * 1024;

// Small CSV parser: handles quoted fields with commas and "" escapes.
export function parseCsv(textIn) {
  const rows = [];
  let row = [], cell = "", q = false;
  const t = textIn.replace(/^﻿/, "");
  for (let i = 0; i < t.length; i++) {
    const c = t[i];
    if (q) {
      if (c === '"' && t[i + 1] === '"') { cell += '"'; i++; }
      else if (c === '"') q = false;
      else cell += c;
    } else if (c === '"') q = true;
    else if (c === ",") { row.push(cell); cell = ""; }
    else if (c === "\n" || c === "\r") {
      if (c === "\r" && t[i + 1] === "\n") i++;
      row.push(cell); rows.push(row); row = []; cell = "";
    } else cell += c;
  }
  if (cell !== "" || row.length) { row.push(cell); rows.push(row); }
  return rows.filter((r) => r.some((v) => v.trim() !== ""));
}

// Turn CSV rows into [{application_number, reason, line}]
export function readRows(raw) {
  if (!raw.length) return { rows: [], hasReason: false };
  const head = raw[0].map((h) => h.trim().toLowerCase().replace(/[^a-z]/g, ""));
  const noIdx = head.findIndex((h) => ["applicationnumber", "applicationno", "appno", "applicationid", "appnumber"].includes(h));
  const reasonIdx = head.findIndex((h) => ["reason", "remarks", "remark", "note"].includes(h));
  const hasHeader = noIdx >= 0;
  const body = hasHeader ? raw.slice(1) : raw;
  const col = hasHeader ? noIdx : 0;
  return {
    hasReason: reasonIdx >= 0,
    rows: body.map((r, i) => ({
      application_number: (r[col] || "").trim(),
      reason: reasonIdx >= 0 ? (r[reasonIdx] || "").trim() : "",
      line: i + (hasHeader ? 2 : 1),
    })),
  };
}

const SAMPLE = "application_number,reason\nMH2600201,\nMH2600202,\nMH2600203,Marks below cut-off\n";

export default function ImportStatusModal({ onClose, onDone }) {
  const [status, setStatus] = useState("SHORTLISTED");
  const [file, setFile] = useState(null);
  const [parsed, setParsed] = useState(null);
  const [reason, setReason] = useState("");
  const [err, setErr] = useState("");
  const [busy, setBusy] = useState(false);
  const [result, setResult] = useState(null);
  const toast = useToast();

  const pickFile = async (e) => {
    const f = e.target.files?.[0];
    setErr(""); setParsed(null); setFile(null);
    if (!f) return;
    if (!/\.csv$/i.test(f.name)) { setErr("Choose a .csv file"); e.target.value = ""; return; }
    if (f.size > MAX_BYTES) { setErr("The file is larger than 1 MB"); e.target.value = ""; return; }
    const txt = await f.text();
    const p = readRows(parseCsv(txt));
    if (!p.rows.length) { setErr("No rows found in this file"); return; }
    setFile(f); setParsed(p);
  };

  const sampleHref = "data:text/csv;charset=utf-8," + encodeURIComponent(SAMPLE);

  const run = async () => {
    setErr("");
    if (!parsed) { setErr("Choose a CSV file first"); return; }
    const missingReasons = status === "REJECTED" && !reason.trim() && parsed.rows.some((r) => !r.reason);
    if (missingReasons) { setErr("Some rows have no reason. Add a default reason below, or fill the reason column in the file."); return; }
    setBusy(true);
    try {
      const r = await api.importStatus({ rows: parsed.rows, to_status: status, default_reason: reason });
      setResult(r);
      toast(`${r.updated} application${r.updated === 1 ? "" : "s"} moved to ${human(status).toLowerCase()}`);
      onDone();
    } catch (x) { setErr(x.message); }
    finally { setBusy(false); }
  };

  return (
    <Modal
      title="Import status from a CSV file"
      onClose={onClose}
      footer={result
        ? <Button variant="primary" onClick={onClose}>Done</Button>
        : <><Button onClick={onClose}>Cancel</Button><Button variant={status === "REJECTED" ? "danger" : "primary"} disabled={busy || !parsed} onClick={run}>{busy ? "Importing…" : `Import as ${human(status).toLowerCase()}`}</Button></>}
    >
      {!result ? (
        <>
          <div className="field">
            <span className="field-label">1. Every application in the file will be moved to</span>
            <div className="seg" role="radiogroup" aria-label="Status to apply">
              {STATUSES.map((s) => (
                <button key={s} type="button" role="radio" aria-checked={status === s} className={status === s ? (s === "REJECTED" ? "on-ABSENT" : s === "SELECTED" ? "on-PRESENT" : "on-EXCUSED") : ""} onClick={() => { setStatus(s); setErr(""); }}>
                  {human(s)}
                </button>
              ))}
            </div>
          </div>
          <Field label="2. CSV file" hint={<>One application number per row (column <span className="mono">application_number</span>). An optional <span className="mono">reason</span> column is saved in the audit trail. <a href={sampleHref} download="status_import_sample.csv">Download a sample</a></>}>
            <input type="file" accept=".csv,text/csv" onChange={pickFile} aria-label="CSV file" />
          </Field>
          {parsed && (
            <div className="result-box" style={{ marginTop: 0, marginBottom: 12 }}>
              <strong>{file.name}</strong>: {parsed.rows.length} row{parsed.rows.length === 1 ? "" : "s"}{parsed.hasReason ? ", with a reason column" : ""}.
              <div className="mono muted" style={{ marginTop: 4 }}>{parsed.rows.slice(0, 4).map((r) => r.application_number || "(blank)").join(", ")}{parsed.rows.length > 4 ? " …" : ""}</div>
            </div>
          )}
          <Field label={status === "REJECTED" ? "Default reason (used where the file has none)" : "Default note (optional)"}>
            <textarea value={reason} onChange={(e) => { setReason(e.target.value); setErr(""); }} placeholder={status === "REJECTED" ? "e.g. Family income above eligibility limit" : "Optional note for the audit trail"} />
          </Field>
          {err && <div className="field-error">{err}</div>}
        </>
      ) : (
        <>
          <p style={{ marginTop: 0 }}>Moved to <Badge value={status} /> from <strong>{file.name}</strong></p>
          <div className="stats" style={{ marginBottom: 8 }}>
            <div className="stat"><span className="stat-label">Updated</span><span className="stat-value">{result.updated}</span></div>
            <div className="stat"><span className="stat-label">Already / duplicate</span><span className="stat-value">{result.skipped}</span></div>
            <div className="stat"><span className="stat-label">Not found</span><span className="stat-value">{result.not_found.length}</span></div>
            <div className="stat"><span className="stat-label">Not allowed</span><span className="stat-value">{result.errors.length}</span></div>
          </div>
          {(result.not_found.length > 0 || result.errors.length > 0) && (
            <div className="result-box">
              <strong>Rows that were not changed:</strong>
              <ul>
                {result.not_found.slice(0, 6).map((r) => <li key={`n${r.line}`}>Line {r.line}: <span className="mono">{r.application_number}</span> not found</li>)}
                {result.errors.slice(0, 6).map((r) => <li key={`e${r.line}`}>Line {r.line}: <span className="mono">{r.application_number}</span>: {r.error}</li>)}
              </ul>
            </div>
          )}
        </>
      )}
    </Modal>
  );
}
