import React from "react";
import { Link } from "react-router-dom";
import AppLayout from "../components/common/AppLayout.jsx";

export default function NotFoundPage() {
  return (
    <AppLayout>
      <div className="empty-state">
        <h5 className="font-display mb-2">Page not found</h5>
        <p className="mb-3">The page you are looking for does not exist or has moved.</p>
        <Link to="/" className="btn btn-primary btn-sm">Go to home</Link>
      </div>
    </AppLayout>
  );
}