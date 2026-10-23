# TicketConcert — Assignment 5: Strategy

Concert Ticketing на базе Assignment 4 (Facade). Java 17+, Maven, SQLite/JDBC, JUnit 5.
Новый код Assignment 5 находится в `src/strategy`, демонстрация — в `src/app/StrategyDemo.java`.

## Запуск

1. Открой `pom.xml` в IntelliJ как Maven-проект, загрузи Maven dependencies.
2. Выбери JDK 17+ и запусти `app.Main`. Рабочая папка — корень TicketConcert.
3. Для тестов и сборки: `mvn clean verify`.

После сборки:

```powershell
java -jar target/ticket-concert.jar
```

На JDK 24+ для SQLite можно добавить `--enable-native-access=ALL-UNNAMED` перед `-jar`.
Для PowerShell доступен `powershell -NoProfile -ExecutionPolicy Bypass -File .\run.ps1`;
этому скрипту нужны Maven в PATH и JDK через JAVA_HOME.
База создаётся в `data/concert.db`, сохраняется между запусками и не удаляется Maven clean.
Другой файл базы: `java -jar target/ticket-concert.jar data/demo.db`.

## Меню

| Пункт | Назначение |
|---|---|
| 1 | Покупка A → B → C с текущими режимами цены и риска |
| 2 | Чтение сохранённых статусов покупки |
| 3 | Компенсация незавершённой покупки, в том числе после перезапуска |
| 4 | Отказ B: покупатель 7 заблокирован |
| 5 | Отказ C: проход недоступен |
| 6 | Цена: auto / standard / demand / booking_fee; партнёрский признак 0/1 |
| 7 | Риск: auto / standard / large_purchase / frequent_purchase |
| 8 | Демо всех стратегий цены, ручного/автоматического выбора, runtime switching |
| 10 | Демо всех стратегий риска, ручного/автоматического выбора, runtime switching |
| 0 | Выход |

Режимы по умолчанию — `auto`; настройки действуют в текущем запуске.
Зал содержит 100 мест. Для новой покупки используй новый ID; покупатель 7 заблокирован.
Демо создаёт свои локальные контексты, не делает покупок и не меняет выбранные режимы реального меню.

## Семья 1: расчёт цены

Контракт: `PricingStrategy.calculatePrice(basePrice, reservedSeats, totalSeats)`.

| Стратегия | Логика |
|---|---|
| StandardPricing | Возвращает базовую цену |
| DemandPricing | При занятости от 50% добавляет 20%, иначе возвращает базовую цену |
| BookingFeePricing | Добавляет фиксированную комиссию 500 |

`PricingContext` получает стратегию в конструкторе, меняет её через setStrategy и делегирует расчёт.
`PricingSelector` поддерживает ручной выбор по имени. Автоматические правила по приоритету:
партнёрская покупка → BookingFeePricing; иначе занятость от 50% → DemandPricing; иначе StandardPricing.
Занятость берётся для выбранного концерта перед резервированием нового места.

## Семья 2: проверка риска

Контракт: `RiskCheckStrategy.check(customerId, amount)`; отказ обозначается IllegalArgumentException.

| Стратегия | Логика |
|---|---|
| StandardRiskCheck | Проверяет блоклист |
| LargePurchaseRiskCheck | Проверяет блоклист, затем запрещает итоговую сумму больше 50000 |
| FrequentPurchaseRiskCheck | Проверяет блоклист, затем запрещает покупку при 3 и более предыдущих покупках за час |

`PurchaseRiskChecker` — контекст с constructor/setStrategy/делегированием, без выбора типа стратегии.
`RiskCheckSelector` поддерживает ручные имена. Автоматические правила по приоритету:
высокий спрос (занято от 50%) → FrequentPurchaseRiskCheck; иначе сумма от 50000 → LargePurchaseRiskCheck; иначе StandardRiskCheck.
При высоком спросе частотная проверка имеет приоритет перед проверкой суммы.
Это альтернативные алгоритмы: выполняется выбранный, а блоклист проверяется в каждом.
Ровно 50000 выбирает проверку суммы, но не нарушает её лимит.

## Внешние данные за интерфейсом

FrequentPurchaseRiskCheck зависит от собственного `PurchaseHistorySource`.
Его метод countRecentPurchases(customerId) предоставляет число покупок за последний час.
`SimulatedPurchaseHistorySource` имитирует ответ внешнего сервиса фиксированным снимком Map:
в меню у покупателя 30 заданы 3 покупки, у отсутствующего клиента — 0.
Новые записи локальной SQLite этот снимок не меняют; реального API здесь нет, что разрешено заданием.
В `RiskStrategiesTest` отдельный fake `FixedPurchaseHistory` возвращает фиксированное число.
Замена источника требует новой реализации интерфейса и её передачи в точке сборки объектов.

