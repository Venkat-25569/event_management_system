 package com.events.event_management.service;

import com.events.event_management.model.Event;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class CertificateService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    public byte[] generateCertificate(Event event, String participantName) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(outputStream);
        PdfDocument pdfDoc = new PdfDocument(writer);
        pdfDoc.setDefaultPageSize(PageSize.A4.rotate()); // Landscape

        Document document = new Document(pdfDoc);
        document.setMargins(50, 50, 50, 50);

        // Title
        Paragraph title = new Paragraph("CERTIFICATE OF PARTICIPATION")
                .setFontSize(28)
                .setBold()
                .setFontColor(new DeviceRgb(13, 71, 161))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);

        // Decorative line
        Paragraph line = new Paragraph("✦ ✦ ✦")
                .setFontSize(16)
                .setFontColor(new DeviceRgb(212, 175, 55))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(line);

        // "This is to certify"
        Paragraph certify = new Paragraph("This is to proudly certify that")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(certify);

        // Participant name
        Paragraph name = new Paragraph(participantName.toUpperCase())
                .setFontSize(32)
                .setBold()
                .setFontColor(new DeviceRgb(13, 71, 161))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(name);

        // Body text
        Paragraph body = new Paragraph("has successfully participated in the event")
                .setFontSize(14)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(15);
        document.add(body);

        // Event title
        Paragraph eventTitle = new Paragraph("\"" + event.getTitle() + "\"")
                .setFontSize(22)
                .setBold()
                .setFontColor(new DeviceRgb(13, 71, 161))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(eventTitle);

        // Event details
        String eventDate = event.getEventDate() != null ? 
            event.getEventDate().format(DATE_FORMATTER) : "N/A";
        
        String details = String.format(
            "Organized by: %s | Date: %s | Venue: %s",
            event.getClubName() != null ? event.getClubName() : "Event Management",
            eventDate,
            event.getVenue() != null ? event.getVenue() : "N/A"
        );
        
        Paragraph eventDetails = new Paragraph(details)
                .setFontSize(12)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(40);
        document.add(eventDetails);

        // Bottom line
        Paragraph bottomLine = new Paragraph("_______________________________________________")
                .setFontSize(10)
                .setFontColor(new DeviceRgb(212, 175, 55))
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(bottomLine);

        // Signatures section
        Paragraph signatures = new Paragraph()
                .add("_________________                    ")
                .add("_________________\n")
                .add("   Organizer                              ")
                .add("   Principal/HOD")
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10);
        document.add(signatures);

        // Certificate number
        Paragraph certNo = new Paragraph(
            "Certificate No: EVT-" + event.getId() + "-" + (System.currentTimeMillis() % 10000)
        )
                .setFontSize(8)
                .setFontColor(ColorConstants.GRAY)
                .setTextAlignment(TextAlignment.CENTER);
        document.add(certNo);

        // College name at bottom
        Paragraph college = new Paragraph("🏛️ YOUR COLLEGE NAME")
                .setFontSize(10)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(10);
        document.add(college);

        document.close();
        return outputStream.toByteArray();
    }
}