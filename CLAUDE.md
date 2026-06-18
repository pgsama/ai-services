# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

Spring Boot 4.1.0 + Spring AI 2.0.0 service with DeepSeek chat and Supabase/PostgreSQL persistence. Java 21. Hexagonal (ports & adapters) architecture. WAR packaging.

## Commands

Use the Maven wrapper (`mvnw.cmd` on Windows, `./mvnw` on Unix).

```bash
./mvnw spring-boot:run
./mvnw clean package
./mvnw test
./mvnw test -Dtest=AiServicesApplicationTests#contextLoads
```

## Architecture

Hexagonal layout under `pg.net.ai_services`:

```
domain/
  model/        Pure POJOs — zero framework dependencies
  port/in/      ChatInputPort, UsuarioInputPort
  port/out/     ChatOutputPort, UsuarioOutputPort
  service/      ChatDomainService, UsuarioDomainService

infrastructure/
  ai/           DeepSeekChatAdapter        implements ChatOutputPort via Spring AI ChatClient
  persistence/  UsuarioPersistenceAdapter  implements UsuarioOutputPort via Spring Data JPA
                UsuarioJpaEntity, UsuarioJpaRepository (package-private), UsuarioMapper (package-private)
  web/          ChatRestAdapter     POST /api/chat
                UsuarioRestAdapter  POST /api/usuario, GET /api/usuario, GET /api/usuario/{id}
                dto/  ChatRequestDto, ChatResponseDto, UsuarioRequestDto, UsuarioResponseDto
  config/       ChatConfig (ChatClient bean), DomainConfig (wires domain services as Spring beans)

config/         DotenvLoader  — pre-Spring; called from main() and ServletInitializer before SpringApplication.run
utils/          CryptoUtils   — AES-256-GCM encode/decode; key from HASH_PASSWORD system property
```

Domain services are plain Java — no Spring annotations. `DomainConfig` instantiates them and injects the output port adapters. `UsuarioDomainService.create` encodes the password via `CryptoUtils.encode` before persisting.

## Dependencies

| Artifact | Purpose |
|---|---|
| `spring-boot-starter-webmvc` | REST layer |
| `spring-ai-starter-model-deepseek` | DeepSeek chat via Spring AI |
| `spring-boot-starter-data-jpa` | Hibernate + Spring Data |
| `postgresql` | PostgreSQL JDBC driver |
| `spring-boot-devtools` | Hot reload (runtime, optional) |
| `spring-boot-starter-tomcat` | Provided — WAR deploy to external container |

## Credentials & Environment

All secrets in `.env` files outside the repo. `DotenvLoader` resolves by OS:
- Windows: `C:/dotenv/ai-services/.env`
- Linux: `/opt/dotenv/ai-services/.env`

Required keys: `DEEPSEEK_API_KEY`, `SUPABASE_URL`, `SUPABASE_DATABASE`, `SUPABASE_USERNAME`, `SUPABASE_PASSWORD`, `HASH_PASSWORD`.

Database: Supabase PostgreSQL, database `ai`, schema `services`. Schema must exist before first run:
```sql
CREATE SCHEMA IF NOT EXISTS services;
```

## Code Style

No comments anywhere — not in Java files, not in `application.properties`. Names must be self-explanatory.

## Notes

- Base package is `pg.net.ai_services` (underscore — hyphen is invalid Java package name).
- WAR: `tomcat` is `provided`. `ServletInitializer` is the WAR entrypoint; `AiServicesApplication.main` for standalone. Both call `DotenvLoader.load()` first.
- `pom.xml` empty `<license>`/`<developers>`/`<scm>` overrides block Spring Boot parent POM inheritance — leave them.
- `UsuarioResponseDto` never exposes the password field.
