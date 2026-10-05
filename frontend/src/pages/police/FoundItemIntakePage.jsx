import React, { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import ErrorAlert from "../../components/common/ErrorAlert.jsx";
import LocationPicker from "../../components/maps/LocationPicker.jsx";
import { categoryService } from "../../services/categoryService";
import { stationService } from "../../services/stationService";
import { policeService } from "../../services/policeService";
import { foundReportService } from "../../services/foundReportService";
import { useAuth } from "../../context/AuthContext.jsx";
import { extractErrorMessage } from "../../services/apiClient";

const today = new Date().toISOString().slice(0, 10);

export default function FoundItemIntakePage() {
  const navigate = useNavigate();
  const { profile } = useAuth();
  const [searchParams, setSearchParams] = useSearchParams();
  const foundReportId = searchParams.get("foundReportId");

  const [categories, setCategories] = useState([]);
  const [stations, setStations] = useState([]);
  const [pendingReports, setPendingReports] = useState([]);
  const [reportLoadError, setReportLoadError] = useState(null);
  const [linkedReport, setLinkedReport] = useState(null);
  const [form, setForm] = useState({
    stationId: "",
    categoryId: "",
    description: "",
    brand: "",
    color: "",
    privateIdentifyingDetails: "",
    foundDate: today
  });
  const [location, setLocation] = useState({ city: "" });
  const [error, setError] = useState(null);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    categoryService.getCategories().then(setCategories).catch(() => {});
    stationService.getStations().then(setStations).catch(() => {});
    policeService.getSubmittedFoundReports()
      .then(setPendingReports)
      .catch((err) => setReportLoadError(extractErrorMessage(err)));
  }, []);

  useEffect(() => {
    if (profile?.stationId) {
      setForm((f) => ({ ...f, stationId: profile.stationId }));
    }
  }, [profile]);

  // Fetch the citizen's found_report exactly once when a foundReportId is present.
  useEffect(() => {
    if (!foundReportId) {
      setLinkedReport(null);
      return;
    }
    setLinkedReport(null);
    foundReportService.getById(foundReportId)
      .then(setLinkedReport)
      .catch((err) => setError(extractErrorMessage(err)));
  }, [foundReportId]);

  // Pre-fill the intake form once both the report and the category list are loaded,
  // so the category name from the report can be matched to a categoryId.
  useEffect(() => {
    if (!linkedReport) return;
    const matchedCategory = categories.find((c) => c.categoryName === linkedReport.category);
    setForm((f) => ({
      ...f,
      categoryId: matchedCategory ? matchedCategory.categoryId : f.categoryId,
      description: linkedReport.description || f.description,
      brand: linkedReport.brand || f.brand,
      color: linkedReport.color || f.color,
      foundDate: linkedReport.foundDate || f.foundDate
    }));
    if (linkedReport.location) setLocation(linkedReport.location);
  }, [linkedReport, categories]);

  const update = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    if (!location.latitude || !location.longitude || !location.city) {
      setError("Please enter the city and pick the location on the map.");
      return;
    }
    setSubmitting(true);
    try {
      const res = await policeService.intakeFoundItem({
        ...form,
        foundReportId: foundReportId || null,
        location
      });
      navigate(`/police/found-items/${res.foundItemId}`);
    } catch (err) {
      setError(extractErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Police · Custody intake"
        title="Register a found item"
        subtitle="Only an authorized officer may create the official custody record."
      />

      <div className="mb-3" style={{ maxWidth: 760 }}>
        <label className="form-label">Finder report to review</label>
        <select
          className="form-select"
          value={foundReportId || ""}
          onChange={(event) => setSearchParams(event.target.value ? { foundReportId: event.target.value } : {})}
        >
          <option value="">Direct intake without a citizen report</option>
          {pendingReports.map((report) => (
            <option key={report.foundReportId} value={report.foundReportId}>
              {report.category} · {report.location?.city || "City not provided"} · {report.foundDate}
            </option>
          ))}
        </select>
        {reportLoadError ? (
          <div className="mt-2"><ErrorAlert message={reportLoadError} /></div>
        ) : pendingReports.length === 0 ? (
          <div className="text-muted-soft mt-2" style={{ fontSize: "0.82rem" }}>
            No new finder reports are waiting for intake.
          </div>
        ) : null}
      </div>

      {linkedReport && (
        <div className="card p-3 mb-3" style={{ maxWidth: 760 }}>
          <div className="page-eyebrow mb-2">Finder's submitted report</div>
          <div><strong>Category:</strong> {linkedReport.category}</div>
          <div><strong>Description:</strong> {linkedReport.description}</div>
          <div>
            <strong>Found:</strong> {linkedReport.foundDate} · <strong>City:</strong> {linkedReport.location?.city || "Not provided"}
          </div>
          {(linkedReport.brand || linkedReport.color) && (
            <div>
              {linkedReport.brand && <><strong>Brand:</strong> {linkedReport.brand} </>}
              {linkedReport.color && <><strong>Color:</strong> {linkedReport.color}</>}
            </div>
          )}
          <div className="text-muted-soft font-mono mt-1" style={{ fontSize: "0.72rem" }}>
            Reference: {linkedReport.foundReportId}
          </div>
          {linkedReport.photos?.length > 0 && (
            <>
              <p className="text-muted-soft mb-2 mt-3" style={{ fontSize: "0.82rem" }}>
                Submitted photo for comparison before registering the item.
              </p>
              <div className="d-flex gap-2 flex-wrap">
                {linkedReport.photos.map((photo) => (
                  <img
                    key={photo.photoId}
                    src={photo.fileUrl}
                    alt="Found report submission"
                    style={{ width: 140, height: 140, objectFit: "cover", borderRadius: "var(--radius-sm)", border: "1px solid var(--line)" }}
                  />
                ))}
              </div>
            </>
          )}
        </div>
      )}

      <form onSubmit={handleSubmit} className="card p-4" style={{ maxWidth: 760 }}>
        <ErrorAlert message={error} />

        <div className="row g-2 mb-3">
          <div className="col-md-6">
            <label className="form-label">Station</label>
            <select className="form-select" required value={form.stationId} onChange={update("stationId")}>
              <option value="">Select station…</option>
              {stations.map((s) => (
                <option key={s.stationId} value={s.stationId}>
                  {s.stationName}
                </option>
              ))}
            </select>
          </div>
          <div className="col-md-6">
            <label className="form-label">Category</label>
            <select className="form-select" required value={form.categoryId} onChange={update("categoryId")}>
              <option value="">Select category…</option>
              {categories.map((c) => (
                <option key={c.categoryId} value={c.categoryId}>
                  {c.categoryName}
                </option>
              ))}
            </select>
          </div>
        </div>

        <div className="mb-3">
          <label className="form-label">Description</label>
          <textarea className="form-control" rows={3} required value={form.description} onChange={update("description")} />
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
          <label className="form-label">Private identifying details</label>
          <textarea
            className="form-control"
            rows={2}
            placeholder="Restricted fields used only for ownership verification — never shown publicly"
            value={form.privateIdentifyingDetails}
            onChange={update("privateIdentifyingDetails")}
          />
        </div>

        <div className="mb-3">
          <label className="form-label">Date found</label>
          <input type="date" className="form-control" required max={today} value={form.foundDate} onChange={update("foundDate")} />
        </div>

        <div className="mb-3">
          <label className="form-label">City</label>
          <input
            className="form-control mb-2"
            required
            value={location.city || ""}
            onChange={(e) => setLocation({ ...location, city: e.target.value })}
          />
          <LocationPicker value={location} onChange={setLocation} />
        </div>

        <button className="btn btn-primary" disabled={submitting}>
          {submitting ? "Registering…" : "Register item"}
        </button>
      </form>
    </AppLayout>
  );
}
