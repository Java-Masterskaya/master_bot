package ru.yandex.masterbot.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;

import jakarta.persistence.OptimisticLockException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import ru.yandex.masterbot.AbstractIntegrationTest;
import ru.yandex.masterbot.adapter.persistence.entity.ApplicationEntity;
import ru.yandex.masterbot.adapter.persistence.mapper.ApplicationMapper;
import ru.yandex.masterbot.adapter.persistence.repository.ApplicationJpaRepository;
import ru.yandex.masterbot.adapter.persistence.repository.StatusHistoryJpaRepository;
import ru.yandex.masterbot.application.application.ApplicationService;
import ru.yandex.masterbot.domain.Application;
import ru.yandex.masterbot.domain.ApplicationStatus;
import ru.yandex.masterbot.domain.IllegalStatusTransitionException;

class ApplicationServiceIntegrationTest extends AbstractIntegrationTest {

  @Autowired private ApplicationService applicationService;

  @Autowired private ApplicationJpaRepository applicationRepository;

  @MockitoSpyBean private ApplicationMapper applicationMapper;

  @Autowired private StatusHistoryJpaRepository historyRepository;

  @BeforeEach
  void cleanDatabase() {
    historyRepository.deleteAll();
    applicationRepository.deleteAll();
  }

  @Test
  void createApplicationPersistsApplicationWithGeneratedNumber() {
    Brief brief =
        new Brief(
            "Разработать CRM",
            "Отдел продаж",
            "CRM-система",
            "Автоматизация обработки заявок",
            "Интеграция с телефонией",
            "500000 рублей",
            "3 месяца");

    Requester requester = new Requester("TELEMOST", "user-123", "chat-456", "Иван");

    Application application = applicationService.create(brief, "TELEMOST", requester);

    assertNotNull(application.id());
    assertTrue(application.number() > 0);
    assertEquals(ApplicationStatus.NEW, application.status());
    assertEquals(0L, application.version());
    assertTrue(application.history().isEmpty());

    var savedEntity = applicationRepository.findById(application.id()).orElseThrow();

    assertEquals(application.number(), savedEntity.getNumber());
    assertEquals("TELEMOST", savedEntity.getSource());
    assertEquals(ApplicationStatus.NEW, savedEntity.getStatus());
    assertEquals(0L, savedEntity.getVersion());

    assertEquals("Разработать CRM", savedEntity.getBriefGoal());
    assertEquals("Отдел продаж", savedEntity.getBriefAudience());
    assertEquals("CRM-система", savedEntity.getBriefProduct());
    assertEquals("user-123", savedEntity.getRequesterUserId());
    assertEquals("chat-456", savedEntity.getRequesterChatId());
  }

  @Test
  void transitionApplicationUpdatesStatusAndCreatesHistory() {
    Application application =
        applicationService.create(
            new Brief(
                "Разработать CRM",
                "Отдел продаж",
                "CRM-система",
                "Автоматизация обработки заявок",
                "Интеграция с телефонией",
                "500000 рублей",
                "3 месяца"),
            "TELEMOST",
            new Requester("TELEMOST", "user-123", "chat-456", "Иван"));

    Application changedApplication =
        applicationService.transition(
            application.id(), ApplicationStatus.IN_REVIEW, "manager-123", "Взял заявку в работу");

    // Проверяем результат, возвращённый сервисом.
    assertEquals(ApplicationStatus.IN_REVIEW, changedApplication.status());
    assertEquals(1L, changedApplication.version());
    assertEquals(1, changedApplication.history().size());

    // Проверяем фактическое состояние заявки в БД.
    var savedEntity = applicationRepository.findById(application.id()).orElseThrow();

    assertEquals(ApplicationStatus.IN_REVIEW, savedEntity.getStatus());
    assertEquals(1L, savedEntity.getVersion());

    // Проверяем запись истории в БД.
    var history =
        historyRepository.findByApplicationIdInOrderByCreatedAtAsc(List.of(application.id()));

    assertEquals(1, history.size());

    var historyEntry = history.get(0);

    assertEquals(ApplicationStatus.NEW, historyEntry.getOldStatus());
    assertEquals(ApplicationStatus.IN_REVIEW, historyEntry.getNewStatus());
    assertEquals("manager-123", historyEntry.getChangedBy());
    assertEquals("Взял заявку в работу", historyEntry.getReason());
    assertNotNull(historyEntry.getCreatedAt());
  }

