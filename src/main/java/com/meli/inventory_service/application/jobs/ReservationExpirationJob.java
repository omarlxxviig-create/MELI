package com.meli.inventory_service.application.jobs;

import com.meli.inventory_service.domain.ports.out.ReservationPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpirationJob {
    private static final Logger log = LoggerFactory.getLogger(ReservationExpirationJob.class);
    private final ReservationPort reservationPort;

    public ReservationExpirationJob(ReservationPort reservationPort) {
        this.reservationPort = reservationPort;
    }

    @Scheduled(fixedRate = 60000) // Ejecutar cada minuto
    public void processExpiredReservations() {
        int processed = 0;
        try {
            var expired = reservationPort.findExpiredReservations(java.time.LocalDateTime.now());
            if (!expired.isEmpty()) {
                log.info("Found {} expired reservations to process", expired.size());
            }

            for (var reservation : expired) {
                try {
                    log.info("Processing expired reservation id={}, userId={}, postId={}",
                            reservation.getId(),
                            reservation.getUserId(),
                            reservation.getPostId());

                    // Mark as expired using domain method
                    reservation.expire();
                    reservationPort.save(reservation);
                    processed++;
                } catch (Exception e) {
                    log.error("Failed to expire reservation id={}: {}",
                            reservation.getId(),
                            e.getMessage());
                }
            }
            if (processed > 0) {
                log.info("Successfully processed {} expired reservations", processed);
            }
        } catch (Exception e) {
            log.error("Error in expiration job: {}", e.getMessage(), e);
        }
    }
}
