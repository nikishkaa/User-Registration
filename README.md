# User Registration Spring Security

Приложение для регистрации пользователей с Spring Security и MySQL.
https://github.com/anshu20126/spring-boot-user-registration-and-Login/blob/main/src/main/resources/templates/index.html
## Запуск приложения

### Вариант 1: Полный Docker (рекомендуется)

```bash
# Запуск всех сервисов (приложение + MySQL + phpMyAdmin)
docker-compose up -d

# Просмотр логов приложения
docker-compose logs -f app

# Проверка статуса
docker-compose ps
```

### Вариант 2: Только база данных в Docker

```bash
# Запуск только MySQL и phpMyAdmin
docker-compose up mysql phpmyadmin -d

# Запуск приложения локально
mvn spring-boot:run
```

### Вариант 3: Локальная разработка

```bash
# Убедитесь, что MySQL запущен локально на порту 3306
# Запуск приложения
mvn spring-boot:run
```

## Доступ к сервисам

- **Spring Boot приложение**: http://localhost:8081
- **phpMyAdmin**: http://localhost:8080
    - Автоматический вход как root (пароль: rootpassword)

## Остановка

```bash
# Остановка всех контейнеров
docker-compose down

# Остановка с удалением данных (включая базу данных)
docker-compose down -v

# Пересборка приложения
docker-compose build app
```

## Структура базы данных

JPA автоматически создаст следующие таблицы:

- **users** - таблица пользователей
- **roles** - таблица ролей
- **users_roles** - связующая таблица

## Роли по умолчанию

При первом запуске автоматически создаются роли:

- ROLE_USER - обычный пользователь
- ROLE_ADMIN - администратор
