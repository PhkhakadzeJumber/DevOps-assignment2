package ge.ticketebi.ticketebi_backend.services;

import ge.ticketebi.ticketebi_backend.domain.dto.MessageResponse;
import ge.ticketebi.ticketebi_backend.domain.dto.TicketResponse;
import ge.ticketebi.ticketebi_backend.domain.entities.*;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidRequestException;
import ge.ticketebi.ticketebi_backend.exceptions.InvalidQrTokenException;
import ge.ticketebi.ticketebi_backend.exceptions.ResourceNotFoundException;
import ge.ticketebi.ticketebi_backend.exceptions.UnauthorizedActionException;
import ge.ticketebi.ticketebi_backend.mappers.impl.TicketMapperImpl;
import ge.ticketebi.ticketebi_backend.repositories.TicketRepository;
import ge.ticketebi.ticketebi_backend.security.qr.QrTokenService;
import ge.ticketebi.ticketebi_backend.services.impl.TicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceImplTest {

    @Mock private TicketRepository ticketRepository;
    @Mock private QrTokenService qrTokenService;
    @Spy private TicketMapperImpl ticketMapper = new TicketMapperImpl();
    @InjectMocks private TicketServiceImpl ticketService;

    private User customer;
    private User organizer;
    private User anotherOrganizer;
    private User admin;
    private TicketEntity ticket;

    @BeforeEach
    void setUp() {
        customer = User.builder()
                .id(10L)
                .email("customer@mail.com")
                .username("customer")
                .role(Role.CUSTOMER)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        organizer = User.builder()
                .id(20L)
                .email("org@mail.com")
                .username("organizer")
                .role(Role.ORGANIZER)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        anotherOrganizer = User.builder()
                .id(21L)
                .email("other-org@mail.com")
                .username("otherOrganizer")
                .role(Role.ORGANIZER)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        admin = User.builder()
                .id(1L)
                .email("admin@mail.com")
                .username("admin")
                .role(Role.ADMIN)
                .authProvider(AuthProvider.LOCAL)
                .enabled(true)
                .build();

        EventEntity event = EventEntity.builder()
                .id(100L)
                .title("Sample event")
                .date(LocalDateTime.now().plusDays(5))
                .organizer(organizer)
                .location(LocationEntity.builder().id(1L).name("Hall").build())
                .build();

        TicketTypeEntity type = TicketTypeEntity.builder()
                .id(1000L)
                .name("General")
                .saleStartTime(LocalDateTime.now().minusDays(1))
                .saleEndTime(LocalDateTime.now().plusDays(1))
                .quantityTotal(100)
                .quantityReserved(0)
                .quantitySold(1)
                .maxPurchase(4)
                .event(event)
                .build();

        OrderEntity order = OrderEntity.builder()
                .id(500L)
                .orderNumber("ORD-1")
                .user(customer)
                .contactEmail(customer.getEmail())
                .status(OrderStatus.CONFIRMED)
                .build();

        ticket = TicketEntity.builder()
                .id(10000L)
                .ticketNumber("TKT-1")
                .qrCode("qr-1")
                .email(customer.getEmail())
                .status(TicketStatus.VALID)
                .ticketType(type)
                .order(order)
                .build();
    }

    @Test
    void getMyTickets_shouldReturnMappedTickets() {
        when(ticketRepository.findAllByUser(customer)).thenReturn(List.of(ticket));

        List<TicketResponse> result = ticketService.getMyTickets(customer);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getId()).isEqualTo(ticket.getId());
        assertThat(result.getFirst().getTicketTypeName()).isEqualTo("General");
    }

    @Test
    void getMyTicketById_shouldReturnMappedTicket() {
        when(ticketRepository.findByIdAndUser(ticket.getId(), customer)).thenReturn(Optional.of(ticket));

        TicketResponse result = ticketService.getMyTicketById(ticket.getId(), customer);

        assertThat(result.getId()).isEqualTo(ticket.getId());
        assertThat(result.getTicketNumber()).isEqualTo("TKT-1");
    }

    @Test
    void getMyTicketById_shouldThrow_whenNotOwned() {
        when(ticketRepository.findByIdAndUser(ticket.getId(), customer)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ticketService.getMyTicketById(ticket.getId(), customer))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Ticket not found");
    }

    @Test
    void verifyAndCheckInByQr_shouldAllowOrganizerOwner() {
        when(qrTokenService.extractTicketNumber("qr-1")).thenReturn("TKT-1");
        when(ticketRepository.findByTicketNumberWithDetails("TKT-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = ticketService.verifyAndCheckInByQr("qr-1", organizer);

        assertThat(response.getMessage()).isEqualTo("Ticket checked in successfully");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        assertThat(ticket.getCheckedInAt()).isNotNull();
        verify(ticketRepository).save(ticket);
    }

    @Test
    void verifyAndCheckInByQr_shouldAllowAdmin() {
        when(qrTokenService.extractTicketNumber("qr-1")).thenReturn("TKT-1");
        when(ticketRepository.findByTicketNumberWithDetails("TKT-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = ticketService.verifyAndCheckInByQr("qr-1", admin);

        assertThat(response.getMessage()).isEqualTo("Ticket checked in successfully");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
    }

    @Test
    void verifyAndCheckInByQr_shouldRejectAnotherOrganizer() {
        when(qrTokenService.extractTicketNumber("qr-1")).thenReturn("TKT-1");
        when(ticketRepository.findByTicketNumberWithDetails("TKT-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.verifyAndCheckInByQr("qr-1", anotherOrganizer))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void verifyAndCheckInByQr_shouldRejectCustomer() {
        when(qrTokenService.extractTicketNumber("qr-1")).thenReturn("TKT-1");
        when(ticketRepository.findByTicketNumberWithDetails("TKT-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.verifyAndCheckInByQr("qr-1", customer))
                .isInstanceOf(UnauthorizedActionException.class)
                .hasMessageContaining("not allowed");
    }

    @Test
    void verifyAndCheckInByQr_shouldThrow_whenAlreadyUsed() {
        ticket.setStatus(TicketStatus.USED);
        when(qrTokenService.extractTicketNumber("qr-1")).thenReturn("TKT-1");
        when(ticketRepository.findByTicketNumberWithDetails("TKT-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.verifyAndCheckInByQr("qr-1", organizer))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("already used");
    }

    @Test
    void verifyAndCheckInByQr_shouldCheckIn_whenOrganizerOwnsEvent() {
        when(qrTokenService.extractTicketNumber("qr-1")).thenReturn("TKT-1");
        when(ticketRepository.findByTicketNumberWithDetails("TKT-1")).thenReturn(Optional.of(ticket));
        when(ticketRepository.save(any(TicketEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MessageResponse response = ticketService.verifyAndCheckInByQr("qr-1", organizer);

        assertThat(response.getMessage()).isEqualTo("Ticket checked in successfully");
        assertThat(ticket.getStatus()).isEqualTo(TicketStatus.USED);
        verify(ticketRepository).save(ticket);
    }

    @Test
    void verifyAndCheckInByQr_shouldReject_whenQrDoesNotMatchStoredToken() {
        when(qrTokenService.extractTicketNumber("another-qr")).thenReturn("TKT-1");
        when(ticketRepository.findByTicketNumberWithDetails("TKT-1")).thenReturn(Optional.of(ticket));

        assertThatThrownBy(() -> ticketService.verifyAndCheckInByQr("another-qr", organizer))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("does not match");
    }

    @Test
    void verifyAndCheckInByQr_shouldPropagateInvalidQrTokenException() {
        when(qrTokenService.extractTicketNumber("bad-token"))
                .thenThrow(new InvalidQrTokenException("Invalid QR token"));

        assertThatThrownBy(() -> ticketService.verifyAndCheckInByQr("bad-token", organizer))
                .isInstanceOf(InvalidQrTokenException.class)
                .hasMessageContaining("Invalid QR token");
    }
}
