package com.lostandfound.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lostandfound.dto.casedto.CaseFileDto;
import com.lostandfound.dto.casedto.CaseFileDto.*;
import com.lostandfound.dto.reports.ReportsSummaryDto;
import com.lostandfound.dto.reports.StationReportDto;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders police case files (CaseFileDto) to PDF. Mirrors the section layout of the
 * frontend jsPDF export (caseFilePdf.js): 10 numbered sections per case.
 *
 * Note: the built-in Helvetica font only covers Latin-1 characters. Names or text in
 * Kannada/Devanagari etc. will not render until a Unicode TTF is embedded.
 */
@Service
public class CaseFilePdfService {

    private static final String EMPTY = "-";
    private static final Color BRAND = new Color(25, 101, 95);
    private static final Color ROW_ALT = new Color(244, 247, 246);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final DateTimeFormatter DATE_ONLY = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK);
    private static final Font SUBTITLE_FONT = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.DARK_GRAY);
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, BRAND);
    private static final Font HEAD_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.WHITE);
    private static final Font LABEL_FONT = new Font(Font.HELVETICA, 8, Font.BOLD, Color.BLACK);
    private static final Font CELL_FONT = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.BLACK);
    private static final Font BANNER_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 7, Font.NORMAL, Color.GRAY);

    /** One case, as an official PDF. */
    public byte[] renderSingle(CaseFileDto caseFile) {
        return build(document -> drawCase(document, caseFile));
    }

    /** Cover page (period + summary + station table) followed by one section per case. */
    public byte[] renderBundle(List<CaseFileDto> caseFiles,
                               ReportsSummaryDto summary,
                               List<StationReportDto> stationReports,
                               String period) {
        return build(document -> {
            drawCover(document, caseFiles.size(), summary, stationReports, period);
            for (CaseFileDto caseFile : caseFiles) {
                document.newPage();
                drawCase(document, caseFile);
            }
        });
    }

    // ------------------------------------------------------------------ document plumbing

    @FunctionalInterface
    private interface DocumentBody {
        void write(Document document) throws DocumentException;
    }

    private byte[] build(DocumentBody body) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 42, 42, 48, 48);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterEvent());
            document.addTitle("FindLine - Police case file");
            document.addCreator("FindLine");
            document.open();
            body.write(document);
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate case file PDF", ex);
        } finally {
            if (document.isOpen()) {
                document.close();
            }
        }
        return out.toByteArray();
    }

    private static class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            Rectangle page = document.getPageSize();
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("CONFIDENTIAL - For official police use. Not a substitute for a registered FIR unless so directed.", FOOTER_FONT),
                    document.leftMargin(), 28, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Page " + writer.getPageNumber(), FOOTER_FONT),
                    page.getRight() - document.rightMargin(), 28, 0);
        }
    }

    // ------------------------------------------------------------------ cover (bulk only)

    private void drawCover(Document document, int caseCount, ReportsSummaryDto summary,
                           List<StationReportDto> stationReports, String period) throws DocumentException {
        banner(document);
        document.add(new Paragraph("Station case bundle", TITLE_FONT));
        Paragraph sub = new Paragraph("Period: " + period + "  |  Generated: "
                + DATE_TIME.format(LocalDateTime.now()) + "  |  Cases in this file: " + caseCount, SUBTITLE_FONT);
        sub.setSpacingAfter(12);
        document.add(sub);

        if (summary != null) {
            List<String[]> rows = new ArrayList<>();
            rows.add(new String[]{"Lost items reported", String.valueOf(summary.getLostItems())});
            rows.add(new String[]{"Found items registered", String.valueOf(summary.getFoundItems())});
            rows.add(new String[]{"Items verified", String.valueOf(summary.getVerifiedItems())});
            rows.add(new String[]{"Claims filed", String.valueOf(summary.getClaims())});
            rows.add(new String[]{"Items recovered / handed over", String.valueOf(summary.getRecoveredItems())});
            rows.add(new String[]{"Recovery rate", summary.getRecoveryRate() + "%"});
            keyValueSection(document, "Period summary", rows);
        }

        if (stationReports != null && !stationReports.isEmpty()) {
            List<String[]> rows = stationReports.stream()
                    .map(s -> new String[]{
                            s.getStationName(),
                            String.valueOf(s.getFoundItems()),
                            String.valueOf(s.getVerifiedItems()),
                            String.valueOf(s.getClaims()),
                            String.valueOf(s.getRecoveredItems()),
                            s.getRecoveryRate() + "%"})
                    .collect(Collectors.toList());
            gridSection(document, "Station-wise summary",
                    new String[]{"Station", "Found", "Verified", "Claims", "Recovered", "Rate"},
                    new float[]{34, 12, 14, 12, 14, 14}, rows);
        }
    }

    // ------------------------------------------------------------------ one case

    private void drawCase(Document document, CaseFileDto c) throws DocumentException {
        StationBlock station = orElse(c.getStation(), new StationBlock());
        PersonBlock person = orElse(c.getComplainant(), new PersonBlock());
        OccurrenceBlock occ = orElse(c.getOccurrence(), new OccurrenceBlock());
        PropertyBlock prop = orElse(c.getProperty(), new PropertyBlock());
        CustodyBlock custody = c.getCustody();

        banner(document);
        document.add(new Paragraph("Case " + text(c.getCaseNumber()), TITLE_FONT));
        String kind = "LOST".equals(c.getCaseType()) ? "Lost property report" : "Found property / seizure memo";
        Paragraph sub = new Paragraph(kind + "  |  " + text(station.getStationName())
                + "  |  Status: " + text(c.getCurrentStatus()), SUBTITLE_FONT);
        sub.setSpacingAfter(10);
        document.add(sub);

        keyValueSection(document, "1. Registration & station", List.of(
                kv("Case number", c.getCaseNumber()),
                kv("Case type", c.getCaseType()),
                kv("Current status", c.getCurrentStatus()),
                kv("Registered at", dateTime(c.getRegisteredAt())),
                kv("Last updated", dateTime(c.getLastUpdatedAt())),
                kv("Investigating station", station.getStationName()),
                kv("Station code", station.getStationCode()),
                kv("Station address", join(", ", station.getAddress(), station.getCity())),
                kv("Station phone", station.getPhone())));

        keyValueSection(document, "2. Complainant / informant", List.of(
                kv("Capacity", person.getRoleLabel()),
                kv("Name", person.getName()),
                kv("Phone", person.getPhone()),
                kv("Email", person.getEmail()),
                kv("Address", person.getAddress())));

        keyValueSection(document, "3. Place and time of occurrence", List.of(
                kv("Date of occurrence", date(occ.getOccurrenceDate())),
                kv("Place of occurrence", join(", ", occ.getAddressText(), occ.getLocality(), occ.getCity(),
                        occ.getState(), occ.getPostalCode())),
                kv("Locality / city", join(", ", occ.getLocality(), occ.getCity())),
                kv("State / PIN", join(" / ", occ.getState(), occ.getPostalCode())),
                kv("Coordinates", occ.getLatitude() != null ? occ.getLatitude() + ", " + occ.getLongitude() : null)));

        keyValueSection(document, "4. Description of property", List.of(
                kv("Category of property", prop.getCategory()),
                kv("Description", prop.getDescription()),
                kv("Brand", prop.getBrand()),
                kv("Colour", prop.getColor()),
                kv("Identifying marks / IMEI / serial", prop.getIdentifyingDetails()),
                kv("Item status", prop.getItemStatus()),
                kv("Property ID", prop.getItemId() != null ? prop.getItemId().toString() : null),
                kv("Photographs on file", String.valueOf(prop.getPhotoUrls() == null ? 0 : prop.getPhotoUrls().size()))));

        if (custody != null) {
            PersonBlock finder = orElse(custody.getFinder(), new PersonBlock());
            keyValueSection(document, "5. Custody / malkhana", List.of(
                    kv("Custody status", custody.getCustodyStatus()),
                    kv("Verification status", custody.getVerificationStatus()),
                    kv("Received in station", dateTime(custody.getReceivedAt())),
                    kv("Linked found-report status", custody.getFoundReportStatus()),
                    kv("Finder name", finder.getName()),
                    kv("Finder phone", finder.getPhone()),
                    kv("Finder email", finder.getEmail())));
        }

        List<MatchBlock> matches = nullSafe(c.getMatches());
        gridSection(document, "6. Matching / investigation leads",
                new String[]{"Score", "Status", "Counterpart", "Reason"},
                new float[]{9, 14, 37, 40},
                matches.isEmpty()
                        ? placeholder(4, "No system matches recorded")
                        : matches.stream().map(m -> new String[]{
                                m.getMatchScore() != null ? m.getMatchScore() + "%" : EMPTY,
                                text(m.getStatus()),
                                text(m.getCounterpartType()) + " | " + text(m.getCounterpartCategory())
                                        + " | " + text(m.getCounterpartDescription()),
                                text(m.getMatchReason())}).collect(Collectors.toList()));

        List<ClaimBlock> claims = nullSafe(c.getClaims());
        gridSection(document, "7. Claims and ownership verification",
                new String[]{"Claimant", "Contact", "Status", "Details / review"},
                new float[]{17, 22, 13, 48},
                claims.isEmpty()
                        ? placeholder(4, "No claims filed")
                        : claims.stream().map(this::claimRow).collect(Collectors.toList()));

        List<DisputeBlock> disputes = nullSafe(c.getDisputes());
        gridSection(document, "8. Disputes",
                new String[]{"Raised by", "Status", "Reason / resolution"},
                new float[]{22, 14, 64},
                disputes.isEmpty()
                        ? placeholder(3, "No disputes")
                        : disputes.stream().map(d -> new String[]{
                                text(d.getRaisedByName()),
                                text(d.getStatus()),
                                join(" | ", d.getReason(), d.getResolution() == null ? null
                                        : "Resolution (" + dateTime(d.getResolvedAt()) + " by "
                                        + text(d.getResolvedByName()) + "): " + d.getResolution())})
                                .collect(Collectors.toList()));

        HandoverBlock handover = c.getHandover();
        List<String[]> handoverRows = new ArrayList<>();
        if (handover == null) {
            handoverRows.add(kv("Status", "Property not yet handed over"));
        } else {
            handoverRows.add(kv("Handover date", dateTime(handover.getHandoverDate())));
            handoverRows.add(kv("Recipient", handover.getRecipientName()));
            handoverRows.add(kv("Recipient phone", handover.getRecipientPhone()));
            handoverRows.add(kv("Duty officer", handover.getOfficerName()));
            handoverRows.add(kv("Acknowledgement / receipt no.", handover.getAcknowledgementReference()));
            handoverRows.add(kv("Notes", handover.getNotes()));
        }
        keyValueSection(document, "9. Handover / property receipt", handoverRows);

        List<HistoryBlock> diary = nullSafe(c.getCaseDiary());
        gridSection(document, "10. Case diary (status history)",
                new String[]{"Date / time", "From -> To", "Officer", "Remarks"},
                new float[]{19, 26, 22, 33},
                diary.isEmpty()
                        ? placeholder(4, "No case diary entries")
                        : diary.stream().map(h -> new String[]{
                                dateTime(h.getChangedAt()),
                                text(h.getOldStatus()) + " -> " + text(h.getNewStatus()),
                                join(" | ", h.getRecordedByName(), h.getRecordedByRole()),
                                text(h.getRemarks())}).collect(Collectors.toList()));
    }

    private String[] claimRow(ClaimBlock cl) {
        String evidence = nullSafe(cl.getEvidence()).isEmpty() ? null
                : "Evidence: " + cl.getEvidence().stream()
                        .map(e -> e.getEvidenceType() != null ? e.getEvidenceType() : e.getDescription())
                        .collect(Collectors.joining("; "));
        String reviewed = cl.getReviewedByName() == null ? null
                : "Reviewed by " + cl.getReviewedByName() + " on " + dateTime(cl.getReviewedAt());
        return new String[]{
                text(cl.getClaimantName()),
                join(" | ", cl.getClaimantPhone(), cl.getClaimantEmail()),
                text(cl.getStatus()),
                join(" | ", cl.getClaimDetails(), reviewed, evidence)};
    }

    // ------------------------------------------------------------------ table helpers

    private void banner(Document document) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        PdfPCell cell = new PdfPCell(new Phrase("FindLine  |  Digital Lost & Found  |  Police case diary", BANNER_FONT));
        cell.setBackgroundColor(BRAND);
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(8);
        table.addCell(cell);
        table.setSpacingAfter(12);
        document.add(table);
    }

    private void keyValueSection(Document document, String title, List<String[]> rows) throws DocumentException {
        addSectionTitle(document, title);
        PdfPTable table = new PdfPTable(new float[]{32, 68});
        table.setWidthPercentage(100);
        table.setSpacingAfter(12);
        int i = 0;
        for (String[] row : rows) {
            table.addCell(bodyCell(row[0], LABEL_FONT, i));
            table.addCell(bodyCell(row[1], CELL_FONT, i));
            i++;
        }
        document.add(table);
    }

    private void gridSection(Document document, String title, String[] head, float[] widths,
                             List<String[]> rows) throws DocumentException {
        addSectionTitle(document, title);
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setHeaderRows(1);
        table.setSpacingAfter(12);
        for (String h : head) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEAD_FONT));
            cell.setBackgroundColor(BRAND);
            cell.setPadding(4);
            cell.setBorderColor(BRAND);
            table.addCell(cell);
        }
        int i = 0;
        for (String[] row : rows) {
            for (String value : row) {
                table.addCell(bodyCell(value, CELL_FONT, i));
            }
            i++;
        }
        document.add(table);
    }

    private void addSectionTitle(Document document, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, SECTION_FONT);
        p.setSpacingBefore(4);
        p.setSpacingAfter(4);
        p.setKeepTogether(true);
        document.add(p);
    }

    private PdfPCell bodyCell(String value, Font font, int rowIndex) {
        PdfPCell cell = new PdfPCell(new Phrase(text(value), font));
        cell.setPadding(4);
        cell.setBorderColor(new Color(200, 205, 202));
        if (rowIndex % 2 == 1) {
            cell.setBackgroundColor(ROW_ALT);
        }
        return cell;
    }

    private List<String[]> placeholder(int columns, String message) {
        String[] row = new String[columns];
        java.util.Arrays.fill(row, "");
        row[0] = message;
        return List.<String[]>of(row);
    }

    // ------------------------------------------------------------------ value helpers

    private static String[] kv(String label, String value) {
        return new String[]{label, text(value)};
    }

    private static String text(String value) {
        return value == null || value.isBlank() ? EMPTY : value;
    }

    private static String dateTime(LocalDateTime value) {
        return value == null ? EMPTY : DATE_TIME.format(value);
    }

    private static String date(LocalDate value) {
        return value == null ? EMPTY : DATE_ONLY.format(value);
    }

    private static String join(String separator, String... parts) {
        String joined = java.util.Arrays.stream(parts)
                .filter(p -> p != null && !p.isBlank())
                .collect(Collectors.joining(separator));
        return joined.isEmpty() ? EMPTY : joined;
    }

    private static <T> T orElse(T value, T fallback) {
        return value != null ? value : fallback;
    }

    private static <T> List<T> nullSafe(List<T> list) {
        return list == null ? List.of() : list;
    }
}