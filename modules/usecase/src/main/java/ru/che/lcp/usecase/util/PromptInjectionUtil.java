package ru.che.lcp.usecase.util;

import ru.che.lcp.client.exception.PromptInjectionException;

import java.util.List;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 * Защита от prompt injection — детектирует попытки перезаписать инструкции LLM.
 * При обнаружении бросает {@link PromptInjectionException} (400 Bad Request).
 *
 * Работает в паре со структурной изоляцией в AnalysisPort:
 * даже если паттерн не пойман здесь, LLM видит текст как данные внутри тегов, а не как инструкции.
 */
public class PromptInjectionUtil {

    // --- Перезапись инструкций ---
    private static final Pattern OVERRIDE_INSTRUCTIONS = Pattern.compile(
            "(?:ignore|disregard|bypass|forget|override)\\s+"
            + "(?:all\\s+)?(?:(?:your|the|prior|previous|above)\\s+){1,2}"
            + "(?:instructions?|rules?|prompt|guidelines?|constraints?|context)",
            CASE_INSENSITIVE
    );

    private static final Pattern NEW_INSTRUCTIONS = Pattern.compile(
            "(?:your\\s+)?new\\s+(?:instructions?|task|role|persona|rules?|goal|objective)",
            CASE_INSENSITIVE
    );

    private static final Pattern FROM_NOW_ON = Pattern.compile(
            "from\\s+now\\s+on|henceforth|starting\\s+now",
            CASE_INSENSITIVE
    );

    // --- Смена роли / персонажа ---
    private static final Pattern ROLE_SWITCH = Pattern.compile(
            "(?:you\\s+are\\s+now|act\\s+(?:as|like)|pretend\\s+(?:to\\s+be|you\\s+are|you're)|"
            + "roleplay\\s+as|behave\\s+as|simulate\\s+(?:a|an|the)|take\\s+(?:the\\s+)?role\\s+of)",
            CASE_INSENSITIVE
    );

    // --- Инъекция через разделители / теги ---
    private static final Pattern DELIMITER_INJECTION = Pattern.compile(
            // XML/HTML теги переключения ролей
            "</?\s*(?:system|prompt|instruction|assistant|user|human|ai)\s*>"
            // LLaMA-style [INST], [SYS]
            + "|\\[/?(?:INST|SYS|SYSTEM|ASSISTANT|USER|END|START)\\]"
            // ChatML <|im_start|>, <|im_end|>
            + "|<\\|(?:im_start|im_end|endoftext|startoftext)\\|>"
            // Markdown заголовки для переключения
            + "|#{3,}\\s*(?:SYSTEM|INSTRUCTION|PROMPT|END|OVERRIDE|NEW TASK)",
            CASE_INSENSITIVE
    );

    // --- Попытка извлечь system prompt ---
    private static final Pattern EXTRACT_PROMPT = Pattern.compile(
            "(?:reveal|show|print|repeat|output|display|tell\\s+me|expose|leak)\\s+"
            + "(?:(?:your|the|my)\\s+)?(?:system\\s+)?(?:prompt|instructions?|rules?|configuration|settings?)",
            CASE_INSENSITIVE
    );

    private static final List<Entry> CHECKS = List.of(
            new Entry(OVERRIDE_INSTRUCTIONS, "instruction override"),
            new Entry(NEW_INSTRUCTIONS,      "new instructions injection"),
            new Entry(FROM_NOW_ON,           "behavioral redirect"),
            new Entry(ROLE_SWITCH,           "role/persona switch"),
            new Entry(DELIMITER_INJECTION,   "delimiter injection"),
            new Entry(EXTRACT_PROMPT,        "system prompt extraction")
    );

    public static void check(String text) {
        for (Entry entry : CHECKS) {
            if (entry.pattern().matcher(text).find()) {
                throw new PromptInjectionException(entry.label());
            }
        }
    }

    private record Entry(Pattern pattern, String label) {}
}