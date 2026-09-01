import React, { useEffect, useState } from "react";
import { NavLink, Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext.jsx";
import { notificationService } from "../../services/notificationService";

const USER_NAV = [
  { to: "/dashboard", label: "Dashboard" },
  { to: "/lost-items/report", label: "Report Lost Item" },
  { to: "/found-items/report", label: "Report Found Item" },
  { to: "/search", label: "Search Found Items" },
  { to: "/map", label: "Map" },
  { to: "/claims", label: "My Claims" },
  { to: "/notifications", label: "Notifications" }
];

const POLICE_NAV = [
  { to: "/police/dashboard", label: "Dashboard" },
  { to: "/police/found-items/new", label: "Intake Found Item" },
  { to: "/police/found-items", label: "Inventory & Verification" },
  { to: "/police/claims", label: "Claims Management" },
  { to: "/police/disputes", label: "Disputes" },
  { to: "/police/reports", label: "Reports" },
  { to: "/notifications", label: "Notifications" }
];

const ADMIN_NAV = [
  { to: "/admin/stations", label: "Police Stations" },
  { to: "/admin/categories", label: "Categories" }
];

const SYSTEM_ADMIN_SIDE_NAV = [
  { to: "/admin/stations", label: "Police Stations" },
  { to: "/admin/categories", label: "Categories" },
  { to: "/police/reports", label: "Reports" }
];

export default function AppLayout({ children }) {
  const { user, profile, isAuthenticated, isPolice, isAdmin, logout } = useAuth();
  const navigate = useNavigate();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    if (!isAuthenticated) return;
    let active = true;
    notificationService
      .getMyNotifications(0, 50)
      .then((data) => {
        if (!active) return;
        const count = (data.content || []).filter((n) => !n.isRead).length;
        setUnread(count);
      })
      .catch(() => {});
    return () => {
      active = false;
    };
  }, [isAuthenticated]);

  const navItems = isPolice ? POLICE_NAV : USER_NAV;

  return (
    <div className="app-shell">
      <header className="topbar">
        <div className="container-fluid d-flex align-items-center justify-content-between py-2 px-3">
          <Link to={isAuthenticated ? (isPolice ? "/police/dashboard" : "/dashboard") : "/"} className="brand-mark">
            <span className="brand-crest">LF</span>
            <span>
              Lost &amp; Found Network
              {isPolice && <span className="text-muted-soft fw-normal"> · Police Portal</span>}
            </span>
          </Link>
          {!isAuthenticated ? (
            <div className="d-flex gap-2">
              <Link to="/search" className="btn btn-outline-primary btn-sm d-none d-sm-inline-block">
                Search found items
              </Link>
              <Link to="/login" className="btn btn-outline-primary btn-sm">
                Sign in
              </Link>
              <Link to="/register" className="btn btn-primary btn-sm">
                Create account
              </Link>
            </div>
          ) : (
            <div className="d-flex align-items-center gap-3">
              <Link to="/notifications" className="text-decoration-none text-muted position-relative">
                Notifications
                {unread > 0 && (
                  <span className="badge rounded-pill ms-1" style={{ background: "var(--caution)" }}>
                    {unread}
                  </span>
                )}
              </Link>
              <div className="dropdown">
                <button
                  className="btn btn-outline-primary btn-sm dropdown-toggle"
                  type="button"
                  data-bs-toggle="dropdown"
                >
                  {user?.name || "Account"}
                </button>
                <ul className="dropdown-menu dropdown-menu-end">
                  <li>
                    <span className="dropdown-item-text text-muted-soft" style={{ fontSize: "0.78rem" }}>
                      {profile?.role?.replaceAll("_", " ")}
                    </span>
                  </li>
                  <li>
                    <button className="dropdown-item" onClick={() => navigate("/profile")}>
                      Profile
                    </button>
                  </li>
                  {isAdmin && (
                    <>
                      <li>
                        <hr className="dropdown-divider" />
                      </li>
                      {ADMIN_NAV.map((item) => (
                        <li key={item.to}>
                          <button className="dropdown-item" onClick={() => navigate(item.to)}>
                            {item.label}
                          </button>
                        </li>
                      ))}
                    </>
                  )}
                  <li>
                    <hr className="dropdown-divider" />
                  </li>
                  <li>
                    <button className="dropdown-item text-danger" onClick={logout}>
                      Sign out
                    </button>
                  </li>
                </ul>
              </div>
            </div>
          )}
        </div>
      </header>

      <div className="app-body">
        {isAuthenticated && (
          <nav className="side-rail d-none d-md-block">
            {navItems.map((item) => (
              <NavLink
                key={item.to}
                to={item.to}
                className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}
              >
                {item.label}
              </NavLink>
            ))}
          </nav>
        )}
        <main className="main-content">{children}</main>
      </div>
    </div>
  );
}
