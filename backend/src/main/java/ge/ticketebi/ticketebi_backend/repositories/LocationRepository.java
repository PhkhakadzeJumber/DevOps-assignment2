package ge.ticketebi.ticketebi_backend.repositories;

import ge.ticketebi.ticketebi_backend.domain.entities.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    Optional<LocationEntity> findByName(String name);
}
