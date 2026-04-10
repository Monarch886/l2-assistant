package ru.che.lcp.usecase.util;

import java.util.regex.Pattern;

/**
 * Вычищает чувствительные данные из текста перед отправкой в LLM.
 * Заменяет найденные данные токенами-плейсхолдерами, чтобы контекст инцидента сохранялся.
 */
public class SensitiveDataUtil {

    // email: user.name+tag@sub.domain.com
    private static final Pattern EMAIL = Pattern.compile(
            "[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}",
            Pattern.CASE_INSENSITIVE
    );

    // IBAN: RU12 3456 7890 1234 5678 9 (с пробелами или без; последняя группа может быть неполной)
    private static final Pattern IBAN = Pattern.compile(
            "\\b[A-Z]{2}\\d{2}[\\s]?(?:[A-Z0-9]{4}[\\s]?){1,6}[A-Z0-9]{1,4}\\b"
    );

    // Телефон: +7/8 в любом форматировании, международные +X..X
    private static final Pattern PHONE = Pattern.compile(
            "(?:\\+7|8)[\\s\\-]?\\(?\\d{3}\\)?[\\s\\-]?\\d{3}[\\s\\-]?\\d{2}[\\s\\-]?\\d{2}"
                    + "|\\+(?!7)\\d{1,3}[\\s\\-]?\\(?\\d{1,4}\\)?(?:[\\s\\-]?\\d{1,4}){2,4}"
    );

    // Имена: 2–3 слова с заглавной буквы (кириллица), минимум 3 символа каждое.
    // Не ловим одиночные слова — слишком много ложных срабатываний.
    private static final Pattern NAME_CYRILLIC = Pattern.compile(
            "[А-ЯЁ][а-яё]{2,}(?:\\s[А-ЯЁ][а-яё]{2,}){1,2}"
    );
    // Латинские имена: только после слов-индикаторов, чтобы избежать ложных срабатываний
    // на технических терминах (Server, Database, Application и т.п.)
    private static final Pattern NAME_LATIN = Pattern.compile(
            "(?i)(client|user|contact|by|from|customer)\\s+([A-Z][a-z]{2,}(?:\\s[A-Z][a-z]{2,}){1,2})"
    );

    public static String sanitize(String text) {
        // Порядок важен: IBAN и email раньше телефона (чтобы не испортить частичные совпадения)
        text = EMAIL.matcher(text).replaceAll("[EMAIL]");
        text = IBAN.matcher(text).replaceAll("[IBAN]");
        text = PHONE.matcher(text).replaceAll("[PHONE]");
        text = NAME_CYRILLIC.matcher(text).replaceAll("[NAME]");
        text = NAME_LATIN.matcher(text).replaceAll("$1 [NAME]");
        return text;
    }
}