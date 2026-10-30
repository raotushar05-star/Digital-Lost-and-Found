package com.lostandfound.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lostandfound.entity.Case;
import com.lostandfound.entity.FoundItem;
import com.lostandfound.entity.HandoverRecord;
import com.lostandfound.entity.PoliceStation;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class HandoverReceiptPdfService {

    private static final Color BRAND = new Color(25, 101, 95);
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");
    private static final Font TITLE = new Font(Font.HELVETICA, 16, Font.BOLD, Color.BLACK);
    private static final Font SECTION = new Font(Font.HELVETICA, 11, Font.BOLD, BRAND);
    private static final Font LABEL = new Font(Font.HELVETICA, 9, Font.BOLD, Color.BLACK);
    private static final Font CELL = new Font(Font.HELVETICA, 9, Font.NORMAL, Color.BLACK);
    private static final Font BANNER = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
    private static final Font SMALL = new Font(Font.HELVETICA, 8, Font.NORMAL, Color.DARK_GRAY);

    public byte[] render(HandoverRecord h, Case linkedCase) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4, 48, 48, 54, 54);
        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            PdfPTable banner = new PdfPTable(1);
            banner.setWidthPercentage(100);
            PdfPCell bc = new PdfPCell(new Phrase("FindLine  |  Digital Lost & Found  |  Property handover receipt", BANNER));
            bc.setBackgroundColor(BRAND);
            bc.setBorder(Rectangle.NO_BORDER);
            bc.setPadding(8);
            banner.addCell(bc);
            banner.setSpacingAfter(14);
            doc.add(banner);

            doc.add(new Paragraph("Handover receipt", TITLE));
            String receiptNo = h.getAcknowledgementReference() != null && !h.getAcknowledgementReference().isBlank()
                    ? h.getAcknowledgementReference()
                    : h.getHandoverId().toString().substring(0, 8).toUpperCase();
            Paragraph sub = new Paragraph("Receipt no. " + receiptNo, SMALL);
            sub.setSpacingAfter(10);
            doc.add(sub);

            FoundItem item = h.getFoundItem();
            PoliceStation station = item.getStation();

            section(doc, "Handover", new String[][]{
                    {"Handover date", date(h.getHandoverDate())},
                    {"Case number", linkedCase != null ? linkedCase.getCaseNumber() : null},
                    {"Station", station.getStationName() + " (" + station.getStationCode() + ")"},
                    {"Duty officer", h.getOfficer().getName()},
                    {"Notes", h.getHandoverNotes()}});
            section(doc, "Property", new String[][]{
                    {"Category", item.getCategory().getCategoryName()},
                    {"Description", item.getDescription()},
                    {"Brand", item.getBrand()},
                    {"Colour", item.getColor()}});
            section(doc, "Received by", new String[][]{
                    {"Name", h.getRecipient().getName()},
                    {"Phone", h.getRecipient().getPhone()}});

            Paragraph declaration = new Paragraph(
                    "The recipient confirms receiving the property described above from the police station named above.", CELL);
            declaration.setSpacingBefore(10);
            declaration.setSpacingAfter(36);
            doc.add(declaration);

            PdfPTable sign = new PdfPTable(2);
            sign.setWidthPercentage(100);
            sign.addCell(signCell("Signature of recipient"));
            sign.addCell(signCell("Signature of duty officer"));
            doc.add(sign);
        } catch (DocumentException ex) {
            throw new IllegalStateException("Failed to generate handover receipt", ex);
        } finally {
            if (doc.isOpen()) {
                doc.close();
            }
        }
        return out.toByteArray();
    }

    private void section(Document doc, String title, String[][] rows) throws DocumentException {
        Paragraph p = new Paragraph(title, SECTION);
        p.setSpacingBefore(6);
        p.setSpacingAfter(4);
        doc.add(p);
        PdfPTable table = new PdfPTable(new float[]{30, 70});
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        for (String[] row : rows) {
            table.addCell(cell(row[0], LABEL));
            table.addCell(cell(row[1], CELL));
        }
        doc.add(table);
    }

    private PdfPCell cell(String value, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(value == null || value.isBlank() ? "-" : value, font));
        c.setPadding(5);
        c.setBorderColor(new Color(200, 205, 202));
        return c;
    }

    private PdfPCell signCell(String label) {
        PdfPCell c = new PdfPCell(new Phrase(label, SMALL));
        c.setBorder(Rectangle.TOP);
        c.setPaddingTop(4);
        c.setPaddingRight(24);
        return c;
    }

    private String date(LocalDateTime value) {
        return value == null ? "-" : DATE_TIME.format(value);
    }
}