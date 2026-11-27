package ru.yandex.masterbot.adapter.persistence.mapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Component;
import ru.yandex.masterbot.adapter.persistence.entity.ApplicationEntity;
import ru.yandex.masterbot.adapter.persistence.entity.StatusHistoryEntity;
import ru.yandex.masterbot.application.Brief;
import ru.yandex.masterbot.application.Requester;
import ru.yandex.masterbot.domain.Application;
import ru.yandex.masterbot.domain.StatusChange;

@Component
public class ApplicationMapper {

  public ApplicationEntity toEntity(
      UUID id,
      String source,
      Brief brief,
      Requester requester,
      Map<String, Object> rawPayload,
      Instant createdAt) {

    return ApplicationEntity.create(id, source, brief, requester, rawPayload, createdAt);
  }

  public Application toDomain(ApplicationEntity entity, List<StatusHistoryEntity> historyEntities) {

    List<StatusChange> history =
        historyEntities.stream()
            .filter(item -> item.getOldStatus() != null)
            .map(item ->
                new StatusChange(
                    item.getOldStatus(),
                    item.getNewStatus(),
                    item.getChangedBy(),
                    item.getReason(),
                    item.getCreatedAt()))
            .toList();

    return Application.restore(
        entity.getId(),
        entity.getNumber(),
        entity.getSource(),
        entity.getStatus(),
        entity.getVersion(),
        history);
  }
}
