import React, { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import AppLayout from "../../components/common/AppLayout.jsx";
import PageHeader from "../../components/common/PageHeader.jsx";
import LoadingSpinner from "../../components/common/LoadingSpinner.jsx";
import EmptyState from "../../components/common/EmptyState.jsx";
import { searchService } from "../../services/searchService";
import { categoryService } from "../../services/categoryService";
import { formatDate } from "../../utils/format";

export default function SearchPage() {
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({ categoryId: "", city: "", color: "", brand: "", keyword: "" });
  const [results, setResults] = useState(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);

  useEffect(() => {
    categoryService.getCategories().then(setCategories).catch(() => {});
  }, []);

  const runSearch = async (targetPage = 0) => {
    setLoading(true);
    try {
      const params = { page: targetPage, size: 12 };
      Object.entries(filters).forEach(([k, v]) => {
        if (v) params[k] = v;
      });
      const data = await searchService.search(params);
      setResults(data);
      setPage(targetPage);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    runSearch(0);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    runSearch(0);
  };

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
            <select
              className="form-select"
              value={filters.categoryId}
              onChange={(e) => setFilters({ ...filters, categoryId: e.target.value })}
            >
              <option value="">All categories</option>
              {categories.map((c) => (
                <option key={c.categoryId} value={c.categoryId}>
                  {c.categoryName}
                </option>
              ))}
            </select>
          </div>
          <div className="col-md-2">
            <input
              className="form-control"
              placeholder="City"
              value={filters.city}
              onChange={(e) => setFilters({ ...filters, city: e.target.value })}
            />
          </div>
          <div className="col-md-2">
            <input
              className="form-control"
              placeholder="Color"
              value={filters.color}
              onChange={(e) => setFilters({ ...filters, color: e.target.value })}
            />
          </div>
          <div className="col-md-2">
            <input
              className="form-control"
              placeholder="Brand"
              value={filters.brand}
              onChange={(e) => setFilters({ ...filters, brand: e.target.value })}
            />
          </div>
          <div className="col-md-2">
            <input
              className="form-control"
              placeholder="Keyword"
              value={filters.keyword}
              onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            />
          </div>
          <div className="col-md-1">
            <button className="btn btn-primary w-100">Go</button>
          </div>
        </div>
      </form>

      {loading ? (
        <LoadingSpinner />
      ) : !results || results.content.length === 0 ? (
        <EmptyState title="No matching items" message="Try widening your filters or checking back later." />
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
            <button
              className="btn btn-sm btn-outline-primary"
              disabled={page === 0}
              onClick={() => runSearch(page - 1)}
            >
              Previous
            </button>
            <span className="text-muted-soft align-self-center font-mono" style={{ fontSize: "0.8rem" }}>
              Page {page + 1} of {Math.max(results.totalPages, 1)}
            </span>
            <button
              className="btn btn-sm btn-outline-primary"
              disabled={page + 1 >= results.totalPages}
              onClick={() => runSearch(page + 1)}
            >
              Next
            </button>
          </div>
        </>
      )}
    </AppLayout>
  );
}
