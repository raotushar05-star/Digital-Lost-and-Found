// Maps every backend status vocabulary (case, lost item, found item, claim,
// dispute, evidence, custody) to a stamp visual style + human label.
const STATUS_MAP = {
  // Case / lifecycle statuses
  REPORTED: { label: "Reported", tone: "neutral" },
  RECEIVED: { label: "Received", tone: "progress" },
  POLICE_VERIFIED: { label: "Police Verified", tone: "progress" },
  POTENTIAL_MATCH: { label: "Potential Match", tone: "caution" },
  CLAIM_SUBMITTED: { label: "Claim Submitted", tone: "caution" },
  UNDER_VERIFICATION: { label: "Under Verification", tone: "caution" },
  APPROVED: { label: "Approved", tone: "success" },
  REJECTED: { label: "Rejected", tone: "danger" },
  RETURNED: { label: "Returned", tone: "success" },
  RESOLVED: { label: "Resolved", tone: "success" },
  WITHDRAWN: { label: "Withdrawn", tone: "neutral" },

  // Found report
  SUBMITTED: { label: "Submitted", tone: "neutral" },
  LINKED: { label: "Linked", tone: "progress" },
  DUPLICATE: { label: "Duplicate", tone: "danger" },

  // Found item verification
  PENDING: { label: "Pending", tone: "caution" },
  VERIFIED: { label: "Verified", tone: "success" },

  // Custody
  IN_CUSTODY: { label: "In Custody", tone: "progress" },
  CLAIMED: { label: "Claimed", tone: "caution" },
  TRANSFERRED: { label: "Transferred", tone: "neutral" },

  // Claim
  DISPUTED: { label: "Disputed", tone: "danger" },

  // Dispute
  OPEN: { label: "Open", tone: "caution" },
  UNDER_REVIEW: { label: "Under Review", tone: "caution" },
  CLOSED: { label: "Closed", tone: "neutral" },

  // Evidence
  ACCEPTED: { label: "Accepted", tone: "success" },
  INCONCLUSIVE: { label: "Inconclusive", tone: "neutral" }
};

export function getStatusMeta(status) {
  return STATUS_MAP[status] || { label: status, tone: "neutral" };
}

export const CASE_STAGE_ORDER = [
  "REPORTED",
  "RECEIVED",
  "POLICE_VERIFIED",
  "POTENTIAL_MATCH",
  "CLAIM_SUBMITTED",
  "UNDER_VERIFICATION",
  "APPROVED",
  "RETURNED",
  "RESOLVED"
];