## Демо на защите

Пункт **8** использует один вход: base=5000, reserved=70, total=100.

```text
standard = 5000
demand = 6000
booking_fee = 5500
Manual: booking_fee = 5500
Auto: DemandPricing = 6000
```

Пункт **10** использует один вход: customer=30, amount=60000, previous purchases=3.

```text
standard = ACCEPTED
large_purchase = REJECTED
frequent_purchase = REJECTED
Manual: standard = ACCEPTED
Auto: FrequentPurchaseRiskCheck = REJECTED
```

Каждый метод создаёт ровно один контекст перед циклом, затем вызывает setStrategy на нём.
Входные данные не меняются. Риск-демо перехватывает ожидаемый отказ, печатает результат и продолжает цикл.
Это позволяет сравнить все алгоритмы даже после отказа одного из них.

## Подключение к Facade

Main выбирает стратегию цены через Selector и устанавливает её в общий PricingContext.
SeatingChart рассчитывает цену через этот контекст и сохраняет бронь (A).
UserFacede передаёт итоговую reservation.price в TicketPrinter (B).
Конструктор принтера для меню получает режим, селектор риска и признак спроса.
Непосредственно перед проверкой он выбирает стратегию с учётом уже рассчитанной суммы.
Успешная проверка позволяет создать билет; C сохраняет доступ с той же итоговой суммой.
При отказе B/C существующий AdminFacade компенсирует выполненные шаги в обратном порядке.
Статусы и SQLite-схема Assignment 4 сохранены. Старые конструкторы остаются совместимыми со старыми вызовами.
DynamicPricingEngine оставлен как часть Assignment 4; текущий путь покупки использует PricingContext.

## Классы и ответственность

Таблица охватывает все 33 production класса/интерфейса. Каждая ответственность — одно предложение до 25 слов без if/and/or/but.

| Класс / интерфейс | Responsibility |
|---|---|
| `strategy.pricing.PricingStrategy` | Defines the ticket price calculation contract. |
| `strategy.pricing.StandardPricing` | Returns the base ticket price. |
| `strategy.pricing.DemandPricing` | Applies a twenty percent markup when at least half the seats are reserved. |
| `strategy.pricing.BookingFeePricing` | Adds a fixed booking fee to the ticket price. |
| `strategy.pricing.PricingContext` | Delegates price calculation to its replaceable strategy. |
| `strategy.pricing.PricingSelector` | Chooses a pricing algorithm from the current booking situation. |
| `strategy.pricing.PricingService` | Preserves the original price calculation API after refactoring. |
| `strategy.risk.RiskCheckStrategy` | Defines the purchase risk checking contract. |
| `strategy.risk.StandardRiskCheck` | Checks the customer blocklist. |
| `strategy.risk.LargePurchaseRiskCheck` | Rejects blocked customers before enforcing the purchase amount limit. |
| `strategy.risk.FrequentPurchaseRiskCheck` | Rejects blocked customers before checking recent purchase frequency. |
| `strategy.risk.PurchaseRiskChecker` | Delegates purchase checks to its replaceable strategy. |
| `strategy.risk.RiskCheckSelector` | Chooses a risk checking algorithm using purchase conditions. |
| `strategy.risk.PurchaseHistorySource` | Defines access to recent customer purchase counts. |
| `strategy.risk.SimulatedPurchaseHistorySource` | Supplies a fixed snapshot of recent purchase counts. |
| `app.Main` | Handles console commands for ticket purchases. |
| `app.StrategyDemo` | Demonstrates replaceable algorithms on identical inputs. |
| `reservation.SeatReservation` | Maintains the lifecycle of a seat reservation. |
| `reservation.ReservationRepository` | Persists seat reservations in SQLite. |
| `reservation.SeatingChart` | Creates reservations priced through the selected strategy. |
| `reservation.DynamicPricingEngine` | Retains the original Assignment 4 demand price calculation. |
| `processing.FraudChecker` | Rejects customers present in the configured blocklist. |
| `processing.TicketProcessing` | Maintains the lifecycle of a printed ticket. |
| `processing.TicketProcessingRepository` | Persists ticket processing records in SQLite. |
| `processing.TicketPrinter` | Issues tickets after selected risk checks succeed. |
| `finalization.TicketAccess` | Maintains the lifecycle of concert access. |
| `finalization.TicketAccessRepository` | Persists concert access records in SQLite. |
| `finalization.RefundService` | Calculates the refundable amount for active access. |
| `finalization.AccessControlGate` | Manages ticket access activation. |
| `Facedes.PurchaseWorkflow` | Maintains the lifecycle of a purchase workflow. |
| `Facedes.PurchaseWorkflowRepository` | Persists purchase workflows in SQLite. |
| `Facedes.UserFacede` | Coordinates the complete ticket purchase workflow. |
| `Facedes.AdminFacade` | Coordinates recovery of incomplete purchase workflows. |

