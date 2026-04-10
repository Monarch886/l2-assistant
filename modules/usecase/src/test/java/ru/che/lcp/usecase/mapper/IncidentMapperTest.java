package ru.che.lcp.usecase.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.che.lcp.aiclient.md.IncidentMr;
import ru.che.lcp.client.dto.HypothesisDto;
import ru.che.lcp.client.enums.CriticalityTypeDto;
import ru.che.lcp.client.view.IncidentView;
import ru.che.lcp.domain.Hypothesis;
import ru.che.lcp.domain.Incident;
import ru.che.lcp.domain.User;
import ru.che.lcp.domain.enums.CriticalityType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IncidentMapperImpl.class, HypothesisMapperImpl.class, UserMapperImpl.class})
class IncidentMapperTest {

    @Autowired
    IncidentMapper mapper;

    @Test
    void toView_fullIncident_mapsAllFields() {
        Incident incident = Incident.builder()
                .category("DATABASE")
                .description("DB pool exhausted")
                .criticality(CriticalityType.HIGH)
                .user(User.builder().id("u1").name("Иван").phone("+79001234567").build())
                .hypotheses(List.of(
                        Hypothesis.builder()
                                .possibleCause("Connection leak")
                                .possibleActions(List.of("Restart service", "Check metrics"))
                                .confidence(80)
                                .build()
                ))
                .build();

        IncidentView view = mapper.toView(incident);

        assertThat(view.getCategory()).isEqualTo("DATABASE");
        assertThat(view.getDescription()).isEqualTo("DB pool exhausted");
        assertThat(view.getCriticality()).isEqualTo(CriticalityTypeDto.HIGH);
        assertThat(view.getUser().getId()).isEqualTo("u1");
        assertThat(view.getUser().getName()).isEqualTo("Иван");
        assertThat(view.getHypotheses()).hasSize(1);

        HypothesisDto hypothesis = view.getHypotheses().get(0);
        assertThat(hypothesis.getPossibleCause()).isEqualTo("Connection leak");
        assertThat(hypothesis.getPossibleActions()).containsExactly("Restart service", "Check metrics");
        assertThat(hypothesis.getConfidence()).isEqualTo(80);
    }

    @Test
    void toView_nullUser_mapsWithNullUser() {
        Incident incident = Incident.builder()
                .category("NETWORK")
                .description("Latency spike")
                .criticality(CriticalityType.MEDIUM)
                .user(null)
                .hypotheses(List.of())
                .build();

        IncidentView view = mapper.toView(incident);

        assertThat(view.getUser()).isNull();
        assertThat(view.getHypotheses()).isEmpty();
    }

    @Test
    void toView_criticalityMappedByName() {
        for (CriticalityType type : CriticalityType.values()) {
            Incident incident = Incident.builder()
                    .criticality(type)
                    .hypotheses(List.of())
                    .build();

            IncidentView view = mapper.toView(incident);

            assertThat(view.getCriticality().name()).isEqualTo(type.name());
        }
    }

    @Test
    void toModel_fullMr_mapsFieldsWithoutUser() {
        IncidentMr mr = IncidentMr.builder()
                .category("AUTH_SERVICE")
                .description("Auth failures spiking")
                .criticality(CriticalityType.CRITICAL)
                .hypotheses(List.of(
                        Hypothesis.builder()
                                .possibleCause("Token expiry misconfiguration")
                                .possibleActions(List.of("Check JWT settings"))
                                .confidence(90)
                                .build()
                ))
                .build();

        Incident model = mapper.toModel(mr);

        assertThat(model.getCategory()).isEqualTo("AUTH_SERVICE");
        assertThat(model.getDescription()).isEqualTo("Auth failures spiking");
        assertThat(model.getCriticality()).isEqualTo(CriticalityType.CRITICAL);
        assertThat(model.getUser()).isNull();
        assertThat(model.getHypotheses()).hasSize(1);
        assertThat(model.getHypotheses().get(0).getConfidence()).isEqualTo(90);
    }

    @Test
    void toModel_nullHypotheses_mapsWithNullHypotheses() {
        IncidentMr mr = IncidentMr.builder()
                .category("UNKNOWN")
                .criticality(CriticalityType.LOW)
                .hypotheses(null)
                .build();

        Incident model = mapper.toModel(mr);

        assertThat(model.getUser()).isNull();
        assertThat(model.getHypotheses()).isNull();
    }
}