import { formatDate, formatDateTime } from "./format";

const MARGIN = 42;
const BRAND = [25, 101, 95];
const EMPTY = "—";

function text(value) {
  if (value == null || value === "") return EMPTY;
  return String(value);
}

function locationLine(occurrence) {
  if (!occurrence) return EMPTY;
  return [occurrence.addressText, occurrence.locality, occurrence.city, occurrence.state, occurrence.postalCode]
    .filter(Boolean)
    .join(", ") || EMPTY;
}

function kvRows(pairs) {
  return pairs.map(([label, value]) => [label, text(value)]);
}

async function loadPdf() {
  const [{ jsPDF }, { default: autoTable }] = await Promise.all([
    import("jspdf"),
    import("jspdf-autotable")
  ]);
  return { jsPDF, autoTable };
}

function addHeader(doc, title, subtitle) {
  const pageWidth = doc.internal.pageSize.getWidth();
  doc.setFillColor(...BRAND);
  doc.rect(0, 0, pageWidth, 28, "F");
  doc.setTextColor(255, 255, 255);
  doc.setFont("helvetica", "bold");
  doc.setFontSize(11);
  doc.text("FindLine  ·  Digital Lost & Found  ·  Police case diary", MARGIN, 18);
  doc.setTextColor(20, 20, 20);
  doc.setFontSize(16);
  doc.text(title, MARGIN, 52);
  doc.setFont("helvetica", "normal");
  doc.setFontSize(9);
  if (subtitle) doc.text(subtitle, MARGIN, 68);
}

function addFooters(doc) {
  const pageWidth = doc.internal.pageSize.getWidth();
  const pageCount = doc.internal.getNumberOfPages();
  for (let page = 1; page <= pageCount; page += 1) {
    doc.setPage(page);
    doc.setFont("helvetica", "normal");
    doc.setFontSize(8);
    doc.setTextColor(90, 90, 90);
    doc.text("CONFIDENTIAL — For official police use. Not a substitute for a registered FIR unless so directed.", MARGIN, 820);
    doc.text(`Page ${page} of ${pageCount}`, pageWidth - MARGIN, 820, { align: "right" });
  }
}

function table(doc, autoTable, startY, head, body, title) {
  let y = startY;
  if (title) {
    if (y > 740) {
      doc.addPage();
      y = 48;
    }
    doc.setFont("helvetica", "bold");
    doc.setFontSize(11);
    doc.setTextColor(25, 101, 95);
    doc.text(title, MARGIN, y);
    y += 8;
    doc.setTextColor(20, 20, 20);
  }
  autoTable(doc, {
    startY: y,
    head: [head],
    body: body.length ? body : [[EMPTY, EMPTY]],
    margin: { left: MARGIN, right: MARGIN },
    headStyles: { fillColor: BRAND, fontSize: 8 },
    styles: { font: "helvetica", fontSize: 8, cellPadding: 4, overflow: "linebreak" },
    columnStyles: head.length === 2 ? { 0: { cellWidth: 150, fontStyle: "bold" } } : {}
  });
  return (doc.lastAutoTable?.finalY || y) + 16;
}

