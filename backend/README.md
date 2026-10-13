# Mr. White backend

This is the Java 17 / Spring Boot 3 backend for the Mr. White multiplayer party
game. Game state is held by the singleton `GameService`; JPA stores only seeded
word pairs. The default profile uses an in-memory H2 database. Set the `prod`
profile and `DATABASE_URL`, `DATABASE_USERNAME`, and `DATABASE_PASSWORD` for
PostgreSQL.

Run it with:

```text
mvn spring-boot:run
```

The REST API is `POST /api/join` with `{ "name": "..." }`. Native STOMP
WebSocket clients connect to `/ws` with the `sessionId` CONNECT header, then
subscribe to `/topic/game`, `/user/queue/private`, and `/user/queue/errors`.
Commands are sent to `/app/sync`, `/app/start`, `/app/vote`, and the other
destinations described by the frontend contract.
