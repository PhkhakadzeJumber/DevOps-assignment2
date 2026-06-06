package ge.ticketebi.ticketebi_backend.repositories;

import ge.ticketebi.ticketebi_backend.domain.entities.OrderEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItemEntity, Long> {
    List<OrderItemEntity> findByOrder(OrderEntity order);
}
