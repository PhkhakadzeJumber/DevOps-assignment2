package ge.ticketebi.ticketebi_backend.repositories;

import ge.ticketebi.ticketebi_backend.domain.entities.OrderEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.OrderStatus;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {
            "orderItems",
            "orderItems.ticketType"
    })
    List<OrderEntity> findByUserOrderByCreatedAtDesc(User user);

    @EntityGraph(attributePaths = {
            "orderItems",
            "orderItems.ticketType"
    })
    Optional<OrderEntity> findByIdAndUser(Long id, User user);

    @Query("""
           select o from OrderEntity o
           left join fetch o.orderItems oi
           left join fetch oi.ticketType
           where o.id = :id
           """)
    Optional<OrderEntity> findByIdWithTickets(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "orderItems",
            "orderItems.ticketType"
    })
    List<OrderEntity> findByStatusAndExpiresAtBefore(OrderStatus status, LocalDateTime now);
}
