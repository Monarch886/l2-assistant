package ru.che.lcp.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Hypothesis {
    private String possibleCause;
    private List<String> possibleActions;
    private Integer confidence;
}
