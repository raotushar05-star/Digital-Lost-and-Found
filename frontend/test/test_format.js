import { describe, it, expect } from "./testRunner.js";
import { formatDate, formatDateTime, timeAgo } from "../src/utils/format.js";

describe("format.js - formatDate", () => {
  it("should return '—' for null/undefined", () => {
    expect(formatDate(null)).toBe("—");
    expect(formatDate(undefined)).toBe("—");
  });

  it("should format valid date string", () => {
    const result = formatDate("2024-08-31T00:00:00Z");
    expect(result).toBeTruthy();
  });

  it("should return original value for invalid dates", () => {
    expect(formatDate("invalid-date")).toBe("invalid-date");
  });
});

describe("format.js - formatDateTime", () => {
  it("should return '—' for null/undefined", () => {
    expect(formatDateTime(null)).toBe("—");
    expect(formatDateTime(undefined)).toBe("—");
  });

  it("should format valid datetime string", () => {
    const result = formatDateTime("2024-08-31T14:30:00Z");
    expect(result).toBeTruthy();
  });
});

describe("format.js - timeAgo", () => {
  it("should return empty string for null/undefined", () => {
    expect(timeAgo(null)).toBe("");
    expect(timeAgo(undefined)).toBe("");
  });

  it("should return 'just now' for very recent times", () => {
    const now = new Date();
    expect(timeAgo(now.toISOString())).toBe("just now");
  });

  it("should return minutes ago for recent times", () => {
    const fiveMinutesAgo = new Date(Date.now() - 5 * 60000);
    const result = timeAgo(fiveMinutesAgo.toISOString());
    expect(result).toContain("m ago");
  });

  it("should return hours ago for older times", () => {
    const twoHoursAgo = new Date(Date.now() - 2 * 60 * 60000);
    const result = timeAgo(twoHoursAgo.toISOString());
    expect(result).toContain("h ago");
  });

  it("should return days ago for very old times", () => {
    const threeDaysAgo = new Date(Date.now() - 3 * 24 * 60 * 60000);
    const result = timeAgo(threeDaysAgo.toISOString());
    expect(result).toContain("d ago");
  });
});
