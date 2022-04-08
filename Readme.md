# Scheduling jobs in Spring with Quartz Clustered mode - PoC

This repository demonstrates how to schedule jobs in Spring Boot application with Quartz Clustered mode.


## Features

* Retry Pattern With Exponential Back-Off
* Quartz Clustered Mode
* PostgreSQL DB
* Flyway
* Docker / Docker Compose

## Getting Started

### Prerequisites:

* Java 11
* Docker

### Installation

Build docker image

```shell
./gradlew bootBuildImage
```

### Usage:

1. Start services.
    ```shell
    cd docker
    docker compose up -d
    ```
2. Check if all services are running.
    ```shell
    docker compose ps
    ```
3. Schedule new todo job.
   ```shell
   curl --location --request POST 'http://localhost:8080/api/v1/todos' \
   --header 'Content-Type: application/json' \
   --data-raw '{
       "id": 2
   }'
   ```
4. Simulate failure.
   ```shell
   curl --location --request POST 'http://localhost:8080/api/v1/todos' \
   --header 'Content-Type: application/json' \
   --data-raw '{
       "id": 1
   }'
   ```
5. Check logs and verify if retry with exponential backoff works.
   ```shell
   docker compose logs -f
   # ...
   # 2022-04-08 08:40:35.884 ERROR 84542 --- [z-demo_Worker-1] com.rbiedrawa.app.todos.TodoJob          : Todo [1] is odd - simulating failure😈
   # 2022-04-08 08:40:35.888 ERROR 84542 --- [z-demo_Worker-1] c.r.app.core.scheduler.RetryableJobBean  : Job todoGroup.todoJob-1 failed with exception. Rescheduling...
   # java.lang.RuntimeException: Todo 1 is odd
   # 2022-04-08 08:40:35.920  INFO 84542 --- [z-demo_Worker-1] c.r.app.core.scheduler.RetryableJobBean  : Successfully rescheduled job todoGroup.todoJob-1 using new trigger todoGroup.todoJob-1 
   # 
   # 2022-04-08 08:40:39.931 ERROR 84542 --- [z-demo_Worker-2] com.rbiedrawa.app.todos.TodoJob          : Todo [1] is odd - simulating failure😈
   # 2022-04-08 08:40:39.931 ERROR 84542 --- [z-demo_Worker-2] c.r.app.core.scheduler.RetryableJobBean  : Job todoGroup.todoJob-1 failed with exception. Rescheduling...
   # java.lang.RuntimeException: Todo 1 is odd
   # 2022-04-08 08:40:39.954  INFO 84542 --- [z-demo_Worker-2] c.r.app.core.scheduler.RetryableJobBean  : Successfully rescheduled job todoGroup.todoJob-1 using new trigger todoGroup.todoJob-1 
   # ...
   ```
6. Open [Adminer](http://localhost:8081/) and investigate database structure. Connection details:
    * System: PostgresSQL
    * Server: db
    * Username: demo
    * Password: demo
    * Database: demo
7. Stop services.
   ```shell
   docker compose down -v
   ```

## References

For further reference, please consider the following sections:

* [Quartz - Job Scheduling](http://www.quartz-scheduler.org/)
* [Flyway](https://flywaydb.org/)

## License

Distributed under the MIT License. See `LICENSE` for more information.
