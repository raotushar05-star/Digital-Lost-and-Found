package com.lostandfound.entity.enums;

/**
 * Mirrors the UI Status System (05_UI_Specification.docx, Section 30) and the
 * typical status progression in 03_Database_Specification.docx (case_status_history).
 */
public enum CaseStatus {
    REPORTED,
    RECEIVED,
    POLICE_VERIFIED,
    POTENTIAL_MATCH,
    CLAIM_SUBMITTED,
    UNDER_VERIFICATION,
    APPROVED,
    REJECTED,
    RETURNED,
    RESOLVED
}
