---
name: mapstruct
description: MapStruct conventions for this project — mapper placement, method naming (toView/toDto/toModel/toEntity), parameter naming by layer, uses={} for nested models, package-private sub-mappers, mandatory @Mapper policies
---

## Placement

Все маперы — в пакете `usecase.mapper`. Юзкейс-слой владеет маппингом, потому что стоит на границе между domain и API.

```
usecase/
  mapper/
    IncidentMapper.java    ← public, маппит top-level доменную модель
    HypothesisMapper.java  ← package-private, маппит вложенную модель
    UserMapper.java        ← package-private, маппит вложенную модель
```

## Правило: 1 Mapper = 1 Domain Model

Каждый доменный класс получает свой отдельный mapper. Никогда не объединять несвязанные модели.

## Обязательные Политики @Mapper

Все маперы без исключения объявляют 3 политики:

```java
import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;
import static org.mapstruct.ReportingPolicy.ERROR;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ERROR,           // незамапленное поле = ошибка компиляции
        nullValueCheckStrategy = ALWAYS,        // всегда проверять source на null перед маппингом
        nullValuePropertyMappingStrategy = IGNORE) // null в source → не трогать target поле
interface SomeMapper { ...
}
```

| Политика                                    | Значение                                        | Зачем                       |
|---------------------------------------------|-------------------------------------------------|-----------------------------|
| `unmappedTargetPolicy = ERROR`              | незамапленное поле в target → ошибка компиляции | не пропустить поле молча    |
| `nullValueCheckStrategy = ALWAYS`           | проверять каждое source-поле на null            | защита от NPE при маппинге  |
| `nullValuePropertyMappingStrategy = IGNORE` | null source → target поле не трогать            | не затирать дефолты null'ом |

Если поле намеренно не маппится — явно указывать `@Mapping(target = "field", ignore = true)`.

## Маперы Используют Друг Друга

Когда доменная модель содержит вложенные объекты — родительский маппер делегирует через `uses = {}`.
Вложенные маперы **package-private**. Публичен только тот, кого инжектируют сервисы.

```java
// public — инжектируется в UseCase/Controller
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ERROR,
        nullValueCheckStrategy = ALWAYS,
        nullValuePropertyMappingStrategy = IGNORE,
        uses = {HypothesisMapper.class, UserMapper.class})
public interface IncidentMapper {
    IncidentView toView(Incident model);

    @Mapping(target = "someField", ignore = true)
    Incident toModel(IncidentMr mr);
}

// package-private — используется только другими маперами
@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ERROR,
        nullValueCheckStrategy = ALWAYS,
        nullValuePropertyMappingStrategy = IGNORE)
interface HypothesisMapper {
    HypothesisDto toDto(Hypothesis model);
}
```

## Названия Методов

| Метод      | Направление                        |
|------------|------------------------------------|
| `toView`   | domain model → API response (View) |
| `toDto`    | domain model → вложенный DTO       |
| `toModel`  | DTO / entity / MR → domain model   |
| `toEntity` | domain model → DB entity           |

Запрещено: `toResponse`, `convert`, `map`, `from`, `transform`, `toXxxResponse`.

## Названия Параметров Отражают Слой Источника

| Слой-источник         | Имя параметра | Пример                           |
|-----------------------|---------------|----------------------------------|
| Domain model          | `model`       | `toView(Incident model)`         |
| API DTO / request     | `dto`         | `toModel(IncidentRequest dto)`   |
| DB entity             | `entity`      | `toModel(IncidentEntity entity)` |
| AI/LLM machine result | `mr`          | `toModel(IncidentMr mr)`         |
| API view              | `view`        | `toModel(IncidentView view)`     |

## Нейминг Классов по Слоям

| Слой                  | Пакет            | Суффикс класса | Примеры                          |
|-----------------------|------------------|----------------|----------------------------------|
| Domain model          | `domain`         | (нет)          | `Incident`, `User`, `Hypothesis` |
| Domain enum           | `domain.enums`   | `Type`         | `CriticalityType`                |
| API response          | `client.view`    | `View`         | `IncidentView`                   |
| Nested API DTO        | `client.dto`     | `Dto`          | `HypothesisDto`, `UserDto`       |
| API enum              | `client.enums`   | `TypeDto`      | `CriticalityTypeDto`             |
| API request           | `client.reqres`  | `Request`      | `IncidentRequest`                |
| AI/LLM response model | `aiclient.md`    | `Mr`           | `IncidentMr`                     |
| DB entity             | `infrastructure` | `Entity`       | `IncidentEntity`                 |

## Шаблон для Нового Доменного Объекта

1. Создать `XxxMapper` (public) в `usecase.mapper` — со всеми тремя политиками
2. Создать package-private маперы для каждого вложенного типа — тоже со всеми политиками
3. Прописать их в `uses = {NestedMapper.class, ...}` родительского
4. Имена методов строго по таблице выше
5. Имена параметров строго по слою источника
6. Незамапленные поля — явный `@Mapping(target = "x", ignore = true)`, иначе ошибка компиляции