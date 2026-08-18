package nl.haystaq.streamforge.entitlement.application;

import nl.haystaq.streamforge.common.ServiceException;
import nl.haystaq.streamforge.entitlement.domain.Subscription;
import nl.haystaq.streamforge.entitlement.domain.Viewer;
import nl.haystaq.streamforge.entitlement.infrastructure.SubscriptionRepository;
import nl.haystaq.streamforge.entitlement.infrastructure.ViewerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class EntitlementService {

    private static final Logger log = LoggerFactory.getLogger(EntitlementService.class);

    private final ViewerRepository viewers;
    private final SubscriptionRepository subscriptions;

    public EntitlementService(ViewerRepository viewers, SubscriptionRepository subscriptions) {
        this.viewers = viewers;
        this.subscriptions = subscriptions;
    }

    /** Wat playback-service aanlevert om een beslissing te kunnen nemen. */
    public record EntitlementRequest(
            UUID viewerId,
            UUID episodeId,
            int maturityRating,
            Set<String> availableRegions,
            int activeStreams) {
    }

    public record EntitlementDecision(
            boolean allowed,
            String reason,
            String plan,
            String maxQuality,
            int maxConcurrentStreams,
            int activeStreams,
            String viewerRegion) {
    }

    public record ViewerView(UUID id, String email, String displayName, String region, int maturityLimit,
                             String plan, String subscriptionStatus, int maxConcurrentStreams, String maxQuality) {
    }

    public List<ViewerView> viewers() {
        return viewers.findAllByOrderByDisplayNameAsc().stream().map(this::toView).toList();
    }

    public ViewerView viewer(UUID id) {
        return toView(viewers.findById(id)
                .orElseThrow(() -> ServiceException.notFound("viewer_not_found", "no viewer " + id)));
    }

    /**
     * De volgorde van de controles bepaalt welke reden je terugkrijgt. Dat is
     * relevant als je een storing analyseert: de eerste reden die faalt wint.
     */
    public EntitlementDecision check(EntitlementRequest request) {
        Viewer viewer = viewers.findById(request.viewerId())
                .orElseThrow(() -> ServiceException.notFound("viewer_not_found", "no viewer " + request.viewerId()));
        Subscription subscription = subscriptions.findByViewerId(viewer.id())
                .orElseThrow(() -> ServiceException.notFound("subscription_not_found",
                        "viewer " + viewer.id() + " has no subscription"));

        if (!subscription.isActive()) {
            return deny(viewer, subscription, request, "subscription_inactive");
        }
        if (!request.availableRegions().contains(viewer.region())) {
            log.warn("region check failed: viewer {} is in {} but title is licensed for {}",
                    viewer.email(), viewer.region(), request.availableRegions());
            return deny(viewer, subscription, request, "region_blocked");
        }
        if (!viewer.mayWatch(request.maturityRating())) {
            return deny(viewer, subscription, request, "maturity_blocked");
        }
        if (!subscription.allowsAnotherStream(request.activeStreams())) {
            log.warn("stream limit reached: viewer {} plan {} allows {} streams, playback reports {} active",
                    viewer.email(), subscription.plan(), subscription.plan().maxConcurrentStreams(),
                    request.activeStreams());
            return deny(viewer, subscription, request, "stream_limit_reached");
        }

        log.info("entitlement granted for viewer {} episode {}", viewer.email(), request.episodeId());
        return new EntitlementDecision(true, "ok", subscription.plan().name(), subscription.plan().maxQuality(),
                subscription.plan().maxConcurrentStreams(), request.activeStreams(), viewer.region());
    }

    private EntitlementDecision deny(Viewer viewer, Subscription subscription, EntitlementRequest request,
                                     String reason) {
        log.info("entitlement denied for viewer {} episode {}: {}", viewer.email(), request.episodeId(), reason);
        return new EntitlementDecision(false, reason, subscription.plan().name(), subscription.plan().maxQuality(),
                subscription.plan().maxConcurrentStreams(), request.activeStreams(), viewer.region());
    }

    private ViewerView toView(Viewer viewer) {
        Subscription subscription = subscriptions.findByViewerId(viewer.id()).orElse(null);
        return new ViewerView(
                viewer.id(),
                viewer.email(),
                viewer.displayName(),
                viewer.region(),
                viewer.maturityLimit(),
                subscription == null ? null : subscription.plan().name(),
                subscription == null ? null : subscription.status().name(),
                subscription == null ? 0 : subscription.plan().maxConcurrentStreams(),
                subscription == null ? null : subscription.plan().maxQuality());
    }
}
