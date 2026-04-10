package ru.che.lcp.usecase.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveDataUtilTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "user.name+tag@sub.domain.org",
            "USER@EXAMPLE.COM"
    })
    void sanitize_email_replacedWithToken(String email) {
        String result = SensitiveDataUtil.sanitize("Incident from " + email + " reported");
        assertThat(result).contains("[EMAIL]").doesNotContain(email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "RU12 3456 7890 1234 5678 9",
            "DE89370400440532013000"
    })
    void sanitize_iban_replacedWithToken(String iban) {
        String result = SensitiveDataUtil.sanitize("Account " + iban + " affected");
        assertThat(result).contains("[IBAN]").doesNotContain(iban);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+7 912 345-67-89",
            "+79123456789",
            "8(912)345-67-89",
            "8 912 345 67 89"
    })
    void sanitize_phone_replacedWithToken(String phone) {
        String result = SensitiveDataUtil.sanitize("Contact: " + phone);
        assertThat(result).contains("[PHONE]").doesNotContain(phone);
    }

    @Test
    void sanitize_cyrillicFullName_replacedWithToken() {
        String result = SensitiveDataUtil.sanitize("Клиент Иван Иванов сообщил об инциденте");
        assertThat(result).contains("[NAME]").doesNotContain("Иван Иванов");
    }

    @Test
    void sanitize_latinFullName_replacedWithToken() {
        String result = SensitiveDataUtil.sanitize("Client John Smith reported an issue");
        assertThat(result).contains("[NAME]").doesNotContain("John Smith");
    }

    @Test
    void sanitize_singleCapitalizedWord_notReplaced() {
        // Single capitalized word is not treated as a name — too many false positives
        String result = SensitiveDataUtil.sanitize("Server Database Application error");
        assertThat(result).isEqualTo("Server Database Application error");
    }

    @Test
    void sanitize_noSensitiveData_returnedUnchanged() {
        String input = "Database connection pool exhausted after deploy";
        assertThat(SensitiveDataUtil.sanitize(input)).isEqualTo(input);
    }

    @Test
    void sanitize_multipleTypesInOneText_allReplaced() {
        String input = "User john.doe@example.com (+79001234567) Иван Иванов IBAN: RU12 3456 7890 1234 5678 9";
        String result = SensitiveDataUtil.sanitize(input);

        assertThat(result)
                .contains("[EMAIL]", "[PHONE]", "[NAME]", "[IBAN]")
                .doesNotContain("john.doe@example.com")
                .doesNotContain("+79001234567")
                .doesNotContain("Иван Иванов")
                .doesNotContain("RU12");
    }
}