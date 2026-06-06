package ge.ticketebi.ticketebi_backend.repositories;

import ge.ticketebi.ticketebi_backend.domain.entities.TicketTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TicketTypeRepository extends JpaRepository<TicketTypeEntity, Long> {
    List<TicketTypeEntity> findByEventId(Long eventId);

    Optional<TicketTypeEntity> findByIdAndEventId(Long id, Long eventId);

    List<TicketTypeEntity> findByEventIdAndSaleStartTimeLessThanEqualAndSaleEndTimeGreaterThanEqual(
            Long eventId,
            LocalDateTime now1,
            LocalDateTime now2
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select tt from TicketTypeEntity tt where tt.id in :ids")
    List<TicketTypeEntity> findAllByIdInForUpdate(@Param("ids") Collection<Long> ids);

}
