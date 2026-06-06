package ge.ticketebi.ticketebi_backend.repositories;

import ge.ticketebi.ticketebi_backend.domain.entities.OrderEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.TicketEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<TicketEntity, Long> {
    Optional<TicketEntity> findByTicketNumber(String ticketNumber);

    Optional<TicketEntity> findByQrCode(String qrCode);

    @EntityGraph(attributePaths = {"ticketType", "ticketType.event", "order"})
    List<TicketEntity> findByOrder(OrderEntity order);

    @Query("""
           select t from TicketEntity t
           join fetch t.ticketType tt
           join fetch tt.event
           join fetch t.order o
           where o.user = :user
           order by t.createdAt desc
           """)
    List<TicketEntity> findAllByUser(@Param("user") User user);

    @Query("""
           select t from TicketEntity t
           join fetch t.ticketType tt
           join fetch tt.event
           join fetch t.order o
           where t.id = :ticketId and o.user = :user
           """)
    Optional<TicketEntity> findByIdAndUser(@Param("ticketId") Long ticketId, @Param("user") User user);

    @Query("""
           select t from TicketEntity t
           join fetch t.ticketType tt
           join fetch tt.event e
           join fetch e.organizer
           join fetch t.order
           where t.id = :ticketId
           """)
    Optional<TicketEntity> findByIdWithDetails(@Param("ticketId") Long ticketId);

    @Query("""
           select t from TicketEntity t
           join fetch t.ticketType tt
           join fetch tt.event e
           join fetch e.organizer
           join fetch t.order
           where t.ticketNumber = :ticketNumber
           """)
    Optional<TicketEntity> findByTicketNumberWithDetails(@Param("ticketNumber") String ticketNumber);
}
