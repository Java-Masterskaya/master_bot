package ru.yandex.masterbot.port;

import java.util.List;
import ru.yandex.masterbot.application.Requester;
import ru.yandex.masterbot.domain.Application;

/** Порт чтения заявок для внешних компонентов приложения. */
public interface ApplicationQuery {

  /**
   * Возвращает последние заявки указанного инициатора.
   *
   * @param requester инициатор заявок
   * @param limit максимальное количество результатов
   * @return список заявок от новых к старым
   */
  List<Application> findRecentByRequester(Requester requester, int limit);
}
