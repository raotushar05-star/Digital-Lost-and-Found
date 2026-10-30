import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import { searchService } from "../../services/searchService";
import { categoryService } from "../../services/categoryService";
import { formatDate } from "../../utils/format";

const emptyFilters = { categoryId: "", city: "", color: "", brand: "", keyword: "" };

export default function SearchPage() {
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);
  const [applied, setApplied] = useState(emptyFilters);
  // Results live only in this component's state, so they are gone as soon as you leave the page.
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(false);
  const [notice, setNotice] = useState(null);
  const [page, setPage] = useState(0);

  useEffect(() => {
    categoryService.getCategories().then(setCategories).catch(() => {});
  }, []);

  const runSearch = async (targetPage, activeFilters) => {
    setLoading(true);
    try {
      const params = { page: targetPage, size: 12 };
      Object.entries(activeFilters).forEach(([k, v]) => {
        if (String(v).trim()) params[k] = String(v).trim();
      });
      const data = await searchService.search(params);
      setResults(data);
      setPage(targetPage);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    const hasFilter = Object.values(filters).some((v) => String(v).trim() !== "");
    if (!hasFilter) {
      setResults(null);
      setNotice("Enter at least one filter to search.");
      return;
    }
    setNotice(null);
    setApplied(filters);
    runSearch(0, filters);
  };

  const set = (field) => (e) => setFilters({ ...filters, [field]: e.target.value });

  return (
    <AppLayout>
      <PageHeader
        eyebrow="Public search"
        title="Search verified found items"
        subtitle="Only items police have physically received and verified appear here."
      />

      <form className="card p-3 mb-4" onSubmit={handleSubmit}>
        <div className="row g-2">
          <div className="col-md-3">
            <select className="form-select" value={filters.categoryId} onChange={set("categoryId")}>
              <option value="">All categories</option>
              {categories.map((c) => (
                <option key={c.categoryId} value={c.categoryId}>{c.categoryName}</option>
              ))}
            </select>
          </div>
          <div className="col-md-2">
            <input className="form-control" placeholder="City" value={filters.city} onChange={set("city")} maxLength={100}/>
          </div>
          <div className="col-md-2">
            <input className="form-control" placeholder="Color" value={filters.color} onChange={set("color")} maxLength={100}/>
          </div>
          <div className="col-md-2">
            <input className="form-control" placeholder="Brand" value={filters.brand} onChange={set("brand")} maxLength={100}/>
          </div>
          <div className="col-md-2">
            <input className="form-control" placeholder="Keyword" value={filters.keyword} onChange={set("keyword")} maxLength={100}/>
          </div>
          <div className="col-md-1">
            <button className="btn btn-primary w-100">Go</button>
          </div>
        </div>
      </form>

      {loading ? (
        <LoadingSpinner />
      ) : results === null ? (
        <EmptyState
          title="Search to see items"
          message={notice || "Choose a category or type a city, colour, brand or keyword, then press Go."}
        />
      ) : results.content.length === 0 ? (
        <EmptyState
          title="No matching items"
          message="Only verified items still in police custody appear here. Try fewer filters."
        />
      ) : (
        <>
          <div className="row g-3">
            {results.content.map((item) => (
              <div className="col-md-4" key={item.foundItemId}>
                <Link to={`/found-items/${item.foundItemId}`} className="text-decoration-none">
                  <div className="card h-100">
                    {item.primaryPhotoUrl ? (
                      <img
                        src={item.primaryPhotoUrl}
                        alt={item.category}
                        style={{ height: 160, objectFit: "cover", borderRadius: "var(--radius) var(--radius) 0 0" }}
                      />
                    ) : (
                      <div
                        style={{ height: 160, background: "var(--paper)", borderRadius: "var(--radius) var(--radius) 0 0" }}
                        className="d-flex align-items-center justify-content-center text-muted-soft"
                      >
                        No photo
                      </div>
                    )}
                    <div className="p-3">
                      <div className="page-eyebrow mb-1">{item.category}</div>
                      <div className="text-ink fw-medium mb-1">{item.description?.slice(0, 70)}</div>
                      <div className="text-muted-soft" style={{ fontSize: "0.8rem" }}>
                        {item.location?.city} · Found {formatDate(item.foundDate)}
                        {item.distanceKm != null && ` · ${item.distanceKm.toFixed(1)} km away`}
                      </div>
                    </div>
                  </div>
                </Link>
              </div>
            ))}
          </div>
          <div className="d-flex justify-content-center gap-2 mt-4">
            <button className="btn btn-sm btn-outline-primary" disabled={page === 0} onClick={() => runSearch(page - 1, applied)}>
              Previous
            </button>
            <span className="text-muted-soft align-self-center font-mono" style={{ fontSize: "0.8rem" }}>
              Page {page + 1} of {Math.max(results.totalPages, 1)}
            </span>
            <button
              className="btn btn-sm btn-outline-primary"
              disabled={page + 1 >= results.totalPages}
              onClick={() => runSearch(page + 1, applied)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </AppLayout>
  );
}