# Sync-storage: что нового и чеклист для адаптеров

Документ для авторов адаптеров (Python, JS, .NET, Java и т.д.): что изменилось в sync-storage и что нужно сделать на стороне клиента.

Референсная реализация: **adapters-java** (`testit-java-commons` → `ru.testit.syncstorage.*`).  
Сервер: **adapters-core** / sync-storage. Java: `SYNC_STORAGE_VERSION` — floor+канал; рантайм берёт последний патч в той же `major.minor` (`v0.4.4` → только numeric `v0.4.*`; `v0.4.3-tms-6.0` → только `v0.4.*-tms-6.0`).

Связанные материалы на стороне сервера: `adapters-core/docs/KEEP_ALIVE.md`, `adapters-core/docs/ENV.md`.

---

## 1. Зачем это нужно

Две типичные проблемы в проде/CI:

| Симптом | Причина |
|---------|---------|
| WARN `keep_alive: worker not found` каждые ~25 с даже в зелёном прогоне | Адаптер шлёт `POST /keep_alive`, а бинарник sync-storage **без** этого route → HTTP 404, клиент трактует как «воркер не найден» |
| Прогон создаётся, результаты местами уходят, но **не финализируется**; sync-storage висит на порту вечно | После падения JVM остаётся процесс sync-storage; следующий запуск **молча reuse** занятого порта с **другим** `--testRunId`; воркер/статусы не сходятся, Work X не отрабатывает для нового прогона |

---

## 2. Что сделано в sync-storage (сервер)

### 2.1 `POST /keep_alive` (снова в контракте)

| | |
|--|--|
| Method / path | `POST /keep_alive` |
| Body | `{"pid":"<workerId>","testRunId":"<uuid>"}` |
| 200 | Keep-alive принят; обновлены global idle (`LastActivity`) и worker `Heartbeat` / `LastStatus`, без смены `status` |
| 400 | Пустые поля / битый JSON |
| 404 | Воркер не зарегистрирован для этого `testRunId` (тело: `Worker not found`) |
| 405 | Не `POST` |

Интервал клиента: **20–30 с** (в Java — 25 с). Подробности и примеры: `KEEP_ALIVE.md`.

### 2.2 `GET /health` расширен

Ответ (additive):

```json
{
  "status": "ok",
  "testRunId": "<primary-test-run-uuid>",
  "last_update": "2026-11-20T10:00:00Z"
}
```

`testRunId` — тот, с которым процесс **запущен** (`--testRunId` / primary). Старые клиенты, читающие только `status`, не ломаются.

### 2.3 Жёсткая привязка к одному testRunId

- Процесс стартует с одним primary `testRunId`.
- `POST /register` с **другим** `testRunId` → **HTTP 409** и текст вида  
  `sync-storage is bound to testRunId <old>, got <new>`.
- Параллельные воркеры (xdist / несколько JVM) по-прежнему ок: тот же `testRunId`, разные `pid`.

### 2.4 Orphan / lifecycle cleanup

- После успешного Work X воркеры контекста снимаются (процесс может дойти до shutdown).
- Воркер не в `completed`, у которого `Heartbeat` старше autocomplete-fallback (дефолт **180 с**, env `TMS_SYNC_STORAGE_AUTOCOMPLETE_FALLBACK_S`) — считается **orphan** и удаляется (типичный случай: JVM умерла, SS остался).
- Idle shutdown по-прежнему смотрит на отсутствие HTTP-активности (`SYNC_STORAGE_IDLE_TIMEOUT` / `--idle-timeout`, дефолт `10m`).

### 2.5 Прочее (сервер)

- `UpdateWorkerStatus` не падает, если контекст/`testRunId` неизвестен (лог + no-op).
- Keep-alive не меняет статус воркера — только таймеры.

---

## 3. Что сделано в Java-адаптере (референс)

Файлы: `SyncStorageRunner`, `ClientWrapper`, `SyncStorageKeepAlive`, `SyncStorageService`.

1. **Keep-alive** после успешного `/register`; стоп при `completed`.
2. Логирование 404 keep-alive:
   - тело содержит `Worker not found` → WARN;
   - иначе (нет route / generic 404) → DEBUG, без спама «worker not found».
3. **Перед reuse** занятого порта:
   - `GET /health` → сравнить `testRunId` с текущим;
   - mismatch → `IllegalStateException` с понятным текстом (порт, чужой id, свой id); **не** глотать как «continue without sync-storage»;
   - поля нет (старый бинарник) → WARN, reuse как раньше (обратная совместимость).
4. При reuse регистрация воркера **обязательна**; провал после ретраев → ошибка.
5. Shutdown-hook: `destroy()` дочернего процесса, если адаптер сам его породил (не Linux `setsid`-detached — там SS намеренно переживает воркер для параллели).

> **Done in Java:** foreign `testRunId` → **port+1**; owned child → **`/wait-completion` before `destroy`**. Spec / porting: [sync-storage-port-bump-and-wait-completion.md](./sync-storage-port-bump-and-wait-completion.md).

Pin версии: `SYNC_STORAGE_VERSION` в `SyncStorageRunner` — поднять после публикации релиза бинарника.

---

## 4. Чеклист для других адаптеров

Сделать у себя то же поведение. Порядок — от критичного к желательному.

### 4.1 Обязательно: совместимость порта / testRunId