  @Test
  void transitionToInvalidStatusDoesNotChangeApplicationOrCreateHistory() {
    Application application =
        applicationService.create(
            new Brief(
                "Разработать CRM",
                "Отдел продаж",
                "CRM-система",
                "Автоматизация обработки заявок",
                "Интеграция с телефонией",
                "500000 рублей",
                "3 месяца"),
            "TELEMOST",
            new Requester("TELEMOST", "user-123", "chat-456", "Иван"));

    assertThrows(
        IllegalStatusTransitionException.class,
        () ->
            applicationService.transition(
                application.id(),
                ApplicationStatus.ACCEPTED,
                "manager-123",
                "Попытка принять новую заявку"));

    // Заявка должна остаться в исходном состоянии.
    var savedEntity = applicationRepository.findById(application.id()).orElseThrow();

    assertEquals(ApplicationStatus.NEW, savedEntity.getStatus());
    assertEquals(0L, savedEntity.getVersion());

    // История не должна содержать записей.
    var history =
        historyRepository.findByApplicationIdInOrderByCreatedAtAsc(List.of(application.id()));

    assertTrue(history.isEmpty());
  }

  @Test
  void concurrentTransitionsAllowOnlyOneSuccessfulUpdate() throws Exception {
    Application application =
        applicationService.create(
            new Brief(
                "Разработать CRM",
                "Отдел продаж",
                "CRM-система",
                "Автоматизация обработки заявок",
                "Интеграция с телефонией",
                "500000 рублей",
                "3 месяца"),
            "TELEMOST",
            new Requester("TELEMOST", "user-123", "chat-456", "Иван"));

    CountDownLatch bothLoaded = new CountDownLatch(2);
    AtomicInteger invocationCount = new AtomicInteger();

    doAnswer(
            invocation -> {
              ApplicationEntity entity = invocation.getArgument(0);

              if (entity.getVersion() == 0 && invocationCount.incrementAndGet() <= 2) {
                bothLoaded.countDown();

                if (!bothLoaded.await(10, TimeUnit.SECONDS)) {
                  throw new AssertionError("Both threads did not load the application");
                }
              }

              return invocation.callRealMethod();
            })
        .when(applicationMapper)
        .toDomain(any(ApplicationEntity.class), anyList());

    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      Future<Application> first =
          executor.submit(() ->
              applicationService.transition(
                  application.id(), ApplicationStatus.IN_REVIEW, "manager-1", "Первый менеджер"));

      Future<Application> second =
          executor.submit(() ->
              applicationService.transition(
                  application.id(), ApplicationStatus.IN_REVIEW, "manager-2", "Второй менеджер"));

      int successfulTransitions = 0;
      int optimisticLockFailures = 0;

      for (Future<Application> future : List.of(first, second)) {
        try {
          future.get(20, TimeUnit.SECONDS);
          successfulTransitions++;
        } catch (ExecutionException exception) {
          if (hasOptimisticLockException(exception)) {
            optimisticLockFailures++;
          } else {
            throw new AssertionError(
                "Unexpected exception during concurrent transition", exception);
          }
        }
      }

      assertEquals(1, successfulTransitions);
      assertEquals(1, optimisticLockFailures);

      var savedEntity = applicationRepository.findById(application.id()).orElseThrow();

      assertEquals(ApplicationStatus.IN_REVIEW, savedEntity.getStatus());
      assertEquals(1L, savedEntity.getVersion());

      var history =
          historyRepository.findByApplicationIdInOrderByCreatedAtAsc(List.of(application.id()));

      assertEquals(1, history.size());
    } finally {
      executor.shutdownNow();
    }
  }

  private boolean hasOptimisticLockException(Throwable throwable) {
    Throwable current = throwable;

    while (current != null) {
      if (current instanceof OptimisticLockException
          || current instanceof ObjectOptimisticLockingFailureException) {
        return true;
      }

      current = current.getCause();
    }

    return false;
  }
}
