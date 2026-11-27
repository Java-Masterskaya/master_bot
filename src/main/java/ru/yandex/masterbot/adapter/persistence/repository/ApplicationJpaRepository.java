package ru.yandex.masterbot.adapter.persistence.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.masterbot.adapter.persistence.entity.ApplicationEntity;

public interface ApplicationJpaRepository extends JpaRepository<ApplicationEntity, UUID> {

  List<ApplicationEntity> findByRequesterChannelAndRequesterUserIdOrderByCreatedAtDesc(
      String requesterChannel, String requesterUserId, Pageable pageable);

  List<ApplicationEntity> findByRequesterChannelAndRequesterChatIdOrderByCreatedAtDesc(
      String requesterChannel, String requesterChatId, Pageable pageable);
}
