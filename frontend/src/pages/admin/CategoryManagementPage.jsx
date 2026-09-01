import React, { useEffect, useState } from "react";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import StatusStamp from "../../components/common/StatusStamp.jsx";
import { categoryService } from "../../services/categoryService";
import { extractErrorMessage } from "../../services/apiClient";

const emptyForm = { categoryName: "", description: "" };

export default function CategoryManagementPage() {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  const load = () => {
    setLoading(true);
    categoryService.getCategories().then(setCategories).finally(() => setLoading(false));
  };

  useEffect(load, []);

  const startEdit = (cat) => {
    setEditingId(cat.categoryId);
    setForm({ categoryName: cat.categoryName, description: cat.description || "", isActive: cat.isActive });
  };

  const resetForm = () => {
    setEditingId(null);
    setForm(emptyForm);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSubmitting(true);
    try {
      if (editingId) {
        await categoryService.update(editingId, form);
      } else {
        await categoryService.create(form);
      }
      resetForm();
      load();
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout>
      <PageHeader eyebrow="Admin" title="Item categories" />
      <div className="row g-4">
        <div className="col-lg-7">
          {loading ? (
            <LoadingSpinner />
          ) : (
            <div className="card">
              <div className="list-group list-group-flush">
                {categories.map((c) => (
                  <div key={c.categoryId} className="list-group-item d-flex justify-content-between align-items-center p-3">
                    <div>
                      <div className="fw-medium d-flex align-items-center gap-2">
                        {c.categoryName}
                        {!c.isActive && <StatusStamp status="WITHDRAWN" />}
                      </div>
                      <div className="text-muted-soft" style={{ fontSize: "0.8rem" }}>{c.description}</div>
                    </div>
                    <button className="btn btn-sm btn-outline-primary" onClick={() => startEdit(c)}>
                      Edit
                    </button>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
        <div className="col-lg-5">
          <form onSubmit={handleSubmit} className="card p-3">
            <h2 style={{ fontSize: "1rem" }} className="mb-2">{editingId ? "Edit category" : "New category"}</h2>
            <ErrorAlert message={error} />
            <div className="mb-2">
              <label className="form-label">Name</label>
              <input
                className="form-control"
                required
                value={form.categoryName}
                onChange={(e) => setForm({ ...form, categoryName: e.target.value })}
              />
            </div>
            <div className="mb-3">
              <label className="form-label">Description</label>
              <textarea
                className="form-control"
                rows={2}
                value={form.description}
                onChange={(e) => setForm({ ...form, description: e.target.value })}
              />
            </div>
            {editingId && (
              <div className="form-check mb-3">
                <input
                  type="checkbox"
                  className="form-check-input"
                  id="catActive"
                  checked={!!form.isActive}
                  onChange={(e) => setForm({ ...form, isActive: e.target.checked })}
                />
                <label className="form-check-label" htmlFor="catActive">Active</label>
              </div>
            )}
            <div className="d-flex gap-2">
              <button className="btn btn-primary btn-sm" disabled={submitting}>
                {submitting ? "Saving…" : editingId ? "Save changes" : "Create category"}
              </button>
              {editingId && (
                <button type="button" className="btn btn-outline-secondary btn-sm" onClick={resetForm}>
                  Cancel
                </button>
              )}
            </div>
          </form>
        </div>
      </div>
    </AppLayout>
  );
}
