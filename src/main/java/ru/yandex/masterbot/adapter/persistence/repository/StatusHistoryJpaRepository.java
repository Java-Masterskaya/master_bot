package ru.yandex.masterbot.adapter.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.masterbot.adapter.persistence.entity.StatusHistoryEntity;

public interface StatusHistoryJpaRepository extends JpaRepository<StatusHistoryEntity, UUID> {
  List<StatusHistoryEntity> findByApplicationIdInOrderByCreatedAtAsc(
      Collection<UUID> applicationIds);
}
