package ee.ria.eudi.qeaa.rp.backend.model;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseObject {
    @Lob
    private String vpToken;
    @Lob
    private String presentationSubmission;
}
