# Sync-storage 0.4.x + адаптеры: итоги, проблемы, гайд для других языков

Документ для переноса правок в **Python / .NET / JS / Robot / …** (в т.ч. с AI-агентом).  
Референс: **adapters-java** + общий бинарник **sync-storage** (`adapters-core`).

Связанные доки:

- [sync-storage-adapter-compat.md](./sync-storage-adapter-compat.md) — контракт keep-alive / health / reuse
- [mode0-duplicate-results-bulk-after-realtime.md](./mode0-duplicate-results-bulk-after-realtime.md) — InProgress + Work X
- `.cursor/rules/sync-storage-finalize.mdc` — hard rule: статус только через send test result
- `adapters-core/docs/WORK_X_STATUS_FINALIZATION.md` — Work X на стороне Go

**Минимальный pin бинарника:** `v0.4.2` (линия 5.7) / `v0.4.2-tms-5.8` (линия 5.8 / TMS 6.0).

---

## 0. Hard rules (агенту читать первым)

1. **Статус тест-результата (Passed / Failed / …) никогда не финализируем через `PUT /api/v2/testResults/{id}`.**  
   Только **send test result**: `POST …/testRuns/{id}/testResults` (`SetAutoTestResultsForTestRun`).
2. С SyncStorage: cut в SS с **финальным** статусом; в TMS на finish теста — только **InProgress**; финал делает **Work X**.
3. Без SyncStorage: адаптер шлёт финальный статус как раньше (realtime / bulk).
4. `PUT /testResults/{id}` допустим только для fixtures / non-status полей; status в теле = **как на сервере**, не перетирать финалом адаптера.
5. Регрессии 0.4.x чиним **на пути send test result + lifecycle**, не заменой Work X на PUT.

---

## 1. Какие проблемы решали

| # | Симптом | Корневая причина | Где чинили |
|---|---------|------------------|------------|
| A | WARN `keep_alive` / 404 каждые ~25 с | В 0.4.0 временно пропал `POST /keep_alive`, клиент слал его | **SS** + тише логи в адаптере |
| B | Прогон «висит», Wrong testRunId, SS на занятом порту | Reuse процесса на порту **без** проверки `testRunId` | **SS** (`/health.testRunId`, register **409**) + **адаптер** (проверка перед reuse) |
| C | Результаты остаются **InProgress**, Work X «как будто был» | Master снимали через ~20 с после `completed` → SS гас до конца Work X | **SS** `v0.4.1+`: master жив до `workXCompleted` |
| D | Work X: search OK, send → **401 Unauthorized** | Bump `api-client-golang` 3.6→3.7: ключ auth `"PrivateToken"`, SS клал `"Bearer or PrivateToken"` | **SS** `v0.4.2+`: `AuthContext` с обоими ключами |
| E | Bulk / realtime: `AlreadyExistsException` на `updateAutoTest`, результаты не уезжают (TMS 6.0) | Ошибка metadata PUT рвала весь flush **до** `sendTestResults` | **Адаптер (Java)**: soft-fail AlreadyExists, всегда пытаться send |
| F | CI: `wait-completion` Connection refused / Temurin gzip | SS уже вышел после Work X; битый Temurin 8.0.514 | **CI**: health-before-wait; pin JDK; setup-java v6 |

---

## 2. Контракт финализации статуса (все языки)

```
Тест finish
  ├─ SyncStorage доступен?
  │    ├─ YES:
  │    │    1) POST cut в SS (/in_progress_test_result) со статусом Passed/Failed/…
  │    │    2) sendTestResults в TMS со статусом InProgress
  │    │    3) bulk в конце НЕ шлёт повторный final для уже отправленных uuid
  │    │    4) Work X (SS): search InProgress → GET → SetAutoTestResultsForTestRun(cut status)
  │    └─ NO:
  │         sendTestResults / bulk с финальным статусом (как до SyncStorage)
  └─ НИКОГДА: PUT /testResults/{id} ради outcome/statusCode/statusType
```

Референс Java: `AdapterTestCaseHelper.stopTestCase`, `HttpWriter.writeTestRealtime`, Go Work X: `OnAllNodesCompletedOrOffline` → `SetAutoTestResultForRun`.

---

## 3. Что сделано в sync-storage (один раз на все адаптеры)

### 3.1 Keep-alive

- `POST /keep_alive` body: `{ "pid", "testRunId" }`
- 200 / 400 / 404 (`Worker not found`) / 405
- Клиент: интервал ~20–30 с после register, стоп при completed

### 3.2 Health + привязка к testRunId

- `GET /health` → `{ status, testRunId, last_update }` (additive)
- `POST /register` с другим `testRunId` → **409**

### 3.3 Lifecycle / orphans

- Master **не** удалять по heartbeat, пока `workXCompleted != true`
- Orphan: non-completed + stale heartbeat > fallback (дефолт 180 с)
- После Work X — снять воркеров контекста

### 3.4 Work X auth (критично для 0.4.2)

OpenAPI client v3.7 ищет ключ контекста **`PrivateToken`**.  
В SS `AuthContext` выставлять **оба**:

```text
"PrivateToken"           → APIKey{Key: token, Prefix: "PrivateToken"}
"Bearer or PrivateToken" → то же (совместимость с client 3.6)
```

Иначе: search/GET (raw HTTP) работают, `SetAutoTestResultsForTestRun` → **401**, результаты остаются InProgress, Work X ретраится каждые ~5 с.

### 3.5 Версии релизов

