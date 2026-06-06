package ge.ticketebi.ticketebi_backend.mappers.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.OrderItemResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.OrderResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.OrderEntity;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderMapperImpl implements Mapper<OrderEntity, OrderResponse> {

    @Override
    public OrderResponse mapTo(OrderEntity order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems() == null
                ? List.of()
                : order.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .ticketTypeId(item.getTicketType().getId())
                        .ticketTypeName(item.getTicketType().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .lineTotal(item.getLineTotal())
                        .build())
                .toList();

        List<TicketResponse> ticketResponses = order.getTicketsOrdered() == null
                ? List.of()
                : order.getTicketsOrdered().stream()
                .map(ticket -> TicketResponse.builder()
                        .id(ticket.getId())
                        .ticketNumber(ticket.getTicketNumber())
                        .qrCode(ticket.getQrCode())
                        .seatInfo(ticket.getSeatInfo())
                        .email(ticket.getEmail())
                        .status(ticket.getStatus())
                        .checkedInAt(ticket.getCheckedInAt())
                        .ticketTypeId(ticket.getTicketType().getId())
                        .ticketTypeName(ticket.getTicketType().getName())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .contactEmail(order.getContactEmail())
                .subTotalAmount(order.getSubTotalAmount())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .expiresAt(order.getExpiresAt())
                .items(itemResponses)
                .tickets(ticketResponses)
                .build();
    }

    @Override
    public OrderEntity mapFrom(OrderResponse orderResponse) {
        throw new UnsupportedOperationException("OrderResponse to OrderEntity mapping is not supported");
    }
}
