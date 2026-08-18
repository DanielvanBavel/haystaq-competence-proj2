package nl.haystaq.streamforge.entitlement.infrastructure;

import nl.haystaq.streamforge.entitlement.domain.Viewer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ViewerRepository extends JpaRepository<Viewer, UUID> {

    Optional<Viewer> findByEmailIgnoreCase(String email);

    List<Viewer> findAllByOrderByDisplayNameAsc();
}
