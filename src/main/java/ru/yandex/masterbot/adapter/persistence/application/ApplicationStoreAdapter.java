package ru.yandex.masterbot.adapter.persistence.application;

import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.masterbot.adapter.persistence.entity.ApplicationEntity;
import ru.yandex.masterbot.adapter.persistence.entity.StatusHistoryEntity;
import ru.yandex.masterbot.adapter.persistence.mapper.ApplicationMapper;
import ru.yandex.masterbot.adapter.persistence.repository.ApplicationJpaRepository;
import ru.yandex.masterbot.adapter.persistence.repository.StatusHistoryJpaRepository;
import ru.yandex.masterbot.application.Brief;
import ru.yandex.masterbot.application.Requester;
import ru.yandex.masterbot.application.application.ApplicationStore;
import ru.yandex.masterbot.domain.Application;
import ru.yandex.masterbot.domain.ApplicationStatus;

@Repository
public class ApplicationStoreAdapter implements ApplicationStore {

  private final ApplicationJpaRepository applicationRepository;
  private final StatusHistoryJpaRepository historyRepository;
  private final ApplicationMapper mapper;

  public ApplicationStoreAdapter(
      ApplicationJpaRepository applicationRepository,
      StatusHistoryJpaRepository historyRepository,
      ApplicationMapper mapper) {
    this.applicationRepository = applicationRepository;
    this.historyRepository = historyRepository;
    this.mapper = mapper;
  }

  @Override
  @Transactional
  public Application create(Brief brief, String source, Requester requester) {
    ApplicationEntity entity =
        ApplicationEntity.create(
            UUID.randomUUID(), source, brief, requester, Map.of(), Instant.now());

    ApplicationEntity savedEntity = applicationRepository.saveAndFlush(entity);

    return mapper.toDomain(savedEntity, List.of());
  }

  @Override
  @Transactional
  public Application transition(UUID id, ApplicationStatus target, String actor, String comment) {

    ApplicationEntity entity =
        applicationRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Application not found: " + id));

    List<StatusHistoryEntity> history =
        historyRepository.findByApplicationIdInOrderByCreatedAtAsc(List.of(id));

    Application application = mapper.toDomain(entity, history);

    Instant now = Instant.now();

    // Проверяем допустимость перехода средствами доменной модели.
    application.changeStatus(target, actor, comment, now);

    entity.changeStatus(target, now);
    ApplicationEntity savedEntity = applicationRepository.saveAndFlush(entity);

    StatusHistoryEntity historyEntry =
        new StatusHistoryEntity(entity, application.status(), target, actor, comment, now);

    historyRepository.saveAndFlush(historyEntry);

    List<StatusHistoryEntity> updatedHistory =
        historyRepository.findByApplicationIdInOrderByCreatedAtAsc(List.of(id));

    return mapper.toDomain(savedEntity, updatedHistory);
  }
}
