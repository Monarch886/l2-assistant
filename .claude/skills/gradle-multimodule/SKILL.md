---
name: gradle-multimodule
description: Gradle multi-module Spring Boot conventions — all versions in gradle.properties, settings.gradle module wiring, root subprojects BOM block, java-library api vs implementation, BOM must be in root not submodule
---

## Версии — Только в `gradle.properties`

Никаких инлайн-версий в build-файлах. Всё в `gradle.properties`:

```properties
springBootVersion=3.4.4
springDependencyManagementVersion=1.1.7
mapstructVersion=1.6.3
lombokMapstructBindingVersion=0.2.0
springdocVersion=2.8.4
springCloudVersion=2024.0.1
springAiVersion=1.0.0
```

В build.gradle ссылаемся через `"${keyName}"`.

## `settings.gradle` — Подключение Модулей

```groovy
rootProject.name = 'ProjectName'

include 'domain', 'usecase', 'controller', 'client', 'ai-client', 'infrastructure'

project(':infrastructure').projectDir = file 'modules/infrastructure'
project(':domain').projectDir        = file 'modules/domain'
project(':usecase').projectDir       = file 'modules/usecase'
project(':controller').projectDir    = file 'modules/controller'
project(':client').projectDir        = file 'modules/client'
project(':ai-client').projectDir     = file 'modules/ai-client'
```

Каждый новый модуль: одна строка `include` + одна строка `project(':x').projectDir`.

## Root `build.gradle` — Все BOM Здесь

**Критично:** все BOM-импорты — только в `subprojects { dependencyManagement { ... } }` корневого файла.
Если BOM объявлен только в дочернем модуле, `infrastructure:bootJar` не сможет разрешить его транзитивные зависимости.

```groovy
subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    dependencyManagement {
        imports {
            mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
            mavenBom "org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"
            // добавляй сюда новые BOM, а не в дочерние модули
        }
    }
}
```

Spring-зависимости из BOM указываются **без версии**.

## `java-library` + `api` vs `implementation`

Когда публичные методы модуля возвращают типы из зависимости, используй `java-library` + `api`,
иначе потребители не увидят эти типы на compile classpath.

```groovy
// usecase/build.gradle
apply plugin: 'java-library'

dependencies {
    api project(':domain')          // domain-типы видны в controller
    implementation project(':ai-client')  // внутренняя деталь, наружу не торчит
    implementation 'org.springframework.boot:spring-boot-starter'
}
```

```groovy
// client/build.gradle — публикуемый API-артефакт
apply plugin: 'java-library'

dependencies {
    api 'org.springframework.boot:spring-boot-starter-web'
    api 'org.springframework.boot:spring-boot-starter-validation'
    api "org.springdoc:springdoc-openapi-starter-webmvc-ui:${springdocVersion}"
    api 'org.springframework.cloud:spring-cloud-starter-openfeign'
}
```

Правило: `api` = часть публичного API модуля. `implementation` = деталь реализации.

## Spring AI — Имя Артефакта Изменилось в 1.0.0

В milestone-версиях (M1–M6): `spring-ai-openai-spring-boot-starter`
В GA 1.0.0+: `spring-ai-starter-model-openai`

```groovy
// ai-client/build.gradle
dependencies {
    implementation 'org.springframework.ai:spring-ai-starter-model-openai'
}
```

Версия берётся из BOM (если BOM подключён в root), иначе явно: `"org.springframework.ai:spring-ai-starter-model-openai:${springAiVersion}"`.

## Только `infrastructure` Собирает fat-jar

```groovy
// infrastructure/build.gradle
plugins {
    id 'org.springframework.boot'   // только здесь!
}
```

Все остальные модули — обычные java-библиотеки без этого плагина.

## Порядок Annotation Processors (Lombok + MapStruct)

Всегда в таком порядке — иначе MapStruct не видит Lombok-методы:

```groovy
annotationProcessor 'org.projectlombok:lombok'
annotationProcessor "org.mapstruct:mapstruct-processor:${mapstructVersion}"
annotationProcessor "org.projectlombok:lombok-mapstruct-binding:${lombokMapstructBindingVersion}"
```