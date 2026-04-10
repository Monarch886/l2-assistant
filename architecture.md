# Microservice Architecture Template

Этот файл описывает шаблон многомодульного Spring Boot микросервиса.
При создании нового сервиса замени переменные в разделе **Variables** и воспроизведи структуру.

---

## Variables

| Variable              | Example value         | Description                                      |
|-----------------------|-----------------------|--------------------------------------------------|
| `{{PROJECT_NAME}}`    | `L2Agent`             | Имя корневого проекта (rootProject.name)         |
| `{{GROUP}}`           | `ru.che.lcp`          | Java group / базовый пакет                       |
| `{{BASE_PACKAGE}}`    | `ru.che.lcp`          | Корневой пакет для `@SpringBootApplication`      |
| `{{APP_CLASS}}`       | `L2AgentApplication`  | Имя главного класса                              |
| `{{MODULES}}`         | см. раздел Modules    | Список модулей и их зависимости друг от друга    |

---

## Module List (default set)

Каждая строка: `module-name → [зависит от ...]`

```
domain        → (нет)
usecase       → domain, ai-client, client       ← mapper-ы в usecase маппят в client.view/dto
controller    → usecase, client, domain
client        → domain                          ← публикуется в Nexus отдельным артефактом
ai-client     → domain
infrastructure → domain, usecase, controller, client, ai-client   ← bootJar entry point
```

> Правило: `infrastructure` — единственный модуль с плагином `org.springframework.boot` (собирает fat jar).
> Все остальные модули — обычные java-библиотеки.

---

## Module Responsibilities

| Модуль           | Назначение                                                                                          |
|------------------|-----------------------------------------------------------------------------------------------------|
| `domain`         | Чистая доменная модель: сущности, value objects, интерфейсы портов. Нет внешних зависимостей.      |
| `usecase`        | Application layer: оркестрирует бизнес-логику через порты домена.                                  |
| `controller`     | Входящие адаптеры: Spring MVC реализации интерфейсов из `client`.                                  |
| `client`         | **Публичный API-артефакт**: интерфейсы контроллеров + DTO + `@FeignClient`. Публикуется в Nexus. Потребители подключают как зависимость и получают готовый типизированный клиент. |
| `ai-client`      | Исходящий адаптер для AI/LLM. Реализует порты из `domain`.                                         |
| `infrastructure` | Composition root: Spring Boot entry point, wire-up конфигурации, БД, messaging и т.д.              |

---

## Directory Layout

```
{{PROJECT_NAME}}/
├── build.gradle              # root: плагины + subprojects { ... }
├── settings.gradle           # include + projectDir mapping
├── gradle.properties         # все версии зависимостей
├── gradlew / gradlew.bat
├── gradle/wrapper/
└── modules/
    ├── domain/
    │   ├── build.gradle
    │   └── src/main/java/{{GROUP}}/domain/
    ├── usecase/
    │   ├── build.gradle
    │   └── src/main/java/{{GROUP}}/usecase/
    ├── controller/
    │   ├── build.gradle
    │   └── src/main/java/{{GROUP}}/controller/
    ├── client/                                  ← публикуется в Nexus
    │   ├── build.gradle
    │   └── src/main/java/{{GROUP}}/client/
    │       ├── dto/                             ← request/response DTO
    │       └── XxxController.java               ← @FeignClient + @RequestMapping интерфейс
    ├── ai-client/
    │   ├── build.gradle
    │   └── src/main/java/{{GROUP}}/aiclient/
    └── infrastructure/
        ├── build.gradle
        └── src/main/java/{{GROUP}}/{{APP_CLASS}}.java
```

---

## File Templates

### `settings.gradle`

```groovy
rootProject.name = '{{PROJECT_NAME}}'

// Перечисли все модули
include 'domain', 'usecase', 'controller', 'client', 'ai-client', 'infrastructure'

// Маппинг: логическое имя → физическая директория
project(':infrastructure').projectDir = file 'modules/infrastructure'
project(':domain').projectDir        = file 'modules/domain'
project(':usecase').projectDir       = file 'modules/usecase'
project(':controller').projectDir    = file 'modules/controller'
project(':client').projectDir        = file 'modules/client'
project(':ai-client').projectDir     = file 'modules/ai-client'
```

### `gradle.properties`  ← **все версии здесь, нигде больше**

```properties
springBootVersion=3.4.4
springDependencyManagementVersion=1.1.7
mapstructVersion=1.6.3
lombokMapstructBindingVersion=0.2.0
springdocVersion=2.8.4
springCloudVersion=2024.0.1
```

### `build.gradle` (root)

```groovy
plugins {
    id 'org.springframework.boot'            version "${springBootVersion}"            apply false
    id 'io.spring.dependency-management'     version "${springDependencyManagementVersion}" apply false
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    group   = '{{GROUP}}'
    version = '1.0-SNAPSHOT'

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    repositories { mavenCentral() }

    // Импортируем BOM — версии зависимостей не указываем вручную
    // ВАЖНО: все BOM — здесь, в subprojects. Если BOM объявлен только в дочернем модуле,
    // родительский модуль (например, infrastructure) не сможет разрешить его транзитивные зависимости.
    dependencyManagement {
        imports {
            mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
            mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
        }
    }

    // Зависимости, общие для ВСЕХ модулей
    dependencies {
        compileOnly          'org.projectlombok:lombok'
        annotationProcessor  'org.projectlombok:lombok'

        implementation       "org.mapstruct:mapstruct:${mapstructVersion}"
        annotationProcessor  "org.mapstruct:mapstruct-processor:${mapstructVersion}"
        // Lombok должен запускаться раньше MapStruct
        annotationProcessor  "org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}"

        testImplementation   'org.springframework.boot:spring-boot-starter-test'
        testCompileOnly      'org.projectlombok:lombok'
        testAnnotationProcessor 'org.projectlombok:lombok'
    }

    test { useJUnitPlatform() }
}
```

