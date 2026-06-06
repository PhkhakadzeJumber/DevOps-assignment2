package ge.ticketebi.ticketebi_backend.services.impl;

import ge.ticketebi.ticketebi_backend.domain.dto.*;
import ge.ticketebi.ticketebi_backend.domain.entities.*;
import ge.ticketebi.ticketebi_backend.domain.events.OrderConfirmedEvent;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.mappers.Mapper;
import ge.ticketebi.ticketebi_backend.repositories.OrderRepository;
import ge.ticketebi.ticketebi_backend.repositories.TicketRepository;
import ge.ticketebi.ticketebi_backend.repositories.TicketTypeRepository;
import ge.ticketebi.ticketebi_backend.security.qr.QrTokenService;
import ge.ticketebi.ticketebi_backend.services.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final Mapper<OrderEntity, OrderResponse> orderMapper;
    private final QrTokenService qrTokenService;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.orders.draft-reservation-minutes:10}")
    private long draftReservationMinutes = 10;

    @Override
    public OrderResponse createDraftOrder(CreateOrderRequest request, User user) {
        expireDraftOrders();

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new InvalidRequestException("Order must contain at least one item");
        }

        Map<Long, Integer> mergedItems = mergeAndValidateItems(request.getItems());
        List<Long> ticketTypeIds = new ArrayList<>(mergedItems.keySet());
        List<TicketTypeEntity> lockedTicketTypes = ticketTypeRepository.findAllByIdInForUpdate(ticketTypeIds);
        if (lockedTicketTypes.size() != ticketTypeIds.size()) {
            throw new ResourceNotFoundException("One or more ticket types do not exist");
        }

        LocalDateTime now = LocalDateTime.now();
        Map<Long, TicketTypeEntity> ticketTypeById = lockedTicketTypes.stream()
                .collect(Collectors.toMap(TicketTypeEntity::getId, t -> t));

        OrderEntity order = new OrderEntity();
        order.setOrderNumber(generateUniqueOrderNumber());
        order.setUser(user);
        order.setStatus(OrderStatus.DRAFT);
        order.setContactEmail(resolveContactEmail(request.getContactEmail(), user.getEmail()));
        order.setExpiresAt(now.plusMinutes(draftReservationMinutes));

        BigDecimal subTotal = BigDecimal.ZERO;
        List<OrderItemEntity> orderItems = new ArrayList<>();
        Long orderEventId = null;

        for (Map.Entry<Long, Integer> entry : mergedItems.entrySet()) {
            TicketTypeEntity ticketType = ticketTypeById.get(entry.getKey());
            int quantity = entry.getValue();
            Long ticketEventId = ticketType.getEvent().getId();
            if (orderEventId == null) {
                orderEventId = ticketEventId;
            } else if (!orderEventId.equals(ticketEventId)) {
                throw new InvalidRequestException("Order items must belong to the same event");
            }

            validateSaleWindow(now, ticketType);
            if (quantity > ticketType.getMaxPurchase()) {
                throw new InvalidRequestException("Quantity exceeds max purchase for ticket type: " + ticketType.getName());
            }
            if (!ticketType.reserveTickets(quantity)) {
                throw new InvalidRequestException("Not enough tickets available for: " + ticketType.getName());
            }

            BigDecimal lineTotal = ticketType.getPrice()
                    .multiply(BigDecimal.valueOf(quantity))
                    .setScale(2, RoundingMode.HALF_UP);

            OrderItemEntity item = OrderItemEntity.builder()
                    .order(order)
                    .ticketType(ticketType)
                    .quantity(quantity)
                    .unitPrice(ticketType.getPrice())
                    .lineTotal(lineTotal)
                    .build();

            orderItems.add(item);
            subTotal = subTotal.add(lineTotal);
        }

        order.setOrderItems(orderItems);
        order.setSubTotalAmount(subTotal);
        order.setTotalAmount(subTotal);

        ticketTypeRepository.saveAll(lockedTicketTypes);
        OrderEntity saved = orderRepository.save(order);
        return orderMapper.mapTo(saved);
    }

    @Override
    public OrderResponse confirmOrder(Long orderId, User user) {
        expireDraftOrders();

        OrderEntity order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CONFIRMED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidRequestException("Order is already confirmed");
        }
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.REFUNDED) {
            throw new InvalidRequestException("Order cannot be confirmed in current state");
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new InvalidRequestException("Order does not contain any items");
        }
        if (isDraftExpired(order, LocalDateTime.now())) {
            expireSingleOrder(order);
            throw new InvalidRequestException("Draft reservation expired. Create a new draft order.");
        }

        Map<Long, TicketTypeEntity> lockedById = lockTicketTypesForOrder(order);
        for (OrderItemEntity item : order.getOrderItems()) {
            TicketTypeEntity lockedType = lockedById.get(item.getTicketType().getId());
            int quantity = item.getQuantity();
            if (!lockedType.confirmPurchase(quantity)) {
                throw new InvalidRequestException("Reservation is no longer valid for: " + lockedType.getName());
            }
        }

        List<TicketEntity> tickets = new ArrayList<>();
        for (OrderItemEntity item : order.getOrderItems()) {
            TicketTypeEntity ticketType = lockedById.get(item.getTicketType().getId());
            for (int i = 0; i < item.getQuantity(); i++) {
                TicketEntity ticket = new TicketEntity();
                ticket.setOrder(order);
                ticket.setTicketType(ticketType);
                ticket.setEmail(order.getContactEmail());
                ticket.setTicketNumber(generateUniqueTicketNumber(order, ticketType));
                ticket.setQrCode(qrTokenService.generateForTicket(ticket));
                tickets.add(ticket);
            }
        }

        order.getTicketsOrdered().addAll(tickets);
        order.setStatus(OrderStatus.CONFIRMED);
        order.setExpiresAt(null);

        ticketTypeRepository.saveAll(lockedById.values());
        ticketRepository.saveAll(tickets);
        OrderEntity saved = orderRepository.save(order);
        eventPublisher.publishEvent(new OrderConfirmedEvent(saved.getId()));
        return orderMapper.mapTo(saved);
    }

    @Override
    public OrderResponse cancelOrder(Long orderId, User user) {
        expireDraftOrders();

        OrderEntity order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CONFIRMED ||
                order.getStatus() == OrderStatus.COMPLETED ||
                order.getStatus() == OrderStatus.REFUNDED) {
            throw new InvalidRequestException("Confirmed/completed/refunded orders cannot be cancelled");
        }

        if (order.getStatus() == OrderStatus.DRAFT) {
            releaseReservation(order);
        }
        order.setStatus(OrderStatus.CANCELLED);
        order.setExpiresAt(null);
        return orderMapper.mapTo(orderRepository.save(order));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(User user) {
        return orderRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(orderMapper::mapTo)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrderById(Long orderId, User user) {
        OrderEntity order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return orderMapper.mapTo(order);
    }

    /**
     * Reservation cleanup is intentionally called on write flows to keep stock consistent
     * without requiring a scheduler at this stage.
     */
    private void expireDraftOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<OrderEntity> expiredDrafts = orderRepository.findByStatusAndExpiresAtBefore(OrderStatus.DRAFT, now);
        for (OrderEntity order : expiredDrafts) {
            expireSingleOrder(order);
        }
    }

    private void expireSingleOrder(OrderEntity order) {
        releaseReservation(order);
        order.setStatus(OrderStatus.CANCELLED);
        order.setExpiresAt(null);
        orderRepository.save(order);
    }

    private void releaseReservation(OrderEntity order) {
        Map<Long, TicketTypeEntity> lockedById = lockTicketTypesForOrder(order);
        for (OrderItemEntity item : order.getOrderItems()) {
            TicketTypeEntity lockedType = lockedById.get(item.getTicketType().getId());
            lockedType.releaseReservedTickets(item.getQuantity());
        }
        ticketTypeRepository.saveAll(lockedById.values());
    }

    private Map<Long, TicketTypeEntity> lockTicketTypesForOrder(OrderEntity order) {
        List<Long> ticketTypeIds = order.getOrderItems().stream()
                .map(oi -> oi.getTicketType().getId())
                .distinct()
                .toList();

        List<TicketTypeEntity> lockedTicketTypes = ticketTypeRepository.findAllByIdInForUpdate(ticketTypeIds);
        if (lockedTicketTypes.size() != ticketTypeIds.size()) {
            throw new ResourceNotFoundException("One or more ticket types do not exist");
        }
        return lockedTicketTypes.stream()
                .collect(Collectors.toMap(TicketTypeEntity::getId, tt -> tt));
    }

    private boolean isDraftExpired(OrderEntity order, LocalDateTime now) {
        return order.getStatus() == OrderStatus.DRAFT
                && order.getExpiresAt() != null
                && order.getExpiresAt().isBefore(now);
    }

    private Map<Long, Integer> mergeAndValidateItems(List<CreateOrderItemRequest> items) {
        Map<Long, Integer> mergedItems = new LinkedHashMap<>();
        for (CreateOrderItemRequest item : items) {
            if (item.getTicketTypeId() == null) {
                throw new InvalidRequestException("ticketTypeId is required");
            }
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new InvalidRequestException("quantity must be greater than 0");
            }
            mergedItems.merge(item.getTicketTypeId(), item.getQuantity(), Integer::sum);
        }
        return mergedItems;
    }

    private void validateSaleWindow(LocalDateTime now, TicketTypeEntity ticketType) {
        if (now.isBefore(ticketType.getSaleStartTime()) || now.isAfter(ticketType.getSaleEndTime())) {
            throw new InvalidRequestException("Sale period is closed for ticket type: " + ticketType.getName());
        }
    }

    private String resolveContactEmail(String requestedEmail, String fallbackEmail) {
        if (requestedEmail != null && !requestedEmail.isBlank()) {
            return requestedEmail.trim();
        }
        return fallbackEmail;
    }

    // for now ignoring uuid collision
    private String generateUniqueOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private String generateUniqueTicketNumber(OrderEntity order, TicketTypeEntity ticketType) {
        return "TKT-" + order.getOrderNumber() + "-" + ticketType.getId() + "-"
                + UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
