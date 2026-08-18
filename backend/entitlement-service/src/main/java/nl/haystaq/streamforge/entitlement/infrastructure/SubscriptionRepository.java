package nl.haystaq.streamforge.entitlement.infrastructure;

import nl.haystaq.streamforge.entitlement.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByViewerId(UUID viewerId);
}
