import React from "react";
import { Routes, Route } from "react-router-dom";
import ProtectedRoute from "./routes/ProtectedRoute.jsx";

import LandingPage from "./pages/public/LandingPage.jsx";
import SearchPage from "./pages/public/SearchPage.jsx";
import FoundItemPublicDetailPage from "./pages/public/FoundItemPublicDetailPage.jsx";
import MapPage from "./pages/public/MapPage.jsx";

import RegisterPage from "./pages/auth/RegisterPage.jsx";
import LoginPage from "./pages/auth/LoginPage.jsx";

import DashboardPage from "./pages/user/DashboardPage.jsx";
import ReportLostItemPage from "./pages/user/ReportLostItemPage.jsx";
import LostItemDetailPage from "./pages/user/LostItemDetailPage.jsx";
import ReportFoundItemPage from "./pages/user/ReportFoundItemPage.jsx";
import ClaimSubmissionPage from "./pages/user/ClaimSubmissionPage.jsx";
import MyClaimsPage from "./pages/user/MyClaimsPage.jsx";
import ClaimDetailPage from "./pages/user/ClaimDetailPage.jsx";
import NotificationsPage from "./pages/user/NotificationsPage.jsx";
import ProfilePage from "./pages/user/ProfilePage.jsx";
import CaseTrackingPage from "./pages/user/CaseTrackingPage.jsx";

import PoliceDashboardPage from "./pages/police/PoliceDashboardPage.jsx";
import FoundItemIntakePage from "./pages/police/FoundItemIntakePage.jsx";
import FoundItemAdminDetailPage from "./pages/police/FoundItemAdminDetailPage.jsx";
import FoundItemInventoryPage from "./pages/police/FoundItemInventoryPage.jsx";
import ClaimsManagementPage from "./pages/police/ClaimsManagementPage.jsx";
import PoliceClaimDetailPage from "./pages/police/PoliceClaimDetailPage.jsx";
import DisputesPage from "./pages/police/DisputesPage.jsx";
import PoliceReportsPage from "./pages/police/PoliceReportsPage.jsx";

import StationManagementPage from "./pages/admin/StationManagementPage.jsx";
import CategoryManagementPage from "./pages/admin/CategoryManagementPage.jsx";

const POLICE_ROLES = ["POLICE_OFFICER", "POLICE_ADMIN"];
const REPORTS_ROLES = ["POLICE_OFFICER", "POLICE_ADMIN", "SYSTEM_ADMIN"];

export default function App() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/" element={<LandingPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/search" element={<SearchPage />} />
      <Route path="/map" element={<MapPage />} />
      <Route path="/found-items/:id" element={<FoundItemPublicDetailPage />} />

      {/* Citizen */}
      <Route path="/dashboard" element={<ProtectedRoute allowedRoles={["USER"]}><DashboardPage /></ProtectedRoute>} />
      <Route path="/lost-items/report" element={<ProtectedRoute allowedRoles={["USER"]}><ReportLostItemPage /></ProtectedRoute>} />
      <Route path="/lost-items/:id" element={<ProtectedRoute><LostItemDetailPage /></ProtectedRoute>} />
      <Route path="/found-items/report" element={<ProtectedRoute allowedRoles={["USER"]}><ReportFoundItemPage /></ProtectedRoute>} />
      <Route path="/found-items/:foundItemId/claim" element={<ProtectedRoute allowedRoles={["USER"]}><ClaimSubmissionPage /></ProtectedRoute>} />
      <Route path="/claims" element={<ProtectedRoute allowedRoles={["USER"]}><MyClaimsPage /></ProtectedRoute>} />
      <Route path="/claims/:id" element={<ProtectedRoute allowedRoles={["USER"]}><ClaimDetailPage /></ProtectedRoute>} />
      <Route path="/notifications" element={<ProtectedRoute><NotificationsPage /></ProtectedRoute>} />
      <Route path="/profile" element={<ProtectedRoute><ProfilePage /></ProtectedRoute>} />
      <Route path="/cases/:id" element={<ProtectedRoute><CaseTrackingPage /></ProtectedRoute>} />

      {/* Police */}
      <Route path="/police/dashboard" element={<ProtectedRoute allowedRoles={POLICE_ROLES}><PoliceDashboardPage /></ProtectedRoute>} />
      <Route path="/police/found-items/new" element={<ProtectedRoute allowedRoles={POLICE_ROLES}><FoundItemIntakePage /></ProtectedRoute>} />
      <Route path="/police/found-items" element={<ProtectedRoute allowedRoles={POLICE_ROLES}><FoundItemInventoryPage /></ProtectedRoute>} />
      <Route path="/police/found-items/:id" element={<ProtectedRoute allowedRoles={POLICE_ROLES}><FoundItemAdminDetailPage /></ProtectedRoute>} />
      <Route path="/police/claims" element={<ProtectedRoute allowedRoles={POLICE_ROLES}><ClaimsManagementPage /></ProtectedRoute>} />
      <Route path="/police/claims/:id" element={<ProtectedRoute allowedRoles={POLICE_ROLES}><PoliceClaimDetailPage /></ProtectedRoute>} />
      <Route path="/police/disputes" element={<ProtectedRoute allowedRoles={POLICE_ROLES}><DisputesPage /></ProtectedRoute>} />
      <Route path="/police/reports" element={<ProtectedRoute allowedRoles={REPORTS_ROLES}><PoliceReportsPage /></ProtectedRoute>} />

      {/* Admin */}
      <Route path="/admin/stations" element={<ProtectedRoute allowedRoles={["SYSTEM_ADMIN"]}><StationManagementPage /></ProtectedRoute>} />
      <Route path="/admin/categories" element={<ProtectedRoute allowedRoles={["SYSTEM_ADMIN"]}><CategoryManagementPage /></ProtectedRoute>} />

      <Route path="*" element={<LandingPage />} />
    </Routes>
  );
}
