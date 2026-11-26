package ru.yandex.masterbot.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.UuidGenerator;
import ru.yandex.masterbot.domain.ApplicationStatus;

@Entity
@Table(name = "application_status_history")
public class StatusHistoryEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @UuidGenerator
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "application_id", nullable = false)
  private ApplicationEntity application;

  @Enumerated(EnumType.STRING)
  @Column(name = "old_status", length = 50)
  private ApplicationStatus oldStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", nullable = false, length = 50)
  private ApplicationStatus newStatus;

  @Column(name = "changed_by", length = 255)
  private String changedBy;

  @Column(name = "reason", columnDefinition = "text")
  private String reason;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  protected StatusHistoryEntity() {}

  public StatusHistoryEntity(
      ApplicationEntity application,
      ApplicationStatus oldStatus,
      ApplicationStatus newStatus,
      String changedBy,
      String reason,
      Instant createdAt) {
    this.application = application;
    this.oldStatus = oldStatus;
    this.newStatus = newStatus;
    this.changedBy = changedBy;
    this.reason = reason;
    this.createdAt = createdAt;
  }

  public UUID getId() {
    return id;
  }

  public ApplicationEntity getApplication() {
    return application;
  }

  public ApplicationStatus getOldStatus() {
    return oldStatus;
  }

  public ApplicationStatus getNewStatus() {
    return newStatus;
  }

  public String getChangedBy() {
    return changedBy;
  }

  public String getReason() {
    return reason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
