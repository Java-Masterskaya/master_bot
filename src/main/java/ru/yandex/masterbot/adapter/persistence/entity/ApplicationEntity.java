package ru.yandex.masterbot.adapter.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;
import ru.yandex.masterbot.application.Brief;
import ru.yandex.masterbot.application.Requester;
import ru.yandex.masterbot.domain.ApplicationStatus;

@Entity
@Table(name = "applications")
public class ApplicationEntity {

  @Id private UUID id;

  @Generated(event = EventType.INSERT)
  @Column(name = "number", nullable = false, unique = true, insertable = false, updatable = false)
  private Long number;

  @Column(name = "source", nullable = false, length = 50)
  private String source;

  @Column(name = "requester_channel", length = 50)
  private String requesterChannel;

  @Column(name = "requester_user_id")
  private String requesterUserId;

  @Column(name = "requester_chat_id")
  private String requesterChatId;

  @Column(name = "requester_name")
  private String requesterName;

  @Column(name = "brief_goal", columnDefinition = "text")
  private String briefGoal;

  @Column(name = "brief_audience", columnDefinition = "text")
  private String briefAudience;

  @Column(name = "brief_product", columnDefinition = "text")
  private String briefProduct;

  @Column(name = "brief_description", columnDefinition = "text")
  private String briefDescription;

  @Column(name = "brief_requirements", columnDefinition = "text")
  private String briefRequirements;

  @Column(name = "brief_budget", columnDefinition = "text")
  private String briefBudget;

  @Column(name = "brief_deadline", columnDefinition = "text")
  private String briefDeadline;

  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "raw_payload", nullable = false, columnDefinition = "jsonb")
  private Map<String, Object> rawPayload;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 50)
  private ApplicationStatus status;

  @Version
  @Column(name = "version", nullable = false)
  private long version;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  protected ApplicationEntity() {}

  @PreUpdate
  protected void onUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public Long getNumber() {
    return number;
  }

  public String getSource() {
    return source;
  }

  public String getRequesterChannel() {
    return requesterChannel;
  }

  public String getRequesterUserId() {
    return requesterUserId;
  }

  public String getRequesterChatId() {
    return requesterChatId;
  }

  public String getRequesterName() {
    return requesterName;
  }

  public String getBriefGoal() {
    return briefGoal;
  }

  public String getBriefAudience() {
    return briefAudience;
  }

  public String getBriefProduct() {
    return briefProduct;
  }

  public String getBriefDescription() {
    return briefDescription;
  }

  public String getBriefRequirements() {
    return briefRequirements;
  }

  public String getBriefBudget() {
    return briefBudget;
  }

  public String getBriefDeadline() {
    return briefDeadline;
  }

  public Map<String, Object> getRawPayload() {
    return rawPayload;
  }

  public ApplicationStatus getStatus() {
    return status;
  }

  public long getVersion() {
    return version;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }

  public static ApplicationEntity create(
      UUID id,
      String source,
      Brief brief,
      Requester requester,
      Map<String, Object> rawPayload,
      Instant createdAt) {

    ApplicationEntity entity = new ApplicationEntity();

    entity.id = Objects.requireNonNull(id);
    entity.source = Objects.requireNonNull(source);

    if (brief != null) {
      entity.briefGoal = brief.goal();
      entity.briefAudience = brief.audience();
      entity.briefProduct = brief.product();
      entity.briefDescription = brief.description();
      entity.briefRequirements = brief.requirements();
      entity.briefBudget = brief.budget();
      entity.briefDeadline = brief.deadline();
    }

    if (requester != null) {
      entity.requesterChannel = requester.channel();
      entity.requesterUserId = requester.userId();
      entity.requesterChatId = requester.chatId();
      entity.requesterName = requester.name();
    }

    entity.rawPayload = rawPayload == null ? Map.of() : new HashMap<>(rawPayload);
    entity.status = ApplicationStatus.NEW;
    entity.createdAt = Objects.requireNonNull(createdAt);
    entity.updatedAt = createdAt;

    return entity;
  }

  public void changeStatus(ApplicationStatus target, Instant updatedAt) {
    this.status = Objects.requireNonNull(target);
    this.updatedAt = Objects.requireNonNull(updatedAt);
  }
}
