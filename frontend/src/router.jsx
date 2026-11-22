// Tiny hash router (no extra dependency). URLs look like  #/applications/12
import { useEffect, useState } from "react";

const current = () => window.location.hash.replace(/^#/, "") || "/";

export function useHashPath() {
  const [path, setPath] = useState(current());
  useEffect(() => {
    const on = () => setPath(current());
    window.addEventListener("hashchange", on);
    return () => window.removeEventListener("hashchange", on);
  }, []);
  return path;
}

export function navigate(to) {
  window.location.hash = to;
}

export function matchRoute(pattern, path) {
  const [cleanPath] = path.split("?");
  const p = pattern.split("/").filter(Boolean);
  const a = cleanPath.split("/").filter(Boolean);
  if (p.length !== a.length) return null;
  const params = {};
  for (let i = 0; i < p.length; i++) {
    if (p[i].startsWith(":")) params[p[i].slice(1)] = decodeURIComponent(a[i]);
    else if (p[i] !== a[i]) return null;
  }
  return params;
}

export function queryParams(path) {
  return Object.fromEntries(new URLSearchParams(path.split("?")[1] || ""));
}

export function Link({ to, children, className, ...rest }) {
  return (
    <a href={`#${to}`} className={className} {...rest}>
      {children}
    </a>
  );
}
