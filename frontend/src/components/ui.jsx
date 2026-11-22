import { createContext, useCallback, useContext, useEffect, useState } from "react";

// ---------- data loading ----------
export function useAsync(fn, deps = []) {
  const [state, setState] = useState({ loading: true, data: null, error: null });
  const run = useCallback(() => {
    setState((s) => ({ ...s, loading: true, error: null }));
    return fn()
      .then((data) => setState({ loading: false, data, error: null }))
      .catch((error) => setState({ loading: false, data: null, error }));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, deps);
  useEffect(() => { run(); }, [run]);
  return { ...state, reload: run };
}

// ---------- toasts ----------
const ToastCtx = createContext(() => {});
export function ToastProvider({ children }) {
  const [items, setItems] = useState([]);
  const push = useCallback((msg, kind = "ok") => {
    const id = Math.random();
    setItems((x) => [...x, { id, msg, kind }]);
    setTimeout(() => setItems((x) => x.filter((t) => t.id !== id)), 3500);
  }, []);
  return (
    <ToastCtx.Provider value={push}>
      {children}
      <div className="toasts" role="status" aria-live="polite">
        {items.map((t) => <div key={t.id} className={`toast toast-${t.kind}`}>{t.msg}</div>)}
      </div>
    </ToastCtx.Provider>
  );
}
export const useToast = () => useContext(ToastCtx);

// ---------- layout bits ----------
export function PageHeader({ title, subtitle, actions }) {
  return (
    <div className="page-header">
      <div>
        <h1>{title}</h1>
        {subtitle && <p className="muted">{subtitle}</p>}
      </div>
      {actions && <div className="row gap">{actions}</div>}
    </div>
  );
}

export const Card = ({ title, actions, children, className = "" }) => (
  <section className={`card ${className}`}>
    {(title || actions) && (
      <div className="card-head">
        {title && <h2>{title}</h2>}
        {actions}
      </div>
    )}
    {children}
  </section>
);

export const Stat = ({ label, value, hint, onClick }) => (
  <button type="button" className={`stat ${onClick ? "clickable" : ""}`} onClick={onClick} disabled={!onClick}>
    <span className="stat-label">{label}</span>
    <span className="stat-value">{value}</span>
    {hint && <span className="stat-hint">{hint}</span>}
  </button>
);

const BADGE = {
  DRAFT: "gray", SUBMITTED: "blue", UNDER_REVIEW: "purple", SHORTLISTED: "amber", SELECTED: "green", REJECTED: "red",
  OPEN: "green", CLOSED: "gray", UPCOMING: "blue",
  ACTIVE: "green", GRADUATED: "purple", DROPPED: "red", INACTIVE: "gray",
  PENDING: "amber", APPROVED: "green",
  PRESENT: "green", ABSENT: "red", EXCUSED: "amber",
  UPLOADED: "green", APPLICANT: "blue", STUDENT: "green", ALUMNUS: "purple", STAFF: "gray",
  SELF_REPORTED: "gray", REFERRAL: "blue", PLACEMENT: "green",
};
export const Badge = ({ value }) => value ? <span className={`badge badge-${BADGE[value] || "gray"}`}>{String(value).replace(/_/g, " ").toLowerCase()}</span> : <span className="muted">-</span>;

export function Button({ variant = "default", ...props }) {
  return <button type="button" className={`btn btn-${variant}`} {...props} />;
}

export function Field({ label, error, children, hint }) {
  return (
    <label className="field">
      <span className="field-label">{label}</span>
      {children}
      {hint && !error && <span className="field-hint">{hint}</span>}
      {error && <span className="field-error">{error}</span>}
    </label>
  );
}

export function Modal({ title, onClose, children, footer }) {
  useEffect(() => {
    const k = (e) => e.key === "Escape" && onClose();
    window.addEventListener("keydown", k);
    return () => window.removeEventListener("keydown", k);
  }, [onClose]);
  return (
    <div className="modal-backdrop" onMouseDown={(e) => e.target === e.currentTarget && onClose()}>
      <div className="modal" role="dialog" aria-modal="true" aria-label={title}>
        <div className="modal-head">
          <h2>{title}</h2>
          <button className="icon-btn" onClick={onClose} aria-label="Close">×</button>
        </div>
        <div className="modal-body">{children}</div>
        {footer && <div className="modal-foot">{footer}</div>}
      </div>
    </div>
  );
}

export function Table({ columns, rows, rowKey = "id", empty = "Nothing here yet", onRowClick, selectable, selected, onToggle, onToggleAll }) {
  if (!rows?.length) return <div className="empty">{empty}</div>;
  const allOn = selectable && rows.every((r) => selected.has(r[rowKey]));
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            {selectable && (
              <th className="check"><input type="checkbox" aria-label="Select all on page" checked={allOn} onChange={() => onToggleAll(rows, !allOn)} /></th>
            )}
            {columns.map((c) => <th key={c.key} style={{ width: c.width, textAlign: c.align }}>{c.label}</th>)}
          </tr>
        </thead>
        <tbody>
          {rows.map((r) => (
            <tr key={r[rowKey]} className={onRowClick ? "clickable" : ""} onClick={() => onRowClick?.(r)}>
              {selectable && (
                <td className="check" onClick={(e) => e.stopPropagation()}>
                  <input type="checkbox" aria-label={`Select row ${r[rowKey]}`} checked={selected.has(r[rowKey])} onChange={() => onToggle(r[rowKey])} />
                </td>
              )}
              {columns.map((c) => <td key={c.key} style={{ textAlign: c.align }}>{c.render ? c.render(r) : r[c.key] ?? "-"}</td>)}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function Pagination({ page, pageSize, total, onPage }) {
  const pages = Math.max(1, Math.ceil(total / pageSize));
  return (
    <div className="pagination">
      <span className="muted">{total === 0 ? "0 results" : `${(page - 1) * pageSize + 1}–${Math.min(page * pageSize, total)} of ${total}`}</span>
      <div className="row gap-sm">
        <Button disabled={page <= 1} onClick={() => onPage(page - 1)}>Previous</Button>
        <span className="muted">Page {page} of {pages}</span>
        <Button disabled={page >= pages} onClick={() => onPage(page + 1)}>Next</Button>
      </div>
    </div>
  );
}

export const Loading = () => <div className="loading" aria-busy="true">Loading…</div>;
export const ErrorBox = ({ error, onRetry }) => (
  <div className="error-box">
    <strong>Something went wrong.</strong> {error?.message}
    {onRetry && <Button onClick={onRetry}>Try again</Button>}
  </div>
);

export const fmtDate = (v) => (v ? new Date(v).toLocaleDateString("en-IN", { day: "2-digit", month: "short", year: "numeric" }) : "-");
export const fmtDateTime = (v) => (v ? new Date(v).toLocaleString("en-IN", { day: "2-digit", month: "short", year: "numeric", hour: "2-digit", minute: "2-digit" }) : "-");
export const fmtSize = (b) => (b > 1048576 ? `${(b / 1048576).toFixed(1)} MB` : `${Math.round(b / 1024)} KB`);
export const human = (s) => String(s || "").replace(/_/g, " ").toLowerCase().replace(/^\w/, (c) => c.toUpperCase());
