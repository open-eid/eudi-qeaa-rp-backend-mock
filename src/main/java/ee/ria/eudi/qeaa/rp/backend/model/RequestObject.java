package ee.ria.eudi.qeaa.rp.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestObject {
    private String requestUriId;
    private String state;
    @Lob
    @Column(name = "request_object_value")
    private String value;
    private Instant expiryTime;
}
