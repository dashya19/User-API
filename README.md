# User API

**Описание:** это простое REST API на Spring Boot, которое предоставляет CRUD для сущности `User` и связанных `Role`. Проект реализован в соответствии с ТЗ: валидация входящих данных, работа с PostgreSQL, миграции Liquibase, Docker, кеширование и централизованная обработка ошибок.

---

## Стек технологий

* Java 17
* Spring Boot 3
* Spring Data JPA
* Hibernate Validator (Jakarta Validation)
* MapStruct (маппинги DTO ↔ Entity)
* PostgreSQL
* Liquibase (миграции) — `src/main/resources/db.changelog/db.changelog-master.yaml`
* Docker + Docker Compose
* Кеширование: `ConcurrentMapCacheManager` (настройка в `CacheConfig`)
* Lombok

---

## Архитектура и структура проекта

Проект следует привычной структуре Spring MVC/Service/Repository:

* `controller` — REST-контроллеры (`UserController`)
* `service`/`service.impl` — бизнес-логика (`UserService`, `RoleService`)
* `repository` — Spring Data JPA репозитории (`UserRepository`, `RoleRepository`)
* `model` — JPA сущности (`User`, `Role`)
* `dto` — request/response DTO + валидация
* `mapper` — MapStruct мапперы
* `exception` — кастомные исключения и `GlobalExceptionHandler` (`@RestControllerAdvice`)
* `config` — конфигурации (CORS, Cache)

Ключевые детали:

* При создании пользователя автоматически создаётся роль (если её нет) через `RoleService.findOrCreateRole`.
* При удалении пользователя роль удаляется, если она больше ни у кого не используется.
* Валидация DTO происходит через аннотации Jakarta Validation.
* Центральная обработка ошибок через `GlobalExceptionHandler` возвращает `ErrorResponseDTO`.
* Кеширование для пользователей и ролей (`users`, `roles`) настроено через аннотации `@Cacheable` и `@CacheEvict`.

---

## Требования для запуска

* Java 17 JDK
* Docker & Docker Compose (если запускаете контейнеры)
* PostgreSQL (если запускаете локально без Docker)
* Maven

---

## Переменные окружения / application.yml

(Файл `application.yml` в репозитории содержит пример конфигурации. Убедитесь, что заданы корректные значения для подключения к PostgreSQL.)

Пример необходимых переменных (при запуске в контейнерах через docker-compose они задаются в `docker-compose.yml`):

* `SPRING_DATASOURCE_URL` — jdbc url (например `jdbc:postgresql://db:5432/userdb`)
* `SPRING_DATASOURCE_USERNAME` — пользователь БД
* `SPRING_DATASOURCE_PASSWORD` — пароль
* `SPRING_JPA_HIBERNATE_DDL_AUTO` — обычно `none` (миграции в Liquibase)

---

## Сборка и запуск

### Через Docker Compose (рекомендуется)

```bash
# В корне проекта
docker-compose up --build
```

Сервис будет доступен по `http://localhost:8080` (проверьте `docker-compose.yml`).

### Локально (без Docker)

1. Запустите PostgreSQL и примените миграции (Liquibase автоматически выполнится при старте приложения, если включено в конфиг).
2. Сборка и запуск приложения:

```bash
mvn clean package
java -jar target/userapi-0.0.1-SNAPSHOT.jar
```

---

## Миграции

В проекте используется Liquibase. Файл миграций: `src/main/resources/db.changelog/db.changelog-master.yaml`.
Liquibase выполняет создание таблиц `roles` и `users` с нужными ограничениями (unique/foreign key).

---

## API

Базовый префикс: `/api`.
Все ответы — JSON в стандартизированной форме (`SuccessResponseDTO` / `ErrorResponseDTO`).

### 1) Создать пользователя

**POST** `/api/createNewUser`

**Body** (JSON):

```json
{
  "fio": "Иван Иванов",
  "phoneNumber": "+71234567890",
  "avatar": "https://example.com/avatar.jpg",
  "roleName": "user"
}
```

**Успешный ответ (201 Created)**

```json
{
  "message": "Пользователь успешно создан",
  "data": {
    "id": "uuid",
    "fio": "Иван Иванов",
    "phoneNumber": "+71234567890",
    "avatar": "https://example.com/avatar.jpg",
    "role": {
      "id": "uuid",
      "roleName": "user"
    }
  }
}
```

**Ошибки валидации** — `400 Bad Request` с `ErrorResponseDTO`.
**Конфликт (дубликат номера/роли)** — `409 Conflict`.

---

### 2) Получить пользователя по UUID

**GET** `/api/users?userID={uuid}`

**Успешный ответ (200 OK)** — `SuccessResponseDTO` с `UserResponseDTO`.

**Ошибка** — `404 Not Found` если пользователь не найден.

---

### 3) Обновить пользователя

**PUT** `/api/userDetailsUpdate`

**Body** (JSON):

