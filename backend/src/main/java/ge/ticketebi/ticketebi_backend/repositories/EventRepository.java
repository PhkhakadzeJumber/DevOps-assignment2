package ge.ticketebi.ticketebi_backend.repositories;

import ge.ticketebi.ticketebi_backend.domain.entities.EventEntity;
import ge.ticketebi.ticketebi_backend.domain.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<EventEntity, Long> {
    @Query("SELECT DISTINCT e FROM EventEntity e " +
            "LEFT JOIN FETCH e.categories " +
            "LEFT JOIN FETCH e.location " +
            "LEFT JOIN FETCH e.organizer " +
            "WHERE e.deleted = false")
    List<EventEntity> findAllWithDetails();

    Optional<EventEntity> findByIdAndDeletedFalse(Long id);

    @Query("SELECT DISTINCT e FROM EventEntity e " +
            "LEFT JOIN FETCH e.categories " +
            "LEFT JOIN FETCH e.location " +
            "LEFT JOIN FETCH e.organizer " +
            "WHERE e.deleted = false AND e.organizer = :organizer")
    List<EventEntity> findByOrganizerWithDetails(@Param("organizer") User organizer);
}
