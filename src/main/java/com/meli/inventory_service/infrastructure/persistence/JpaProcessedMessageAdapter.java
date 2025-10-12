package com.meli.inventory_service.infrastructure.persistence;

import com.meli.inventory_service.domain.ports.out.ProcessedMessagePort;
import com.meli.inventory_service.domain.model.ProcessedMessage;
import com.meli.inventory_service.infrastructure.persistence.spring.ProcessedMessageRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Adaptador JPA para el puerto ProcessedMessagePort.
 * Implementa las operaciones de persistencia para mensajes procesados,
 * necesarios para garantizar idempotencia en el procesamiento de eventos.
 */
@Component
public class JpaProcessedMessageAdapter implements ProcessedMessagePort {
    private final ProcessedMessageRepository repository;

    public JpaProcessedMessageAdapter(ProcessedMessageRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ProcessedMessage> findByMessageIdAndConsumerName(String messageId, String consumerName) {
        return repository.findByMessageIdAndConsumerName(messageId, consumerName);
    }

    @Override
    public ProcessedMessage save(ProcessedMessage m) {
        return repository.save(m);
    }
}