function drawCase(doc, autoTable, caseFile, { firstPage } = { firstPage: true }) {
  if (!firstPage) doc.addPage();
  const station = caseFile.station || {};
  const person = caseFile.complainant || {};
  const occurrence = caseFile.occurrence || {};
  const property = caseFile.property || {};
  const custody = caseFile.custody || {};
  const subtitle = [
    caseFile.caseType === "LOST" ? "Lost property report" : "Found property / seizure memo",
    station.stationName,
    `Status: ${text(caseFile.currentStatus)}`
  ].filter(Boolean).join("  ·  ");

  addHeader(doc, `Case ${text(caseFile.caseNumber)}`, subtitle);

  let y = 86;
  y = table(doc, autoTable, y, ["Field", "Particulars"], kvRows([
    ["Case number", caseFile.caseNumber],
    ["Case type", caseFile.caseType],
    ["Current status", caseFile.currentStatus],
    ["Registered at", formatDateTime(caseFile.registeredAt)],
    ["Last updated", formatDateTime(caseFile.lastUpdatedAt)],
    ["Investigating station", station.stationName],
    ["Station code", station.stationCode],
    ["Station address", [station.address, station.city].filter(Boolean).join(", ")],
    ["Station phone", station.phone]
  ]), "1. Registration & station");

  y = table(doc, autoTable, y, ["Field", "Particulars"], kvRows([
    ["Capacity", person.roleLabel],
    ["Name", person.name],
    ["Phone", person.phone],
    ["Email", person.email],
    ["Address", person.address]
  ]), "2. Complainant / informant");

  y = table(doc, autoTable, y, ["Field", "Particulars"], kvRows([
    ["Date of occurrence", formatDate(occurrence.occurrenceDate)],
    ["Place of occurrence", locationLine(occurrence)],
    ["Locality / city", [occurrence.locality, occurrence.city].filter(Boolean).join(", ")],
    ["State / PIN", [occurrence.state, occurrence.postalCode].filter(Boolean).join(" / ")],
    ["Coordinates", occurrence.latitude != null ? `${occurrence.latitude}, ${occurrence.longitude}` : EMPTY]
  ]), "3. Place and time of occurrence");

  y = table(doc, autoTable, y, ["Field", "Particulars"], kvRows([
    ["Category of property", property.category],
    ["Description", property.description],
    ["Brand", property.brand],
    ["Colour", property.color],
    ["Identifying marks / IMEI / serial", property.identifyingDetails],
    ["Item status", property.itemStatus],
    ["Property ID", property.itemId],
    ["Photographs on file", (property.photoUrls || []).length]
  ]), "4. Description of property");

  if (custody.custodyStatus || custody.finder) {
    const finder = custody.finder || {};
    y = table(doc, autoTable, y, ["Field", "Particulars"], kvRows([
      ["Custody status", custody.custodyStatus],
      ["Verification status", custody.verificationStatus],
      ["Received in station", formatDateTime(custody.receivedAt)],
      ["Linked found-report status", custody.foundReportStatus],
      ["Finder name", finder.name],
      ["Finder phone", finder.phone],
      ["Finder email", finder.email]
    ]), "5. Custody / malkhana");
  }

  const matches = caseFile.matches || [];
  y = table(
    doc,
    autoTable,
    y,
    ["Score", "Status", "Counterpart", "Reason"],
    matches.length
      ? matches.map((m) => [
          m.matchScore != null ? `${m.matchScore}%` : EMPTY,
          text(m.status),
          `${text(m.counterpartType)} · ${text(m.counterpartCategory)} · ${text(m.counterpartDescription)}`,
          text(m.matchReason)
        ])
      : [["No system matches recorded", "", "", ""]],
    "6. Matching / investigation leads"
  );

  const claims = caseFile.claims || [];
  y = table(
    doc,
    autoTable,
    y,
    ["Claimant", "Contact", "Status", "Details / review"],
    claims.length
      ? claims.map((cl) => [
          text(cl.claimantName),
          [cl.claimantPhone, cl.claimantEmail].filter(Boolean).join(" · ") || EMPTY,
          text(cl.status),
          [
            cl.claimDetails,
            cl.reviewedByName ? `Reviewed by ${cl.reviewedByName} on ${formatDateTime(cl.reviewedAt)}` : null,
            (cl.evidence || []).length ? `Evidence: ${(cl.evidence || []).map((e) => e.evidenceType || e.description).join("; ")}` : null
          ].filter(Boolean).join(" | ")
        ])
      : [["No claims filed", "", "", ""]],
    "7. Claims and ownership verification"
  );

  const disputes = caseFile.disputes || [];
  y = table(
    doc,
    autoTable,
    y,
    ["Raised by", "Status", "Reason / resolution"],
    disputes.length
      ? disputes.map((d) => [
          text(d.raisedByName),
          text(d.status),
          [d.reason, d.resolution ? `Resolution (${formatDateTime(d.resolvedAt)} by ${text(d.resolvedByName)}): ${d.resolution}` : null]
            .filter(Boolean)
            .join(" | ")
        ])
      : [["No disputes", "", ""]],
    "8. Disputes"
  );

  const handover = caseFile.handover;
  y = table(doc, autoTable, y, ["Field", "Particulars"], handover ? kvRows([
    ["Handover date", formatDateTime(handover.handoverDate)],
    ["Recipient", handover.recipientName],
    ["Recipient phone", handover.recipientPhone],
    ["Duty officer", handover.officerName],
    ["Acknowledgement / receipt no.", handover.acknowledgementReference],
    ["Notes", handover.notes]
  ]) : [["Status", "Property not yet handed over"]], "9. Handover / property receipt");

  const diary = caseFile.caseDiary || [];
  table(
    doc,
    autoTable,
    y,
    ["Date / time", "From → To", "Officer", "Remarks"],
    diary.length
      ? diary.map((entry) => [
          formatDateTime(entry.changedAt),
          `${text(entry.oldStatus)} → ${text(entry.newStatus)}`,
          [entry.recordedByName, entry.recordedByRole].filter(Boolean).join(" · ") || EMPTY,
          text(entry.remarks)
        ])
      : [["No case diary entries", "", "", ""]],
    "10. Case diary (status history)"
  );
}

