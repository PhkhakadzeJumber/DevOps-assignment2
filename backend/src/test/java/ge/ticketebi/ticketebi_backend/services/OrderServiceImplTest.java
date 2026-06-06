package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.CreateOrderItemRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.CreateOrderRequest;
import ge.ticketebi.ticketebi_backend.domain.dto.OrderResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.*;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.mappers.impl.OrderMapperImpl;
import ge.ticketebi.ticketebi_backend.repositories.OrderRepository;
import ge.ticketebi.ticketebi_backend.repositories.TicketRepository;
import ge.ticketebi.ticketebi_backend.repositories.TicketTypeRepository;
import ge.ticketebi.ticketebi_backend.security.qr.QrTokenService;
import ge.ticketebi.ticketebi_backend.services.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private TicketRepository ticketRepository;
    @Mock private TicketTypeRepository ticketTypeRepository;
    @Mock private QrTokenService qrTokenService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Spy private OrderMapperImpl orderMapper = new OrderMapperImpl();

    @InjectMocks private OrderServiceImpl orderService;

    private User user;
    private EventEntity event;
    private TicketTypeEntity ticketType;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(10L)
                .email("buyer@mail.com")
                .username("buyer")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        event = EventEntity.builder()
                .id(101L)
                .title("Rock Night")
                .date(LocalDateTime.now().plusDays(10))
                .organizer(User.builder().id(99L).authProvider(AuthProvider.LOCAL).build())
                .location(LocationEntity.builder().id(1L).name("Hall").build())
                .build();

        ticketType = TicketTypeEntity.builder()
                .id(1001L)
                .name("General")
                .price(new BigDecimal("15.00"))
                .quantityTotal(100)
                .quantityReserved(0)
                .quantitySold(0)
                .maxPurchase(5)
                .saleStartTime(LocalDateTime.now().minusDays(1))
                .saleEndTime(LocalDateTime.now().plusDays(1))
                .event(event)
                .build();
    }

    @Test
    void createDraftOrder_shouldReserveStockAndCreateExpiringDraft() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(List.of(
                        CreateOrderItemRequest.builder().ticketTypeId(ticketType.getId()).quantity(1).build(),
                        CreateOrderItemRequest.builder().ticketTypeId(ticketType.getId()).quantity(2).build()
                ))
                .build();

        when(orderRepository.findByStatusAndExpiresAtBefore(eq(OrderStatus.DRAFT), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(ticketTypeRepository.findAllByIdInForUpdate(List.of(ticketType.getId())))
                .thenReturn(List.of(ticketType));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setId(500L);
            return order;
        });

        OrderResponse response = orderService.createDraftOrder(request, user);

        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getStatus()).isEqualTo(OrderStatus.DRAFT);
        assertThat(response.getContactEmail()).isEqualTo("buyer@mail.com");
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().getFirst().getQuantity()).isEqualTo(3);
        assertThat(response.getSubTotalAmount()).isEqualByComparingTo("45.00");
        assertThat(response.getTotalAmount()).isEqualByComparingTo("45.00");
        assertThat(response.getExpiresAt()).isNotNull();
        assertThat(ticketType.getQuantityReserved()).isEqualTo(3);
        verify(ticketTypeRepository).saveAll(anyCollection());
    }

    @Test
    void createDraftOrder_shouldThrow_whenAnyTicketTypeDoesNotExist() {
        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(List.of(CreateOrderItemRequest.builder().ticketTypeId(9999L).quantity(1).build()))
                .build();

        when(orderRepository.findByStatusAndExpiresAtBefore(eq(OrderStatus.DRAFT), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(ticketTypeRepository.findAllByIdInForUpdate(List.of(9999L))).thenReturn(List.of());

        assertThatThrownBy(() -> orderService.createDraftOrder(request, user))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ticket types do not exist");
    }

    @Test
    void createDraftOrder_shouldThrow_whenItemsAreFromDifferentEvents() {
        EventEntity secondEvent = EventEntity.builder()
                .id(202L)
                .title("Jazz Night")
                .date(LocalDateTime.now().plusDays(7))
                .organizer(User.builder().id(98L).authProvider(AuthProvider.LOCAL).build())
                .location(LocationEntity.builder().id(2L).name("Arena").build())
                .build();

        TicketTypeEntity secondType = TicketTypeEntity.builder()
                .id(2002L)
                .name("VIP")
                .price(new BigDecimal("30.00"))
                .quantityTotal(50)
                .quantityReserved(0)
                .quantitySold(0)
                .maxPurchase(3)
                .saleStartTime(LocalDateTime.now().minusDays(1))
                .saleEndTime(LocalDateTime.now().plusDays(1))
                .event(secondEvent)
                .build();

        CreateOrderRequest request = CreateOrderRequest.builder()
                .items(List.of(
                        CreateOrderItemRequest.builder().ticketTypeId(ticketType.getId()).quantity(1).build(),
                        CreateOrderItemRequest.builder().ticketTypeId(secondType.getId()).quantity(1).build()
                ))
                .build();

        when(orderRepository.findByStatusAndExpiresAtBefore(eq(OrderStatus.DRAFT), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(ticketTypeRepository.findAllByIdInForUpdate(List.of(ticketType.getId(), secondType.getId())))
                .thenReturn(List.of(ticketType, secondType));

        assertThatThrownBy(() -> orderService.createDraftOrder(request, user))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("same event");
    }

    @Test
    void confirmOrder_shouldConsumeReservationAndGenerateTickets() {
        ticketType.setQuantityReserved(2);

        OrderItemEntity orderItem = OrderItemEntity.builder()
                .id(1L)
                .ticketType(ticketType)
                .quantity(2)
                .unitPrice(ticketType.getPrice())
                .lineTotal(new BigDecimal("30.00"))
                .build();

        OrderEntity order = OrderEntity.builder()
                .id(600L)
                .orderNumber("ORD-ABC123")
                .status(OrderStatus.DRAFT)
                .user(user)
                .contactEmail("contact@mail.com")
                .subTotalAmount(new BigDecimal("30.00"))
                .totalAmount(new BigDecimal("30.00"))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .orderItems(List.of(orderItem))
                .build();
        orderItem.setOrder(order);

        when(orderRepository.findByStatusAndExpiresAtBefore(eq(OrderStatus.DRAFT), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(orderRepository.findByIdAndUser(order.getId(), user)).thenReturn(Optional.of(order));
        when(ticketTypeRepository.findAllByIdInForUpdate(List.of(ticketType.getId())))
                .thenReturn(List.of(ticketType));
        when(qrTokenService.generateForTicket(any(TicketEntity.class))).thenReturn("qr-1", "qr-2");
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.confirmOrder(order.getId(), user);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(response.getTickets()).hasSize(2);
        assertThat(response.getTickets()).extracting(t -> t.getQrCode())
                .containsExactlyInAnyOrder("qr-1", "qr-2");
        assertThat(response.getExpiresAt()).isNull();
        assertThat(ticketType.getQuantitySold()).isEqualTo(2);
        assertThat(ticketType.getQuantityReserved()).isEqualTo(0);
        verify(ticketTypeRepository).saveAll(argThat(saved -> {
            if (saved == null) {
                return false;
            }
            var iterator = saved.iterator();
            if (!iterator.hasNext()) {
                return false;
            }
            TicketTypeEntity first = iterator.next();
            if (iterator.hasNext()) {
                return false;
            }
            return first.getId().equals(ticketType.getId())
                    && first.getQuantitySold().equals(2)
                    && first.getQuantityReserved().equals(0);
        }));
    }

    @Test
    void confirmOrder_shouldThrow_whenDraftExpired() {
        ticketType.setQuantityReserved(1);

        OrderItemEntity orderItem = OrderItemEntity.builder()
                .ticketType(ticketType)
                .quantity(1)
                .unitPrice(ticketType.getPrice())
                .lineTotal(ticketType.getPrice())
                .build();

        OrderEntity order = OrderEntity.builder()
                .id(700L)
                .orderNumber("ORD-EXPIRED")
                .status(OrderStatus.DRAFT)
                .user(user)
                .contactEmail("contact@mail.com")
                .subTotalAmount(ticketType.getPrice())
                .totalAmount(ticketType.getPrice())
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .orderItems(List.of(orderItem))
                .build();
        orderItem.setOrder(order);

        when(orderRepository.findByStatusAndExpiresAtBefore(eq(OrderStatus.DRAFT), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(orderRepository.findByIdAndUser(order.getId(), user)).thenReturn(Optional.of(order));
        when(ticketTypeRepository.findAllByIdInForUpdate(List.of(ticketType.getId())))
                .thenReturn(List.of(ticketType));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatThrownBy(() -> orderService.confirmOrder(order.getId(), user))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("reservation expired");

        assertThat(ticketType.getQuantityReserved()).isEqualTo(0);
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    void cancelOrder_shouldReleaseReservedStock_forDraftOrder() {
        ticketType.setQuantityReserved(1);

        OrderItemEntity orderItem = OrderItemEntity.builder()
                .ticketType(ticketType)
                .quantity(1)
                .unitPrice(ticketType.getPrice())
                .lineTotal(ticketType.getPrice())
                .build();

        OrderEntity order = OrderEntity.builder()
                .id(800L)
                .orderNumber("ORD-CANCEL")
                .status(OrderStatus.DRAFT)
                .user(user)
                .contactEmail("contact@mail.com")
                .subTotalAmount(new BigDecimal("15.00"))
                .totalAmount(new BigDecimal("15.00"))
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .orderItems(List.of(orderItem))
                .build();
        orderItem.setOrder(order);

        when(orderRepository.findByStatusAndExpiresAtBefore(eq(OrderStatus.DRAFT), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(orderRepository.findByIdAndUser(order.getId(), user)).thenReturn(Optional.of(order));
        when(ticketTypeRepository.findAllByIdInForUpdate(List.of(ticketType.getId())))
                .thenReturn(List.of(ticketType));
        when(orderRepository.save(any(OrderEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderResponse response = orderService.cancelOrder(order.getId(), user);

        assertThat(response.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.getExpiresAt()).isNull();
        assertThat(ticketType.getQuantityReserved()).isEqualTo(0);
    }
}
