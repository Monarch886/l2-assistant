package ru.che.lcp.aiclient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import ru.che.lcp.aiclient.md.IncidentMr;
import ru.che.lcp.domain.enums.CriticalityType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalysisPortTest {

    @Mock
    ChatClient chatClient;
    @Mock
    ChatClient.ChatClientRequestSpec requestSpec;
    @Mock
    ChatClient.CallResponseSpec callResponseSpec;
    @InjectMocks
    AnalysisPort analysisPort;

    @Test
    void analyze_successfulCall_returnsMrFromLlm() {
        IncidentMr expected = IncidentMr.builder()
                .category("DATABASE")
                .description("DB pool exhausted")
                .criticality(CriticalityType.HIGH)
                .build();

        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(IncidentMr.class)).thenReturn(expected);

        IncidentMr result = analysisPort.analyze("DB connection pool exhausted");

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void analyze_wrapsDescriptionInXmlTags() {
        when(chatClient.prompt()).thenReturn(requestSpec);
        when(requestSpec.system(anyString())).thenReturn(requestSpec);
        when(requestSpec.user(anyString())).thenReturn(requestSpec);
        when(requestSpec.call()).thenReturn(callResponseSpec);
        when(callResponseSpec.entity(IncidentMr.class)).thenReturn(IncidentMr.builder().build());

        analysisPort.analyze("some incident text");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(requestSpec).user(captor.capture());
        assertThat(captor.getValue())
                .contains("<incident_description>")
                .contains("some incident text")
                .contains("</incident_description>");
    }

    @Test
    void fallback_returnsFallbackMr() {
        IncidentMr fallback = analysisPort.fallback(new RuntimeException("OpenAI timeout"));

        assertThat(fallback.getCategory()).isEqualTo("UNKNOWN");
        assertThat(fallback.getCriticality()).isEqualTo(CriticalityType.HIGH);
        assertThat(fallback.getHypotheses()).hasSize(1);
        assertThat(fallback.getHypotheses().get(0).getConfidence()).isEqualTo(100);
        assertThat(fallback.getDescription()).isNotBlank();
    }

    @Test
    void fallback_calledWithAnyException_alwaysReturnsSafeResult() {
        IncidentMr result1 = analysisPort.fallback(new RuntimeException("timeout"));
        IncidentMr result2 = analysisPort.fallback(new IllegalStateException("circuit open"));

        assertThat(result1.getCategory()).isEqualTo(result2.getCategory());
        assertThat(result1.getCriticality()).isEqualTo(result2.getCriticality());
    }
}