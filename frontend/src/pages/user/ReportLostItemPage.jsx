import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import LocationPicker from "../../components/maps/LocationPicker.jsx";
import { categoryService } from "../../services/categoryService";
import { lostItemService } from "../../services/lostItemService";
import { extractErrorMessage } from "../../services/apiClient";

const today = new Date().toISOString().slice(0, 10);

export default function ReportLostItemPage() {
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState({
    categoryId: "",
    description: "",
    brand: "",
    color: "",
    identifyingDetails: "",
    lostDate: today
  });
  const [location, setLocation] = useState({ city: "" });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    categoryService.getCategories().then(setCategories).catch(() => {});
  }, []);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!location.latitude || !location.longitude) {
      setError("Please pick the approximate location on the map.");
      return;
    }
    if (!location.city) {
      setError("Please enter the city for this location.");
      return;
    }
    setSubmitting(true);
    try {
      const res = await lostItemService.create({ ...form, location });
      navigate(`/lost-items/${res.lostItemId}`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout>
      <PageHeader eyebrow="Citizen · Lost item" title="Report a lost item" />
      <form onSubmit={handleSubmit} className="card p-4" style={{ maxWidth: 720 }}>
        <ErrorAlert message={error} />

        <div className="mb-3">
          <label className="form-label">Category</label>
          <select className="form-select" required value={form.categoryId} onChange={update("categoryId")}>
            <option value="">Select a category…</option>
            {categories.map((c) => (
              <option key={c.categoryId} value={c.categoryId}>
                {c.categoryName}
              </option>
            ))}
          </select>
        </div>

        <div className="mb-3">
          <label className="form-label">Description</label>
          <textarea
            className="form-control"
            rows={3}
            required
            placeholder="What does it look like? Any distinguishing features?"
            value={form.description}
            onChange={update("description")}
          />
        </div>

        <div className="row g-2 mb-3">
          <div className="col-md-6">
            <label className="form-label">Brand (optional)</label>
            <input className="form-control" value={form.brand} onChange={update("brand")} />
          </div>
          <div className="col-md-6">
            <label className="form-label">Color (optional)</label>
            <input className="form-control" value={form.color} onChange={update("color")} />
          </div>
        </div>

        <div className="mb-3">
          <label className="form-label">Identifying details (kept private, shown only to police)</label>
          <textarea
            className="form-control"
            rows={2}
            placeholder="Serial numbers, engravings, unique marks…"
            value={form.identifyingDetails}
            onChange={update("identifyingDetails")}
          />
        </div>

        <div className="mb-3">
          <label className="form-label">Date lost</label>
          <input type="date" className="form-control" required max={today} value={form.lostDate} onChange={update("lostDate")} />
        </div>

        <div className="mb-3">
          <label className="form-label">City</label>
          <input
            className="form-control mb-2"
            required
            placeholder="e.g. Bengaluru"
            value={location.city || ""}
            onChange={(e) => setLocation({ ...location, city: e.target.value })}
          />
          <LocationPicker value={location} onChange={setLocation} />
        </div>

        <button className="btn btn-primary" disabled={submitting}>
          {submitting ? "Submitting…" : "Submit report"}
        </button>
      </form>
    </AppLayout>
  );
}
