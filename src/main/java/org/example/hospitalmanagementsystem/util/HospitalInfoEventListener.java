package org.example.hospitalmanagementsystem.util;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.hospitalmanagementsystem.service.VectorIndexService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class HospitalInfoEventListener {


    private final VectorIndexService vectorIndexService;
    @Async("vectorTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHospitalInfoCreated(HospitalInfoCreatedEvent event) {

        try {

            vectorIndexService.indexDocument( event.id(),
                    event.category(),
                    event.title(),
                    event.description());

            log.info("Hospital info id={} indexed successfully", event.id());

        } catch (Exception e) {
            log.error("Failed to index hospital info id={}", event.id(), e);
        }
    }
    @Async("vectorTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHospitalInfoUpdated(HospitalInfoUpdatedEvent event) {

        try {


            vectorIndexService.updateDocument(event.id(),event.category(),event.title(), event.description());

            log.info("Hospital info id={} re-indexed successfully", event.id());

        } catch (Exception e) {

            log.error("Failed to re-index hospital info id={}", event.id(), e);
        }


    }
    @Async("vectorTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHospitalInfoDeactivated(HospitalInfoDeactivatedEvent event) {

        try {
            vectorIndexService.deactivateDocument(
                    event.hospitalInfoId(), event.category(), event.title(), event.description());
            log.info("Hospital info id={} marked inactive in vector index", event.hospitalInfoId());
        } catch (Exception e) {
            log.error("Failed to deactivate hospital info id={} in vector index", event.hospitalInfoId(), e);
        }
    }

    @Async("vectorTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHospitalInfoReactivated(HospitalInfoActivateEvent event) {

        try {
            vectorIndexService.reactivateDocument(
                    event.hospitalInfoId(), event.category(), event.title(), event.description());
            log.info("Hospital info id={} marked activate in vector index", event.hospitalInfoId());
        } catch (Exception e) {
            log.error("Failed to reactivate hospital info id={} in vector index", event.hospitalInfoId(), e);
        }
    }

    @Async("vectorTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleHospitalInfoDeleted(HospitalInfoDeleteEvent event) {

        try {
            vectorIndexService.deleteDocument(event.hospitalInfoId());
            log.info("Hospital info id={} delete in vector index", event.hospitalInfoId());
        } catch (Exception e) {
            log.error("Failed to delete hospital info id={} in vector index", event.hospitalInfoId(), e);
        }
    }


}