```json
{
  "id": "uuid",
  "fio": "Новое ФИО",
  "phoneNumber": "+79998887766",
  "avatar": "https://example.com/new.jpg",
  "roleName": "admin"
}
```

Поля, которые равны `null` или отсутствуют, не будут изменены (в `UpdateUserRequestDTO` некоторые поля опциональны).

**Успешный ответ (200 OK)** — `SuccessResponseDTO` с обновлённым `UserResponseDTO`.

---

### 4) Удалить пользователя

**DELETE** `/api/users?userID={uuid}`

**Успешный ответ (200 OK)**:

```json
{
  "message": "Пользователь успешно удален",
  "data": null
}
```

При удалении: если роль после удаления больше ни у кого не используется, она также удаляется. В противном случае удаление роли не выполняется — реализована проверка `isRoleInUse`.

---

## Валидация

* `fio`: обязательное, от 2 до 255 символов
* `phoneNumber`: обязательный, международный формат (регулярное выражение `^\+?[1-9]\d{1,14}$`)
* `avatar`: URL (проверяется аннотацией `@URL`)
* `roleName`: обязательное при создании, от 2 до 50 символов

Ошибки возвращаются через `GlobalExceptionHandler` как `ErrorResponseDTO` с детальной информацией.

---

## Кеширование

* Кеширование реализовано для `users` и `roles`.
* Конфигурация — `CacheConfig` с `ConcurrentMapCacheManager`.
* Вставка / обновление / удаление помечены `@CacheEvict` для поддержания консистентности.

---

## Postman — примеры запросов и коллекция

Ниже — коллекция Postman (JSON). Импортируйте её в Postman: `File → Import → Raw text` и вставьте JSON.

> **Postman Collection (упрощённый)**

```json
{
  "info": {
    "name": "User API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Create User",
      "request": {
        "method": "POST",
        "header": [
          {"key":"Content-Type","value":"application/json"}
        ],
        "body": {
          "mode": "raw",
          "raw": "{\"fio\": \"Иван Иванов\", \"phoneNumber\": \"+71234567890\", \"avatar\": \"https://example.com/avatar.jpg\", \"roleName\": \"user\"}"
        },
        "url": {"raw": "http://localhost:8080/api/createNewUser", "protocol": "http", "host": ["localhost"], "port":"8080", "path":["api","createNewUser"]}
      }
    },
    {
      "name": "Get User",
      "request": {
        "method": "GET",
        "url": {"raw": "http://localhost:8080/api/users?userID={{userID}}", "host":["localhost"],"port":"8080","path":["api","users"], "query":[{"key":"userID","value":"{{userID}}"}]}
      }
    },
    {
      "name": "Update User",
      "request": {
        "method": "PUT",
        "header": [{"key":"Content-Type","value":"application/json"}],
        "body": {"mode":"raw","raw":"{\"id\": \"{{userID}}\", \"fio\": \"Новое ФИО\" }"},
        "url": {"raw":"http://localhost:8080/api/userDetailsUpdate","host":["localhost"],"port":"8080","path":["api","userDetailsUpdate"]}
      }
    },
    {
      "name": "Delete User",
      "request": {
        "method": "DELETE",
        "url": {"raw":"http://localhost:8080/api/users?userID={{userID}}","host":["localhost"],"port":"8080","path":["api","users"],"query":[{"key":"userID","value":"{{userID}}"}]}
      }
    }
  ]
}
```

**Переменные окружения для Postman:**

* `userID` — UUID созданного пользователя (используйте в запросах GET/PUT/DELETE)

---

## Обработка ошибок

Используется `@RestControllerAdvice` (`GlobalExceptionHandler`) для возвращения структурированных ошибок (`ErrorResponseDTO`). Включены обработчики для:

* `EntityNotFoundException` → 404
* `MethodArgumentNotValidException` / `ConstraintViolationException` → 400
* `DataIntegrityViolationException` → 409 (с анализом причины)
* Кастомные исключения (`DuplicatePhoneNumberException`, `DuplicateRoleException`, `RoleInUseException`) → 409
* Общие ошибки → 500

---

## Замечания и рекомендации

* В продакшене стоит заменить `ConcurrentMapCacheManager` на распределённый кеш (Redis) и настроить TTL и эвикацию.
* Рекомендую добавить логирование (SLF4J/Logback) с уровнями и correlation-id для трассировки запросов.
* В `RoleServiceImpl` кеширование `@Cacheable(value = "roles", key = "#roleName")` и `@CacheEvict(value = "roles", key = "#roleId")` — сейчас ключи разные (name vs id). Это рабочо, но при масштабировании лучше унифицировать ключи или использовать отдельные кеши/методы для поиска по id/name.
* Рассмотреть добавление тестов (unit/integration) для сервисов и контроллеров, а также для Liquibase миграций.

---

## Контакты / как править README

Если нужно — могу адаптировать README под GitHub (badges, CI, workflow), добавить complete Postman collection с примерами ответов, или сгенерировать OpenAPI/Swagger документацию.

Спасибо — удачи с прохождением тестового задания!
