package ru.yandex.masterbot.application.application;

import java.util.UUID;
import org.springframework.stereotype.Service;
import ru.yandex.masterbot.application.Brief;
import ru.yandex.masterbot.application.Requester;
import ru.yandex.masterbot.domain.Application;
import ru.yandex.masterbot.domain.ApplicationStatus;

@Service
public class ApplicationService {

  private final ApplicationStore applicationStore;

  public ApplicationService(ApplicationStore applicationStore) {
    this.applicationStore = applicationStore;
  }

  public Application create(Brief brief, String source, Requester requester) {
    return applicationStore.create(brief, source, requester);
  }

  public Application transition(UUID id, ApplicationStatus target, String actor, String comment) {
    return applicationStore.transition(id, target, actor, comment);
  }
}
