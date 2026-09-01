import { describe, it, expect } from "./testRunner.js";
import { getStatusMeta } from "../src/utils/statusConfig.js";

describe("statusConfig.js - getStatusMeta", () => {
  it("should return correct metadata for REPORTED status", () => {
    const result = getStatusMeta("REPORTED");
    expect(result.label).toBe("Reported");
    expect(result.tone).toBe("neutral");
  });

  it("should return correct metadata for APPROVED status", () => {
    const result = getStatusMeta("APPROVED");
    expect(result.label).toBe("Approved");
    expect(result.tone).toBe("success");
  });

  it("should return correct metadata for REJECTED status", () => {
    const result = getStatusMeta("REJECTED");
    expect(result.label).toBe("Rejected");
    expect(result.tone).toBe("danger");
  });

  it("should return neutral tone for unknown status", () => {
    const result = getStatusMeta("UNKNOWN_STATUS");
    expect(result.tone).toBe("neutral");
  });

  it("should return the status as label for unknown status", () => {
    const result = getStatusMeta("UNKNOWN_STATUS");
    expect(result.label).toBe("UNKNOWN_STATUS");
  });

  it("should return correct metadata for VERIFIED status", () => {
    const result = getStatusMeta("VERIFIED");
    expect(result.tone).toBe("success");
  });

  it("should return correct metadata for PENDING status", () => {
    const result = getStatusMeta("PENDING");
    expect(result.tone).toBe("caution");
  });
});
