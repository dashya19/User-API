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
* Docker & Docker Compose (если нужно запускать контейнеры)
* PostgreSQL (если нужно запускать локально без Docker)
* Maven

---

## Переменные окружения / application.yml

(Файл `application.yml` в репозитории содержит пример конфигурации. Необходимо убедиться, что заданы корректные значения для подключения к PostgreSQL.)

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

Сервис будет доступен по `http://localhost:8080` (указано в `docker-compose.yml`).

### Локально (без Docker)

1. Запуск PostgreSQL и применение миграции (Liquibase автоматически выполнится при старте приложения).
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
  "fio": "Смирнов Антон Алексеевич",
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
    "fio": "Смирнов Антон Алексеевич",
    "phoneNumber": "+71234567890",
    "avatar": "https://example.com/avatar.jpg",
    "role": {
      "id": "2a4453ee-8f0c-4b99-822e-4ac987b51023",
      "roleName": "user"
    }
  }
}
```

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
    "fio": "Смирнов Антон Алексеевич",
    "phoneNumber": "+71234567890",
    "avatar": "https://example.com/avatar.jpg",
    "role": {
      "id": "2a4453ee-8f0c-4b99-822e-4ac987b51023",
      "roleName": "user"
    }
  }
}
```

---

### 3) Обновить пользователя

**PUT** `/userDetailsUpdate`

**Request Body (JSON):**

```json
{
  "id": "b9a23f4e-8bfc-4d6d-8e41-9d71503cf39e",
  "fio": "Смирнов Антон Алексеевич",
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
    "fio": "Смирнов Антон Алексеевич",
    "phoneNumber": "+79998887766",
    "avatar": "https://example.com/new-avatar.jpg",
    "role": {
      "id": "13b0de40-8b01-4c51-9efc-7b0d2e2a1f93",
      "roleName": "admin"
    }
  }
}
```

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

---
