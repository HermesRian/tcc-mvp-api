package com.petapp.petapp.scheduler;

import com.petapp.petapp.service.PetPostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Dispara periodicamente a sincronizacao de publicacoes da pagina do Facebook.
 * O intervalo e o atraso inicial podem ser alterados no application.properties
 * (facebook.sync.interval-ms e facebook.sync.initial-delay-ms).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PetPostSyncScheduler {

    private final PetPostService petPostService;

    @Scheduled(
        fixedDelayString = "${facebook.sync.interval-ms:1800000}",
        initialDelayString = "${facebook.sync.initial-delay-ms:60000}"
    )
    public void syncPeriodically() {
        log.info("Scheduled synchronization started");
        try {
            petPostService.syncPostsFromFacebook();
        } catch (Exception e) {
            // Falha numa execucao nao deve cancelar as proximas
            log.error("Scheduled synchronization failed: {}", e.getMessage());
        }
    }
}
