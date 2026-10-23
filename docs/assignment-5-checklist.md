# Assignment 5 — проверка текущего TicketConcert

Проверено 23 октября 2026 года.

| Требование | Подтверждение |
|---|---|
| Две семьи по три разных алгоритма | PricingStrategy: base / demand / fixed fee; RiskCheckStrategy: blocklist / amount / history |
| Два контекста | PricingContext и PurchaseRiskChecker: constructor, setStrategy, делегирование |
| Селекторы отдельно | PricingSelector и RiskCheckSelector; выбор типа не находится в контекстах |
| Внешний источник за интерфейсом | PurchaseHistorySource, SimulatedPurchaseHistorySource, FixedPurchaseHistory fake в тестах |
| Ручной и автоматический выбор | Пункты 6/7 для покупки; Manual/Auto в обоих демо |
| Одинаковый вход для всех алгоритмов | StrategyDemo.pricing и StrategyDemo.risk |
| Переключение одного контекста | Контекст создаётся перед циклом, setStrategy вызывается на том же объекте |
| JUnit 5 | Тесты каждой стратегии, всех правил селекторов, параметризованные тесты обеих семей |
| История до/после | c076dd1 → 35e5d90; одинаковый PricingServiceTest сохранён |
| README | Все 33 production типа, ответственность ≤25 слов без if/and/or/but; сравнение паттернов; ссылки на коммиты |
| UML | Общая диаграмма и отдельная Strategy в SVG/PNG/DOT; изображения просмотрены |
| Демо из JAR | Пункты 8/10 проверены на реальном процессе Java; все четыре таблицы остались пустыми |

## Проверки

* Maven verify: **105 тестов**, 0 failures, 0 errors; BUILD SUCCESS.
* Строки бизнес-кода: **99.19%**; strategy.pricing и strategy.risk: **100% строк**.
* Унаследованный JaCoCo-порог ≥80% каждого бизнес-пакета пройден; app исключён из порога, но проверен тестами.
* Демо не меняет режимы реальной покупки: StrategyDemoTest.
* PricingServiceTest в двух исторических коммитах и текущих файлах имеет одинаковый текст.

## Сдача

Проект следует передавать с полной Git history, включая оба коммита до/после рефакторинга.
Веб-загрузка одних файлов эту историю не переносит.
Защита и live coding остаются обязательными.
