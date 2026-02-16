package org.example.util;

import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.example.dao.BookingDAO;
import org.example.model.Booking;

import javax.mail.util.ByteArrayDataSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ReminderService {

    public void sendUpcomingShowReminders() {
        BookingDAO bookingDAO = new BookingDAO();
        List<Booking> bookings = bookingDAO.getUpcomingBookings();

        for (Booking booking : bookings) {
            try {
                byte[] pdf = generateTicketPdf(booking);
                sendEmail(booking.user().email(), pdf);
                bookingDAO.markReminderSent(booking.id());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public byte[] generateTicketPdf(Booking booking) throws IOException {

        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream content = new PDPageContentStream(document, page);
        content.beginText();
        content.setFont(new PDType1Font(Standard14Fonts.FontName.COURIER), 10);
        PDRectangle bbox = page.getBBox();
        content.newLineAtOffset(0, bbox.getHeight());

        content.newLineAtOffset(0, -20);
        content.showText("Movie: " + booking.show().title());
        content.newLineAtOffset(0, -20);
        content.showText("Screen: " + booking.show().screen().name());
        content.newLineAtOffset(0, -20);
        content.showText("Seats: " + booking.seatsBooked());
        content.newLineAtOffset(0, -20);
        content.showText("Show Time: " + booking.show().startTime());

        content.endText();
        content.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();

        return out.toByteArray();
    }

    public void sendEmail(String to, byte[] pdfBytes) throws EmailException {

        String myEmail = "kaushikkalesh@gmail.com";
        String appPassword = "pjqmqfmzbyfptpdu";

        MultiPartEmail email = new MultiPartEmail();

        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(587);
        email.setAuthenticator(
                new DefaultAuthenticator(myEmail, appPassword)
        );
        email.setStartTLSEnabled(true);

        email.setFrom(myEmail);
        email.setSubject("Your Movie Ticket");
        email.setMsg("Your show starts in 1 hour. Ticket attached.");
        email.addTo(to);

        ByteArrayDataSource dataSource = new ByteArrayDataSource(pdfBytes, "application/pdf");

        email.attach(dataSource, "ticket.pdf", "Movie Ticket");

        email.send();

    }
}

