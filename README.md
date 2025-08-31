# User API

![Java](https://img.shields.io/badge/Java-17-red.svg)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.0-green.svg)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-3.2.0-darkgreen.svg)
![Hibernate Validator](https://img.shields.io/badge/Hibernate_Validator-8.0.1-blueviolet.svg)
![MapStruct](https://img.shields.io/badge/MapStruct-1.5.5-purple.svg)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)
![Liquibase](https://img.shields.io/badge/Liquibase-4.25.0-yellow.svg)
![Docker](https://img.shields.io/badge/Docker-20.10-lightblue.svg)
![Docker Compose](https://img.shields.io/badge/Docker_Compose-1.29.2-0db7ed.svg)
![Caching](https://img.shields.io/badge/Caching-ConcurrentMapCacheManager-orange.svg)
![Lombok](https://img.shields.io/badge/Lombok-1.18.30-pink.svg)

**Описание:** это простое REST API на Spring Boot, которое предоставляет CRUD для сущности `User` и связанных `Role`. Проект реализован в соответствии с ТЗ: валидация входящих данных, работа с PostgreSQL, миграции Liquibase, Docker, кеширование и централизованная обработка ошибок.

---

## Стек технологий

* Java 17
* Spring Boot 3
* Spring Data JPA
* Hibernate Validator (Jakarta Validation)
* MapStruct (маппинги DTO ↔ Entity)
* PostgreSQL
* Liquibase (миграции)
* Docker + Docker Compose
* Кеширование
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

## API (Postman)

Базовый URL: `http://localhost:8080/api`

### 1) Создать пользователя

**POST** `/createNewUser`

**Request Body (JSON):**

```json
{
  "fio": "Иван Иванов",
  "phoneNumber": "+71234567890",
  "avatar": "https://example.com/avatar.jpg",
  "roleName": "user"
}
```

**Response 201 (Created):**

```json
{
  "message": "Пользователь успешно создан",
  "data": {
    "id": "b9a23f4e-8bfc-4d6d-8e41-9d71503cf39e",
    "fio": "Иван Иванов",
    "phoneNumber": "+71234567890",
    "avatar": "https://example.com/avatar.jpg",
    "role": {
      "id": "2a4453ee-8f0c-4b99-822e-4ac987b51023",
      "roleName": "user"
    }
  }
}
```

**Ошибки:**

* `400 Bad Request` — при некорректных данных (валидация).
* `409 Conflict` — при дублировании номера телефона или роли.

---

### 2) Получить пользователя по UUID

**GET** `/users?userID={uuid}`

**Пример запроса:**

```
GET http://localhost:8080/api/users?userID=b9a23f4e-8bfc-4d6d-8e41-9d71503cf39e
```

**Response 200 (OK):**

```json
{
  "message": "Пользователь найден",
  "data": {
    "id": "b9a23f4e-8bfc-4d6d-8e41-9d71503cf39e",
    "fio": "Иван Иванов",
    "phoneNumber": "+71234567890",
    "avatar": "https://example.com/avatar.jpg",
    "role": {
      "id": "2a4453ee-8f0c-4b99-822e-4ac987b51023",
      "roleName": "user"
    }
  }
}
```

**Ошибки:**

* `404 Not Found` — пользователь не найден.

---

### 3) Обновить пользователя

**PUT** `/userDetailsUpdate`

**Request Body (JSON):**

```json
{
  "id": "b9a23f4e-8bfc-4d6d-8e41-9d71503cf39e",
  "fio": "Иван Петров",
  "phoneNumber": "+79998887766",
  "avatar": "https://example.com/new-avatar.jpg",
  "roleName": "admin"
}
```

**Response 200 (OK):**

```json
{
  "message": "Пользователь успешно обновлен",
  "data": {
    "id": "b9a23f4e-8bfc-4d6d-8e41-9d71503cf39e",
    "fio": "Иван Петров",
    "phoneNumber": "+79998887766",
    "avatar": "https://example.com/new-avatar.jpg",
    "role": {
      "id": "13b0de40-8b01-4c51-9efc-7b0d2e2a1f93",
      "roleName": "admin"
    }
  }
}
```

**Ошибки:**

* `404 Not Found` — пользователь не найден.
* `409 Conflict` — дублирование номера телефона или роли.

---

### 4) Удалить пользователя

**DELETE** `/users?userID={uuid}`

**Пример запроса:**

```
DELETE http://localhost:8080/api/users?userID=b9a23f4e-8bfc-4d6d-8e41-9d71503cf39e
```

**Response 200 (OK):**

```json
{
  "message": "Пользователь успешно удален",
  "data": null
}
```

**Ошибки:**

* `404 Not Found` — пользователь не найден.
* `409 Conflict` — роль не может быть удалена, если она используется другими пользователями.

---

## Postman Collection (готовая для импорта)

В файле ниже описаны все эндпоинты. Импортируйте JSON в Postman: **File → Import → Raw Text → вставьте JSON**.

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

**Переменные окружения Postman:**

* `userID` — UUID созданного пользователя.

---

## Обработка ошибок

Используется `GlobalExceptionHandler`. Ошибки возвращаются в формате:

```json
{
  "timestamp": "2025-08-31T12:34:56",
  "status": 400,
  "error": "Ошибка валидации данных",
  "message": "Некорректные данные в запросе: {fio=ФИО является обязательным}",
  "path": "/api/createNewUser"
}
```

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

## Контакты / как править README

Если нужно — могу адаптировать README под GitHub (badges, CI, workflow), добавить complete Postman collection с примерами ответов, или сгенерировать OpenAPI/Swagger документацию.

Спасибо — удачи с прохождением тестового задания!
