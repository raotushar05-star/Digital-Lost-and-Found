import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import { userService } from "../../services/userService";
import { extractErrorMessage } from "../../services/apiClient";
import { useAuth } from "../../context/AuthContext.jsx";

export default function ProfilePage() {
  const { refreshProfile } = useAuth();
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [saved, setSaved] = useState(false);

  useEffect(() => {
    userService.getProfile().then(setForm).finally(() => setLoading(false));
  }, []);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSaving(true);
    setSaved(false);
    try {
      const updated = await userService.updateProfile({
        name: form.name,
        email: form.email,
        phone: form.phone,
        address: form.address
      });
      setForm(updated);
      setSaved(true);
      refreshProfile();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  if (loading || !form) return <AppLayout><LoadingSpinner /></AppLayout>;

  return (
    <AppLayout>
      <PageHeader eyebrow="Account" title="Your profile" />
      <form onSubmit={handleSubmit} className="card p-4" style={{ maxWidth: 560 }}>
        <ErrorAlert message={error} />
        {saved && <div className="alert alert-success">Profile updated.</div>}

        <div className="mb-3">
          <label className="form-label">Role</label>
          <div>
            <span className="stamp stamp-neutral">{form.role?.replaceAll("_", " ")}</span>
            {form.stationName && <span className="text-muted-soft ms-2" style={{ fontSize: "0.85rem" }}>{form.stationName}</span>}
          </div>
        </div>
        <div className="mb-3">
          <label className="form-label">Full name</label>
          <input className="form-control" required value={form.name || ""} onChange={update("name")} />
        </div>
        <div className="mb-3">
          <label className="form-label">Email</label>
          <input type="email" className="form-control" value={form.email || ""} onChange={update("email")} />
        </div>
        <div className="mb-3">
          <label className="form-label">Phone</label>
          <input className="form-control" required value={form.phone || ""} onChange={update("phone")} />
        </div>
        <div className="mb-3">
          <label className="form-label">Address</label>
          <textarea className="form-control" rows={2} value={form.address || ""} onChange={update("address")} />
        </div>
        <button className="btn btn-primary" disabled={saving}>
          {saving ? "Saving…" : "Save changes"}
        </button>
      </form>
    </AppLayout>
  );
}
