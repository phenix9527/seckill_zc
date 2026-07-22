# seckill_zc

Spring Boot + MyBatis Plus + MySQL Maven project running on Java 21.

## Requirements

- JDK 21
- Maven 3.9+
- MySQL 8+

## Run

Default MySQL 8 connection:

| Item | Value |
| --- | --- |
| Host | `127.0.0.1` or `localhost` |
| Port | `3306` |
| User | `root` |
| Password | `123456` |
| Database | `mysql` |
| Driver | `com.mysql.cj.jdbc.Driver` |

After connection succeeds, create the business database:

```sql
CREATE DATABASE seckill_zc DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Then start the application:

```bash
mvn spring-boot:run
```

Default datasource URL:

```text
jdbc:mysql://localhost:3306/mysql?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
```

You can override connection values with `MYSQL_URL`, `MYSQL_USERNAME`, and `MYSQL_PASSWORD`.
