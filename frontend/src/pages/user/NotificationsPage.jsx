import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import { notificationService } from "../../services/notificationService";
import { timeAgo } from "../../utils/format";

function linkFor(n) {
  if (n.relatedMatchId) return "/dashboard";
  if (n.relatedCaseId) return `/cases/${n.relatedCaseId}`;
  return null;
}

export default function NotificationsPage() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);

  const load = () => {
    setLoading(true);
    notificationService.getMyNotifications(0, 50).then(setData).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const handleMarkRead = async (id) => {
    await notificationService.markRead(id);
    load();
  };

  const handleMarkAll = async () => {
    await notificationService.markAllRead();
    load();
  };

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Notifications"
        title="Notifications"
        actions={
          <button className="btn btn-sm btn-outline-primary" onClick={handleMarkAll}>
            Mark all as read
          </button>
        }
      />
      {loading ? (
        <LoadingSpinner />
      ) : !data || data.content.length === 0 ? (
        <EmptyState title="You're all caught up" message="New matches and case updates will appear here." />
      ) : (
        <div className="card">
          <div className="list-group list-group-flush">
            {data.content.map((n) => {
              const to = linkFor(n);
              const body = (
                <div
                  className="d-flex justify-content-between align-items-start p-3"
                  style={{ background: n.isRead ? "transparent" : "var(--brand-tint)" }}
                >
                  <div>
                    <div className="fw-medium">{n.title}</div>
                    <div className="text-muted-soft" style={{ fontSize: "0.85rem" }}>{n.message}</div>
                    <div className="font-mono text-muted-soft mt-1" style={{ fontSize: "0.72rem" }}>
                      {timeAgo(n.createdAt)}
                    </div>
                  </div>
                  {!n.isRead && (
                    <button
                      className="btn btn-sm btn-link"
                      onClick={(e) => {
                        e.preventDefault();
                        handleMarkRead(n.notificationId);
                      }}
                    >
                      Mark read
                    </button>
                  )}
                </div>
              );
              return to ? (
                <Link key={n.notificationId} to={to} className="list-group-item list-group-item-action p-0 text-decoration-none">
                  {body}
                </Link>
              ) : (
                <div key={n.notificationId} className="list-group-item p-0">
                  {body}
                </div>
              );
            })}
          </div>
        </div>
      )}
    </AppLayout>
  );
}
