import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import { adminService } from "../../services/adminService";
import { extractErrorMessage } from "../../services/apiClient";

export default function UserManagementPage() {
  const [q, setQ] = useState("");
  const [role, setRole] = useState("");
  const [applied, setApplied] = useState({ q: "", role: "" });
  const [data, setData] = useState(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const load = (targetPage, filters) => {
    setLoading(true);
    setError(null);
    const params = { page: targetPage, size: 25 };
    if (filters.q) params.q = filters.q;
    if (filters.role) params.role = filters.role;
    adminService
      .getUsers(params)
      .then((res) => {
        setData(res);
        setPage(targetPage);
      })
      .catch((err) => setError(extractErrorMessage(err)))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    load(0, applied);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    const filters = { q: q.trim(), role };
    setApplied(filters);
    load(0, filters);
  };

  const toggleActive = async (user) => {
    const verb = user.isActive ? "Deactivate" : "Activate";
    if (!window.confirm(`${verb} ${user.name}?`)) return;
    try {
      await adminService.setUserActive(user.userId, !user.isActive);
      load(page, applied);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  const unlock = async (user) => {
    try {
      await adminService.unlockUser(user.userId);
      load(page, applied);
    } catch (err) {
      setError(extractErrorMessage(err));
    }
  };

  return (
    <AppLayout>
      <PageHeader
        eyebrow="System administration"
        title="Users"
        subtitle="Deactivate an account to block sign-in immediately. System administrator accounts are protected."
        actions={
          <form className="d-flex flex-wrap gap-2 align-items-end" onSubmit={handleSubmit}>
            <div>
              <label htmlFor="user-q" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>Search</label>
              <input id="user-q" className="form-control form-control-sm" placeholder="Name, email or phone" value={q} onChange={(e) => setQ(e.target.value)} />
            </div>
            <div>
              <label htmlFor="user-role" className="form-label mb-1" style={{ fontSize: "0.75rem" }}>Role</label>
              <select id="user-role" className="form-select form-select-sm" value={role} onChange={(e) => setRole(e.target.value)}>
                <option value="">All roles</option>
                <option value="USER">Citizen</option>
                <option value="POLICE_OFFICER">Station Officer</option>
                <option value="POLICE_ADMIN">Station Admin</option>
                <option value="SYSTEM_ADMIN">System Admin</option>
              </select>
            </div>
            <button className="btn btn-sm btn-primary">Search</button>
          </form>
        }
      />
      {error && <ErrorAlert message={error} />}
      {loading ? (
        <LoadingSpinner />
      ) : !data || data.content.length === 0 ? (
        <EmptyState title="No users found" message="Try a different search or role." />
      ) : (
        <>
          <div className="card">
            <div className="table-responsive">
              <table className="table table-sm align-middle mb-0">
                <thead>
                  <tr className="text-muted-soft" style={{ fontSize: "0.78rem" }}>
                    <th className="ps-3">Name</th>
                    <th>Contact</th>
                    <th>Role</th>
                    <th>Station</th>
                    <th>Status</th>
                    <th />
                  </tr>
                </thead>
                <tbody>
                  {data.content.map((u) => (
                    <tr key={u.userId} style={{ fontSize: "0.85rem" }}>
                      <td className="ps-3 fw-medium">{u.name}</td>
                      <td>
                        <div>{u.email || "—"}</div>
                        <div className="text-muted-soft">{u.phone}</div>
                      </td>
                      <td>{u.role.replaceAll("_", " ")}</td>
                      <td>{u.stationName || "—"}</td>
                      <td>
                        <span className={`stamp ${u.isActive ? "stamp-success" : "stamp-danger"}`}>
                          {u.isActive ? "Active" : "Deactivated"}
                        </span>
                        {u.locked && <span className="stamp stamp-caution ms-1">Locked</span>}
                      </td>
                      <td className="text-end pe-3">
                        {u.role !== "SYSTEM_ADMIN" && (
                          <div className="d-flex gap-2 justify-content-end">
                            {u.locked && (
                              <button className="btn btn-sm btn-outline-secondary" onClick={() => unlock(u)}>Unlock</button>
                            )}
                            <button
                              className={`btn btn-sm ${u.isActive ? "btn-outline-danger" : "btn-outline-primary"}`}
                              onClick={() => toggleActive(u)}
                            >
                              {u.isActive ? "Deactivate" : "Activate"}
                            </button>
                          </div>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
          <div className="d-flex justify-content-center gap-2 mt-3">
            <button className="btn btn-sm btn-outline-primary" disabled={page === 0} onClick={() => load(page - 1, applied)}>Previous</button>
            <span className="text-muted-soft align-self-center font-mono" style={{ fontSize: "0.8rem" }}>
              Page {page + 1} of {Math.max(data.totalPages, 1)}
            </span>
            <button className="btn btn-sm btn-outline-primary" disabled={page + 1 >= data.totalPages} onClick={() => load(page + 1, applied)}>Next</button>
          </div>
        </>
      )}
    </AppLayout>
  );
}