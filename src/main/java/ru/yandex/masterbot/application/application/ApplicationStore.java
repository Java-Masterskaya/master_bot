package ru.yandex.masterbot.application.application;

import java.util.UUID;
import ru.yandex.masterbot.application.Brief;
import ru.yandex.masterbot.application.Requester;
import ru.yandex.masterbot.domain.Application;
import ru.yandex.masterbot.domain.ApplicationStatus;

public interface ApplicationStore {

  Application create(Brief brief, String source, Requester requester);

  Application transition(UUID id, ApplicationStatus target, String actor, String comment);
}