### `modules/domain/build.gradle`

```groovy
// Domain — чистая доменная модель, нет зависимостей на другие модули
dependencies {
}
```

### `modules/usecase/build.gradle`

```groovy
// java-library: domain-типы из usecase видны потребителям на compile classpath
apply plugin: 'java-library'

dependencies {
    implementation project(':domain')
    implementation project(':ai-client')
    implementation project(':client')   // маперы в usecase.mapper маппят в client.view / client.dto

    implementation 'org.springframework.boot:spring-boot-starter'
}
```

### `modules/client/build.gradle`  ← публикуемый API-артефакт

```groovy
// java-library: api-зависимости транзитивно попадают на compile classpath потребителей
apply plugin: 'java-library'

dependencies {
    implementation project(':domain')

    // api — видны потребителям (controller и др.) без повторного объявления
    api 'org.springframework.boot:spring-boot-starter-web'
    api 'org.springframework.boot:spring-boot-starter-validation'
    api "org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}"
    api 'org.springframework.cloud:spring-cloud-starter-openfeign'
}
```

### `modules/controller/build.gradle`

```groovy
dependencies {
    implementation project(':usecase')
    implementation project(':client')
    implementation project(':domain')

    implementation 'org.springframework.boot:spring-boot-starter-web'
}
```

### `modules/ai-client/build.gradle`

```groovy
dependencies {
    implementation project(':domain')
}
```

### `modules/infrastructure/build.gradle`  ← единственный fat-jar

```groovy
plugins {
    id 'org.springframework.boot'   // только здесь!
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter'

    implementation project(':domain')
    implementation project(':usecase')
    implementation project(':controller')
    implementation project(':client')
    implementation project(':ai-client')
}
```

### `modules/client/src/main/java/{{GROUP}}/client/XxxController.java`

Один интерфейс — два использования: сервер его реализует, потребитель через `@FeignClient` его вызывает.

```java
package {{GROUP}}.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import {{GROUP}}.client.dto.XxxRequest;

@FeignClient(name = "{{PROJECT_NAME_LOWER}}", url = "${{{project_name_lower}}.service.url}")
@Tag(name = "Xxx", description = "...")
@RequestMapping("/api/v1/xxx")
public interface XxxController {

    @Operation(summary = "...", description = "...")
    // + @RequestBody OpenAPI, @ApiResponses
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> doSomething(
            @Valid @org.springframework.web.bind.annotation.RequestBody XxxRequest request
    );
}
```

### `modules/controller/src/main/java/{{GROUP}}/controller/XxxControllerImpl.java`

```java
package {{GROUP}}.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import {{GROUP}}.client.XxxController;
import {{GROUP}}.client.dto.XxxRequest;
import {{GROUP}}.usecase.XxxUseCase;

@RestController
@RequiredArgsConstructor
public class XxxControllerImpl implements XxxController {

    private final XxxUseCase useCase;

    @Override
    public ResponseEntity<Void> doSomething(XxxRequest request) {
        useCase.handle(request);
        return ResponseEntity.accepted().build();
    }
}
```

### `modules/infrastructure/src/main/java/{{BASE_PACKAGE}}/{{APP_CLASS}}.java`

```java
package {{BASE_PACKAGE}};

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class {{APP_CLASS}} {
    public static void main(String[] args) {
        SpringApplication.run({{APP_CLASS}}.class, args);
    }
}
```

---

## Dependency Flow (Clean Architecture)

```
[controller] ──▶ [usecase] ──▶ [domain] ◀── [client] ◀── [controller]
     │               │                           │
     └───────────────┴──────────────────────▶ [client]
                     │
                     └──▶ [ai-client] ──▶ [domain]

[infrastructure] агрегирует всё и содержит точку входа
```

Правила:
- `domain` ни от кого не зависит.
- `client` содержит API-контракт: интерфейс + DTO + View + `@FeignClient`. Публикуется в Nexus.
- `ai-client` реализует адаптер к LLM, зависит от `domain`.
- `usecase` зависит от `domain`, `ai-client` и `client` — последний нужен, т.к. маперы в `usecase.mapper` маппят доменные модели в `client.view` / `client.dto`.
- `controller` реализует интерфейсы из `client`, использует `usecase`, видит `domain` напрямую.
- `infrastructure` — composition root, тянет все модули.

---

## How to Instantiate a New Service

1. Скопируй структуру директорий, заменив `{{PROJECT_NAME}}`, `{{GROUP}}`, `{{APP_CLASS}}`.
2. Скорректируй список модулей в `settings.gradle` (добавь/удали строки `include` и `project(':x').projectDir`).
3. Добавь/удали зависимости между модулями в их `build.gradle`.
4. Все новые версии библиотек — только в `gradle.properties`.
5. Новые Spring-зависимости без явной версии (покрываются BOM).
6. API-контракт (интерфейсы + DTO) — всегда в модуль `client`. Публикуй его в Nexus отдельно от fat jar.