function drawSummaryCover(doc, autoTable, { summary, stationReports, period, generatedAt, caseCount }) {
  addHeader(doc, "Station case bundle", `Period: ${period}  ·  Generated: ${generatedAt}  ·  Cases in this file: ${caseCount}`);
  let y = 86;
  if (summary) {
    y = table(doc, autoTable, y, ["Measure", "Count / rate"], [
      ["Lost items reported", String(summary.lostItems ?? EMPTY)],
      ["Found items registered", String(summary.foundItems ?? EMPTY)],
      ["Items verified", String(summary.verifiedItems ?? EMPTY)],
      ["Claims filed", String(summary.claims ?? EMPTY)],
      ["Items recovered / handed over", String(summary.recoveredItems ?? EMPTY)],
      ["Recovery rate", summary.recoveryRate != null ? `${summary.recoveryRate}%` : EMPTY]
    ], "Period summary");
  }
  if (stationReports?.length) {
    y = table(
      doc,
      autoTable,
      y,
      ["Station", "Found", "Verified", "Claims", "Recovered", "Rate"],
      stationReports.map((station) => [
        station.stationName,
        String(station.foundItems),
        String(station.verifiedItems),
        String(station.claims),
        String(station.recoveredItems),
        `${station.recoveryRate}%`
      ]),
      "Station-wise summary"
    );
  }
  return y;
}

export async function downloadCaseFilePdf(caseFile) {
  const { jsPDF, autoTable } = await loadPdf();
  const doc = new jsPDF({ format: "a4", unit: "pt" });
  drawCase(doc, autoTable, caseFile, { firstPage: true });
  addFooters(doc);
  const slug = (caseFile.caseNumber || "case").replace(/[^\w.-]+/g, "-");
  doc.save(`findline-case-${slug}.pdf`);
}

export async function downloadAllCaseFilesPdf({ caseFiles, summary, stationReports, period }) {
  const { jsPDF, autoTable } = await loadPdf();
  const doc = new jsPDF({ format: "a4", unit: "pt" });
  const generatedAt = new Date().toLocaleString();
  drawSummaryCover(doc, autoTable, {
    summary,
    stationReports,
    period,
    generatedAt,
    caseCount: caseFiles.length
  });
  caseFiles.forEach((caseFile, index) => {
    drawCase(doc, autoTable, caseFile, { firstPage: false });
    if (index === 0) {
      // header already drawn per case
    }
  });
  addFooters(doc);
  const fromTo = (period || "all-dates").replace(/\s+/g, "-").replace(/[^\w.-]+/g, "");
  doc.save(`findline-all-cases-${fromTo}.pdf`);
}

export async function downloadCaseFileById(loader, caseId) {
  const caseFile = await loader(caseId);
  await downloadCaseFilePdf(caseFile);
}
