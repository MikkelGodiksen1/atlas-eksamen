# Atlas App — Spring Boot

Webapplikationen for Nordic Atlas Packaging. Se hovedmappen [../README.md](../README.md) for forretningskontekst, analyse-dokumenter og Jira-eksport.

## Hurtig start

```bash
# 1. Lokal udvikling med H2 in-memory + StubImageGenerator
mvn spring-boot:run

# 2. Eller med Docker Compose (PostgreSQL + app)
docker-compose up --build

# 3. Tests
mvn test
```

Åbn `http://localhost:8080`. Login som admin: `admin@nap.dk` / `admin123`.

## Profiler

| Profile | DB | ImageGenerator |
|---------|-----|----------------|
| `local` (default) | H2 in-memory | StubImageGenerator (composite via Java AWT) |
| `prod` (Railway) | PostgreSQL | OpenAIImageGenerator (kræver `OPENAI_API_KEY`) |
| `test` | H2 in-memory | StubImageGenerator |

## Pakke-struktur

```
dk.nap.atlas
├── controller/    Controller-lag (Spring MVC)
├── service/       Forretningslogik
├── service/ai/    Strategy-pattern for billede-generering
├── repository/    JDBC-baserede repositories
├── model/         POJO-modeller
├── dto/           Form-binding-objekter
├── config/        Spring Security + multipart
└── exception/     GlobalExceptionHandler
```

## Deployment til Railway

1. Push repo til GitHub.
2. Opret Railway-projekt → "Deploy from GitHub repo".
3. Tilføj PostgreSQL-plugin → `DATABASE_URL` etc. injectes automatisk.
4. Sæt env-vars: `SPRING_PROFILES_ACTIVE=prod`, `OPENAI_API_KEY=sk-...`.
5. Railway læser `railway.toml` og bygger via Dockerfile.

Healthcheck: `/actuator/health`.
