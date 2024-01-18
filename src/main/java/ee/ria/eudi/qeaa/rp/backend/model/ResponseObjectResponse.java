package ee.ria.eudi.qeaa.rp.backend.model;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Builder;

@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ResponseObjectResponse(
    String state,
    String vpToken,
    String presentationSubmission) {
}
