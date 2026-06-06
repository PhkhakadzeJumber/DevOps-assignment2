package ge.ticketebi.ticketebi_backend.services.impl;

import ge.ticketebi.ticketebi_backend.domain.events.OrderConfirmedEvent;
import ge.ticketebi.ticketebi_backend.services.TicketDeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderConfirmedListener {

    private final TicketDeliveryService ticketDeliveryService;

    @Async("ticketDeliveryExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOrderConfirmed(OrderConfirmedEvent event) {
        try {
            ticketDeliveryService.sendOrderTickets(event.orderId());
        } catch (Exception ex) {
            log.error("Failed to send tickets for orderId={}", event.orderId(), ex);
        }
    }
}