| Tag | Содержание |
|-----|------------|
| `v0.4.0` | keep-alive, health.testRunId, 409, orphans (без fix master / auth) |
| `v0.4.1` | master жив до конца Work X |
| `v0.4.2` | AuthContext PrivateToken (+ оба ключа) |
| `*-tms-5.8` | те же фиксы, линия api-client / TMS 5.8 |

Адаптер должен **пиннуть** бинарь ≥ `v0.4.2` / `v0.4.2-tms-5.8`.

---

## 4. Чеклист для адаптера (другой язык)

Скопировать агенту как todo.

### 4.1 Pin sync-storage

- [ ] Константа версии ≥ `v0.4.2` (или `v0.4.2-tms-5.8` для 5.8/6.0)
- [ ] Download URL / имя файла совпадают с релизом `sync-storage-public`

### 4.2 Keep-alive client

- [ ] После успешного `register` — периодический `POST /keep_alive`
- [ ] 404: если body ≈ `Worker not found` → WARN; иначе (нет route) → DEBUG, без спама
- [ ] Стоп keep-alive при completed / shutdown

### 4.3 Port reuse / health

- [ ] Перед reuse занятого порта: `GET /health`
- [ ] Если `testRunId` в health **≠** текущему → **ошибка** (не молчаливый reuse); подсказка сменить порт / убить процесс
- [ ] Register: обработать **409**
- [ ] Shutdown hook гасит **свой** дочерний процесс SS (не чужой orphan на порту)

### 4.4 Finish теста + SyncStorage

- [ ] Cut в SS с **финальным** статусом
- [ ] В TMS — `sendTestResults` с **InProgress** (если cut принят)
- [ ] Bulk в конце **не** шлёт второй final для уже отправленных id
- [ ] **Запрещено** финализировать outcome через PUT testResults

### 4.5 Metadata update vs send results (TMS 6.0)

- [ ] `AlreadyExistsException` / аналог на `updateAutoTest` **не** должен блокировать `sendTestResults`
- [ ] Realtime: update упал с AlreadyExists → всё равно send result
- [ ] Bulk: parallel update/create упал → log + всё равно flush results
- [ ] Retry на concurrency / AlreadyExists (backoff)

### 4.6 CI (если есть e2e как в Java)

- [ ] Перед `wait-completion`: проверить `/health`; если SS уже мёртв — искать маркер `Work X completed successfully` в логе
- [ ] Не считать Connection refused фатальным, если Work X уже отработал

---

## 5. Промпт для агента (другой репозиторий адаптера)

```text
Задача: привести адаптер <LANG> к совместимости с sync-storage ≥ v0.4.2
(референс: testit-tms/adapters-java, docs/sync-storage-0.4-cross-adapter-guide.md).

Hard rules:
- Статус Passed/Failed финализировать ТОЛЬКО через send test result
  (SetAutoTestResultsForTestRun), НИКОГДА через PUT /api/v2/testResults/{id}.
- С SyncStorage: cut final → SS; TMS на finish = InProgress; Work X делает final.
- Pin бинарника sync-storage: v0.4.2 или v0.4.2-tms-5.8.

Сделай по чеклисту раздела 4 того гайда:
1) bump pin
2) keep-alive + тихие 404
3) health.testRunId перед reuse + 409
4) finish/InProgress контракт
5) AlreadyExists на updateAutoTest не блокирует sendTestResults
Не предлагай PUT для финализации статуса.
Минимум кода, без лишних рефакторингов.
```

---

## 6. Как проверять

### 6.1 Sync-storage / Work X

В `service.log` / stdout SS после прогона:

```text
All processing tasks completed! Performing work X...
foundResult: <uuid>
Work X completed successfully!
```

Красные флаги:

```text
Failed to send final test result via TestRuns API: ... 401 Unauthorized
```
→ бинарь < 0.4.2 или сломан AuthContext.

```text
WARNING: sync-storage stopped without Work X success marker
```
→ lifecycle / master снят рано / Work X не дошёл.

### 6.2 Адаптер

- Port mismatch → явная ошибка с двумя testRunId, не silent reuse.
- Keep-alive на старом бинаре без route → нет WARN-спама «worker not found».
- TMS 6.0: AlreadyExists на PUT autotest в логе допустим как WARN; результаты в прогоне всё равно Passed/Failed, не InProgress.

### 6.3 Validator / UI

- Outcomes не залипают в InProgress после wait-completion.
- Mode 0: нет лишних orphan-строк без testPoint из-за повторного create final.

---

## 7. Карта файлов (Java-референс)

| Область | Где смотреть |
|---------|----------------|
| Pin версии SS | `SyncStorageRunner.SYNC_STORAGE_VERSION` |
| Keep-alive / health reuse | `SyncStorageRunner`, `ClientWrapper`, `SyncStorageKeepAlive` |
| Finish + InProgress | `AdapterTestCaseHelper.stopTestCase` |
| Realtime / bulk / AlreadyExists | `HttpWriter`, `BulkAutotestHelper` |
| Work X + auth | `adapters-core/.../core_service.go`, `test_result_sender.go` |
| CI wait | `.github/workflows/test.yml` |

---

## 8. Краткий timeline (для контекста агенту)

1. **0.4.0** — restore keep-alive, health.testRunId, 409, orphan cleanup; побочно: ранний съём master + bump go api-client.
2. **Java** — reuse validation, quieter 404, CI health-before-wait, pin 0.4.x.
3. **InProgress в CI** — сначала думали на PUT; **отклонено**. Контракт: send test result.
4. **0.4.1** — master жив до Work X.
5. Логи CI: Work X **401** на SetAutoTest → **0.4.2** AuthContext `PrivateToken`.
6. **TMS 6.0 / cucumber8**: AlreadyExists на updateAutoTest рвал bulk до send → soft-fail + always send results.
