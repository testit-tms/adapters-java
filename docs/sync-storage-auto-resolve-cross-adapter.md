# Sync Storage: auto-resolve latest patch (cross-adapter plan)

Глобальный план для **всех** адаптеров (Java / Python / JS / .NET / Kotlin / …).  
Референс: **adapters-java** — `SyncStorageVersionConstraint`, `SyncStorageReleaseResolver`, `SyncStorageRunner`.

Цель: адаптер **не пинит точный бинарь навсегда**, а задаёт **floor + канал** и на старте берёт **последний патч** в своём диапазоне.

---

## 1. Контракт версии (одинаковый везде)

Константа / конфиг в адаптере — это **floor**, не «единственная разрешённая версия».

| Floor в коде | Канал | Что выбираем |
|--------------|--------|--------------|
| `v0.4.4` | **numeric** | только теги `vMAJOR.MINOR.PATCH` **без** `-tms-*` |
| `v0.4.3-tms-6.0` | **tms-6.0** | только теги `vMAJOR.MINOR.PATCH-tms-6.0` |
| `v0.4.2-tms-5.8` | **tms-5.8** | только `…-tms-5.8` |

Правила отбора кандидата:

1. Тот же **major.minor**, что у floor (линейка `0.4.*`, не `0.5.*`).
2. **patch ≥** patch у floor.
3. Канал:
   - floor **без** `-tms-…` → кандидат **строго numeric** (regex `^v?\d+\.\d+\.\d+$`);
   - floor **с** `-tms-X.Y` → кандидат **строго** с тем же суффиксом; `tms-5.8` ≠ `tms-6.0`, numeric не подходит.
4. Из подходящих — **max(patch)**.
5. Игнор: draft / prerelease GitHub, теги с `-rc` и прочим мусором вне схемы.

Нормализация тега для URL: всегда с префиксом `v` (`v0.4.3-tms-6.0`).

Источник списка:  
`GET https://api.github.com/repos/testit-tms/sync-storage-public/releases?per_page=100`  
Headers: `Accept: application/vnd.github+json`, `User-Agent: testit-<lang>-adapter`.

Артефакт:  
`https://github.com/testit-tms/sync-storage-public/releases/download/{tag}/syncstorage-{tag}-{os}_{arch}[.exe]`

---

## 2. Поведение рантайма

```
start SyncStorage (адаптер сам поднимает процесс)
  → resolveLatest(floor)          # HTTP к GitHub API
  → cachePath = …/syncstorage-{resolved}-{os}_{arch}
  → if file missing → download
  → spawn binary
```

| Ситуация | Resolve API | Download |
|----------|-------------|----------|
| Новый JVM/процесс адаптера, сам стартует SS | да | только если файла resolved-версии нет в cache |
| Тот же процесс, повторный start | нет (кэш в памяти) | нет |
| На порту уже чужой/свой SS (reuse) | не нужен | не нужен |
| API упал / нет кандидатов | — | fallback на **floor**, качать floor если нет в cache |

Имя файла в cache **включает resolved tag** → новая версия = новое имя = автодокачка без ручной чистки. Старые бинари можно оставлять.

---

## 3. Что вынести в каждом адаптере (чеклист)

Минимальный набор модулей (имена — по вкусу языка):

1. **Parse floor** — major / minor / patch / optional `tmsChannel`.
2. **selectLatest(floor, tags[])** — чистая функция, **unit-тесты без сети**.
3. **fetchReleaseTags()** — GitHub API, skip draft/prerelease.
4. **resolveLatest(floor)** — fetch + select + fallback floor; лог: `floor → resolved (channel=…)`.
5. **Wire в start/download** — URL и cache path только от **resolved**, не от сырого floor (кроме fallback).
6. **CI** — тот же алгоритм перед `wget`/`curl` (Java: `.github/scripts/resolve-sync-storage-version.sh`).

Не делать:

- «latest» со всего репо без фильтра канала;
- fallback на `releases[0]` / чужой `tms-*`;
- подтягивать другой **minor** (`0.4` → `0.5`) без смены floor в релизе адаптера.

---

## 4. Какой floor ставить в ветке адаптера

| Ветка / продукт | Floor (пример) |
|-----------------|----------------|
| TMS 6.0 line | `v0.4.3-tms-6.0` (или минимально принятый `v0.4.x-tms-6.0`) |
| TMS 5.8 line | `v0.4.2-tms-5.8` |
| Public / без TMS-суффикса в релизе SS | `v0.4.3` (numeric) |

Bump floor в адаптере — только когда поднимаете **минимально поддерживаемый** патч или меняете канал. Патч-релизы SS в той же линейке подхватываются сами.

---

## 5. План раскатки по репозиториям

Порядок (один контракт → копипаст логики + тесты):

| # | Репо | Статус / действие |
|---|------|-------------------|
| 1 | **adapters-java** | референс (сделано) |
| 2 | **adapters-python** | тот же parse/select/resolve; cache dir по конвенции проекта |
| 3 | **adapters-js** | idem |
| 4 | **adapters-dotnet** | idem |
| 5 | **adapters-kotlin** / прочие | idem |
| 6 | CI каждого репо | resolve script или inline; в логе `floor=` / `resolved=` |
| 7 | Docs в каждом репо | ссылка на этот контракт; не дублировать расходящиеся правила |

Критерий «готово» для языка:

- [ ] Unit-тесты: numeric vs `tms-6.0` vs `tms-5.8`, не пересекаются; minor line не пересекается; patch ≥ floor.
- [ ] Интеграционно: при наличии более нового тега в канале качается он, не floor.
- [ ] Offline/API fail → стартует floor (если артефакт доступен / в cache).
- [ ] Reuse порта не ломается.

---

## 6. Тест-матрица (общая)

| Floor | Tags (фрагмент) | Expected |
|-------|-----------------|----------|
| `v0.4.0` | `v0.4.3`, `v0.4.3-tms-6.0`, `v0.4.2-tms-5.8` | `v0.4.3` |
| `v0.4.0-tms-6.0` | те же | `v0.4.3-tms-6.0` |
| `v0.4.1-tms-5.8` | те же | `v0.4.2-tms-5.8` |
| `v0.4.4` | только до `v0.4.3` numeric | fallback floor / null→floor |
| `v0.3.0` | `v0.3.12`, `v0.4.3` | `v0.3.12` |

---

## 7. Риски

| Риск | Митигация |
|------|-----------|
| Rate limit GitHub API | User-Agent; fallback floor; при необходимости `GH_TOKEN` в CI |
| Сломанный «latest» в канале | floor = минимально проверенный патч; не ходить ниже floor |
| CI качает floor, адаптер — newer | CI обязан использовать **тот же** resolve |
| Два адаптера / два канала на одной машине | разные имена файлов в cache — ок |

---

## 8. Связанные доки

- `docs/sync-storage-adapter-compat.md` — keep-alive, health.testRunId, reuse
- `docs/sync-storage-0.4-cross-adapter-guide.md` — Work X / статусы
- Java code: `ru.testit.syncstorage.SyncStorageReleaseResolver`
