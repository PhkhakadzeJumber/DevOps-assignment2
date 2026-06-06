package ge.ticketebi.ticketebi_backend.services.impl;

import ge.ticketebi.ticketebi_backend.domain.entities.OrderEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.TicketEntity;
import ge.ticketebi.ticketebi_backend.repositories.OrderRepository;
import ge.ticketebi.ticketebi_backend.repositories.TicketRepository;
import ge.ticketebi.ticketebi_backend.services.QrImageService;
import ge.ticketebi.ticketebi_backend.services.TicketDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketDeliveryServiceImpl implements TicketDeliveryService {

    private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final JavaMailSender mailSender;
    private final QrImageService qrImageService;

    @Value("${spring.mail.username:no-reply@ticketebi.local}")
    private String fromEmail;

    @Override
    @Transactional(readOnly = true)
    public void sendOrderTickets(Long orderId) {
        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Order not found for ticket delivery: " + orderId));

        List<TicketEntity> tickets = ticketRepository.findByOrder(order);
        if (tickets.isEmpty()) {
            return;
        }

        TicketEntity firstTicket = tickets.getFirst();
        String eventTitle = firstTicket.getTicketType().getEvent().getTitle();
        String eventDate = firstTicket.getTicketType().getEvent().getDate().format(EVENT_DATE_FORMATTER);

        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setFrom(fromEmail);
            helper.setTo(order.getContactEmail());
            helper.setSubject("Your tickets for order " + order.getOrderNumber());
            helper.setText(buildBody(order, tickets, eventTitle, eventDate), false);

            for (TicketEntity ticket : tickets) {
                byte[] qrPng = qrImageService.generatePng(ticket.getQrCode(), 360);
                helper.addAttachment(
                        "ticket-" + ticket.getTicketNumber() + ".png",
                        new ByteArrayResource(qrPng)
                );
            }

            mailSender.send(message);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to deliver tickets for order " + order.getOrderNumber(), ex);
        }
    }

    private String buildBody(OrderEntity order, List<TicketEntity> tickets, String eventTitle, String eventDate) {
        StringBuilder builder = new StringBuilder();
        builder.append("Hello,\n\n");
        builder.append("Your order has been confirmed.\n\n");
        builder.append("Order: ").append(order.getOrderNumber()).append('\n');
        builder.append("Event: ").append(eventTitle).append('\n');
        builder.append("Date: ").append(eventDate).append("\n\n");
        builder.append("Total: ").append(order.getTotalAmount()).append("\n\n");
        builder.append("Tickets:\n");
        int index = 1;
        for (TicketEntity ticket : tickets) {
            String seatInfo = ticket.getSeatInfo() == null || ticket.getSeatInfo().isBlank()
                    ? "N/A"
                    : ticket.getSeatInfo();
            builder.append(index++).append(". ")
                    .append("Number: ").append(ticket.getTicketNumber())
                    .append(" | Type: ").append(ticket.getTicketType().getName())
                    .append(" | Price: ").append(ticket.getTicketType().getPrice())
                    .append(" | Seat: ").append(seatInfo)
                    .append('\n');
        }
        builder.append("\nQR code images are attached to this email.\n");
        return builder.toString();
    }
}