Диаграммы:

* Strategy и точки подключения: [SVG](docs/strategy-uml.svg), [PNG](docs/strategy-uml.png), [DOT](docs/strategy-uml.dot).
* Весь проект Facade + Strategy: [SVG](docs/uml.svg), [PNG](docs/uml.png), [DOT](docs/uml.dot).
* Исходные материалы Assignment 4 сохранены в `docs/assignment-4/` как историческая версия.

## Почему Strategy, а не State или Template Method

В проекте взаимозаменяемые алгоритмы выполняют одну операцию: расчёт цены либо проверку риска.
Контекст хранит объект интерфейса и делегирует ему вызов. Клиент может заменить этот объект через setStrategy во время работы.
Автоматическое решение принимает отдельный Selector, а не контекст. Новую стратегию можно передать в существующий контекст без его изменения;
для выбора новой стратегии по имени потребуется добавить имя в Selector.
State связывает поведение с текущим состоянием объекта и переходами между состояниями.
Наши стратегии не переводят контекст из состояния в состояние: они являются выбранными способами расчёта или проверки.
Статусы покупки из Assignment 4 существуют отдельно от этой системы алгоритмов.
Template Method задавал бы каркас операции в базовом классе с переопределением шагов через наследование.
У нас алгоритм заменяется через композицию: контекст содержит объект стратегии.
Поэтому основой решения является Strategy. Условия внутри алгоритмов, например порог суммы, относятся к самой проверке;
ветвление по выбору алгоритма вынесено в Selector.

## История рефакторинга

* [До: c076dd1 — PricingService со switch](https://github.com/Temirlan222/Stratefy-patter-sdp/commit/c076dd17328fa2ef813b8de37bb82eda76807a28).
* [После: 35e5d90 — выделены стратегии цены](https://github.com/Temirlan222/Stratefy-patter-sdp/commit/35e5d90e7bec0dd8925172913bf549040afdca0b).

`test/strategy/pricing/PricingServiceTest.java` содержит те же пять тестов на обоих коммитах.
Они проверяют сохранение публичного контракта при рефакторинге.
Коммиты существуют в истории проекта; ссылки открываются после публикации соответствующих коммитов в GitHub.
Веб-загрузка одних файлов не переносит Git history; для сдачи нужно загрузить коммиты обычным Git push с авторизацией.
Для переноса проекта сохраняй полную историю, включая оба коммита рефакторинга.

## Тесты

`mvn clean verify` запускает JUnit 5, создаёт JAR и проверяет унаследованный порог JaCoCo 80% строк каждого бизнес-пакета.
`app` исключён из порога, но меню покрыто интеграционными тестами.

* PricingServiceTest: неизменный контракт до/после.
* PricingStrategiesTest / RiskStrategiesTest: каждый алгоритм, границы условий, параметризованные проверки обеих семей.
* PricingSelectorTest / RiskCheckSelectorTest: имена, неизвестное имя, все автоматические правила, приоритеты.
* PricingContextTest / PurchaseRiskCheckerTest: делегирование аргументов и смена стратегии того же контекста.
* SimulatedPurchaseHistorySourceTest: заданные значения истории и отсутствующий клиент.
* PurchaseRiskIntegrationTest: итоговая цена передаётся в проверку риска; отказ освобождает бронь.
* PricingMenuTest / RiskMenuTest: ручной/автоматический режим, SQLite-результаты, блоклист и компенсация.
* StrategyDemoTest: оба демо, продолжение после отказа, сохранность настроек реальной покупки.
* Старые тесты Assignment 4: состояния, репозитории, Facade, восстановление и компенсации.

Отчёты: `target/surefire-reports`, `target/site/jacoco/index.html`.
Проверенные результаты текущей версии: [чек-лист Assignment 5](docs/assignment-5-checklist.md).
Защита с live coding остаётся обязательной частью Assignment 5.