**Было (TMS-43088):** mismatch → fail-fast.  
**Сейчас (Java):** mismatch → `port+1` … `+maxBump` (см. [sync-storage-port-bump-and-wait-completion.md](./sync-storage-port-bump-and-wait-completion.md)).

```text
if GET /health == 200:
  runningId = health.testRunId
  if runningId is present AND runningId != myTestRunId:
    try port+1 .. preferred+maxBump; FAIL if none free
  else:
    attach / register on existing instance
else:
  start new sync-storage --testRunId {myTestRunId} --port ...
```

- Не логировать mismatch как debug и не **attach** к чужому `testRunId`.
- После attach: `/register` должен **успеть**; иначе — ошибка, а не тихий прогон без воркера.
- Ожидать **409** на `/register` при чужом `testRunId` (после обновления сервера) и показывать тот же смысл пользователю.

### 4.2 Обязательно: keep-alive

- После `/register` (или с момента `in_progress`) — фоновый цикл `POST /keep_alive` каждые **20–30 с** с тем же `pid` + `testRunId`.
- Остановить при `completed` / конце прогона / shutdown.
- Ошибки keep-alive **не** валят тесты:
  - 404 + `Worker not found` → warn (опционально);
  - 404 без этого текста / сеть → debug/trace, без ложного «worker not found».
- Не использовать GET; только **POST** + JSON body.

### 4.3 Желательно: жизненный цикл процесса

| Сценарий | Рекомендация |
|----------|----------------|
| Адаптер сам стартует SS как child | Shutdown-hook / `destroy` при выходе JVM/процесса (где OS позволяет) |
| Detached / `setsid` / общий SS на несколько воркеров | Не убивать SS из одного воркера; полагаться на orphan cleanup + idle + явный `wait-completion` в CI |
| CI | Старт SS с `--testRunId`; в конце сначала `GET /health`, и только если жив — `wait-completion`. Уже остановленный SS = успех, не `Connection refused` в логе |

### 4.4 Версия бинарника

- Pin sync-storage на релиз **с** `/keep_alive` и `testRunId` в `/health`.
- Документировать в README адаптера: при ошибке «another testRunId» — убить процесс на порту или сменить порт.

### 4.5 Не ломать старые серверы (если нужно)

| Фича сервера | Старый SS без фичи | Поведение клиента |
|--------------|--------------------|-------------------|
| `testRunId` в `/health` | поля нет | WARN + reuse (как раньше) **или** жёсткий fail — выбрать политику; в Java — WARN |
| `POST /keep_alive` | 404 generic | не спамить WARN «worker not found» |
| 409 на `/register` | не будет | mismatch ловится только через health у нового SS |

---

## 5. Минимальный happy-path клиента

```text
1. Resolve testRunId (создать прогон / взять из конфига)
2. GET /health на порту
   - 200 + тот же testRunId → attach
   - 200 + другой testRunId → FAIL
   - не 200 → spawn syncstorage --testRunId ... --port ... --baseURL ...
3. POST /register { pid, testRunId }
4. Start keep-alive loop (25s)
5. POST /set_worker_status in_progress / completed по ходу тестов
6. (master) дождаться Work X: GET /wait-completion?testRunId=...
7. Stop keep-alive
```

`pid` — стабильный id воркера в рамках процесса (не обязан быть OS PID); один keep-alive loop на воркер.

---

## 6. Как проверить вручную

1. **Keep-alive:** длинный `in_progress` без частых `set_worker_status`, но с keep-alive → нет Force Work X до истечения fallback по heartbeat.
2. **Mismatch порта:** оставить SS с `--testRunId A`, запустить адаптер с `B` на том же порту → сразу понятная ошибка, без «вечного» registered.
3. **Orphan:** убить клиент при `registered`/`in_progress` → через ~fallback воркер снимается; при отсутствии активности → idle shutdown.
4. **Параллель:** два воркера, один `testRunId`, один порт → оба register + keep-alive, прогон финализируется.

---

## 7. Env / CLI (кратко)

| Переменная / флаг | Назначение | Дефолт |
|-------------------|------------|--------|
| `--testRunId` | Primary test run (обязателен) | — |
| `--port` / `SYNC_STORAGE_PORT` | Порт | часто `49152` в адаптерах |
| `--idle-timeout` / `SYNC_STORAGE_IDLE_TIMEOUT` | Idle shutdown | `10m` |
| `TMS_SYNC_STORAGE_AUTOCOMPLETE_FALLBACK_S` | Stuck/orphan порог по heartbeat (сек.) | `180` |

Полная таблица: `adapters-core/docs/ENV.md`.

---

## 8. Ссылки на код (Java)

| Тема | Где смотреть |
|------|----------------|
| Floor/канал + resolve latest release | `SyncStorageRunner`, `SyncStorageReleaseResolver` |
| Start/reuse, health check | `SyncStorageRunner.java` |
| Keep-alive HTTP | `ClientWrapper.sendKeepAlive`, `SyncStorageKeepAlive` |
| Fail-fast mismatch | `SyncStorageService.initializeSyncStorage` (не глотать `IllegalStateException`) |
| Сервер: keep-alive | `handlers/keep_alive_handler.go` |
| Сервер: health + testRunId | `handlers/health_handler.go` |
| Сервер: 409 register | `handlers/register_handler.go`, `SyncStorage.RegisterWorker` |
| Сервер: orphan cleanup | `testrun_context.CleanupWorkers` |
