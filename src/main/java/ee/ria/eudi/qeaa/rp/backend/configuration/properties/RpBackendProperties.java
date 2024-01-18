package ee.ria.eudi.qeaa.rp.backend.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Validated
@ConfigurationProperties(prefix = "eudi.rp-backend")
public record RpBackendProperties(
    @NotBlank
    @Pattern(regexp = ".*(?<!/)$")
    String baseUrl,
    @NotBlank
    @Pattern(regexp = ".*(?<!/)$")
    String callbackUrl,
    @NotNull
    TimeToLive ttl) {

    @ConfigurationProperties(prefix = "eudi.rp-backend.ttl")
    public record TimeToLive(
        @NotNull
        Duration requestUri) {
    }
}
