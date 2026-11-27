package ru.yandex.masterbot.adapter.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import ru.yandex.masterbot.adapter.persistence.entity.ApplicationEntity;
import ru.yandex.masterbot.adapter.persistence.entity.StatusHistoryEntity;
import ru.yandex.masterbot.adapter.persistence.mapper.ApplicationMapper;
import ru.yandex.masterbot.adapter.persistence.repository.ApplicationJpaRepository;
import ru.yandex.masterbot.adapter.persistence.repository.StatusHistoryJpaRepository;
import ru.yandex.masterbot.application.Requester;
import ru.yandex.masterbot.domain.Application;
import ru.yandex.masterbot.port.ApplicationQuery;

@Repository
public class ApplicationQueryAdapter implements ApplicationQuery {

  private final ApplicationJpaRepository applicationRepository;
  private final StatusHistoryJpaRepository historyRepository;
  private final ApplicationMapper mapper;

  public ApplicationQueryAdapter(
      ApplicationJpaRepository applicationRepository,
      StatusHistoryJpaRepository historyRepository,
      ApplicationMapper mapper) {
    this.applicationRepository = applicationRepository;
    this.historyRepository = historyRepository;
    this.mapper = mapper;
  }

  @Override
  public List<Application> findRecentByRequester(Requester requester, int limit) {
    if (requester == null || limit <= 0) {
      return List.of();
    }

    List<ApplicationEntity> entities;

    if (requester.userId() != null && !requester.userId().isBlank()) {
      entities =
          applicationRepository.findByRequesterChannelAndRequesterUserIdOrderByCreatedAtDesc(
              requester.channel(), requester.userId(), PageRequest.of(0, limit));
    } else if (requester.chatId() != null && !requester.chatId().isBlank()) {
      entities =
          applicationRepository.findByRequesterChannelAndRequesterChatIdOrderByCreatedAtDesc(
              requester.channel(), requester.chatId(), PageRequest.of(0, limit));
    } else {
      return List.of();
    }

    if (entities.isEmpty()) {
      return List.of();
    }

    List<UUID> applicationIds = entities.stream().map(ApplicationEntity::getId).toList();

    List<StatusHistoryEntity> history =
        historyRepository.findByApplicationIdInOrderByCreatedAtAsc(applicationIds);

    var historyByApplicationId =
        history.stream()
            .collect(java.util.stream.Collectors.groupingBy(item -> item.getApplication().getId()));

    return entities.stream()
        .map(entity ->
            mapper.toDomain(entity, historyByApplicationId.getOrDefault(entity.getId(), List.of())))
        .toList();
  }
}
