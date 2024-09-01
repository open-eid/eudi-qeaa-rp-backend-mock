package ee.ria.eudi.qeaa.rp.backend.controller;

import com.nimbusds.jwt.SignedJWT;
import ee.ria.eudi.qeaa.rp.backend.configuration.properties.RpBackendProperties;
import ee.ria.eudi.qeaa.rp.backend.error.ServiceException;
import ee.ria.eudi.qeaa.rp.backend.model.RequestObject;
import ee.ria.eudi.qeaa.rp.backend.model.Transaction;
import ee.ria.eudi.qeaa.rp.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.text.ParseException;
import java.time.Instant;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class RequestController {
    public static final String REQUEST_POST_REQUEST_MAPPING = "/request";
    public static final String REQUEST_GET_REQUEST_MAPPING = "/request.jwt/{requestUriId}";
    public static final String APPLICATION_OAUTH_AUTHZ_REQ_JWT = "application/oauth-authz-req+jwt";
    private final RpBackendProperties rpBackendProperties;
    private final TransactionRepository transactionRepository;

    @PostMapping(path = REQUEST_POST_REQUEST_MAPPING, consumes = "application/jws", produces = MediaType.APPLICATION_JSON_VALUE)
    public RequestObjectResponse postRequestObject(@RequestBody String requestObject) throws ParseException {
        Transaction transaction = startTransaction(requestObject);
        return RequestObjectResponse.builder()
            .requestUri(URI.create(rpBackendProperties.baseUrl() + "/request.jwt/" + transaction.getRequestObject().getRequestUriId()))
            .transactionId(transaction.getTransactionId())
            .responseCode(transaction.getResponseCode())
            .expiryTime(transaction.getRequestObject().getExpiryTime().getEpochSecond())
            .build();
    }

    @GetMapping(path = REQUEST_GET_REQUEST_MAPPING, produces = APPLICATION_OAUTH_AUTHZ_REQ_JWT)
    public String getRequestObject(@PathVariable("requestUriId") String requestUriId) {
        return transactionRepository.findByRequestObjectRequestUriId(requestUriId)
            .map(Transaction::getRequestObject)
            .filter(ro -> ro.getExpiryTime().isAfter(Instant.now()))
            .map(RequestObject::getValue)
            .orElseThrow(() -> new ServiceException("Request Object not found"));
    }

    private Transaction startTransaction(String request) throws ParseException {
        String state = getState(request);
        RequestObject requestObject = RequestObject.builder()
            .requestUriId(UUID.randomUUID().toString())
            .state(state)
            .value(request)
            .expiryTime(Instant.now().plusSeconds(rpBackendProperties.ttl().requestUri().toSeconds()))
            .build();
        Transaction transaction = Transaction.builder()
            .transactionId(UUID.randomUUID().toString())
            .responseCode(UUID.randomUUID().toString())
            .requestObject(requestObject).build();
        transactionRepository.save(transaction);
        return transaction;
    }

    private String getState(String request) throws ParseException {
        SignedJWT jwt = SignedJWT.parse(request);
        return jwt.getJWTClaimsSet().getStringClaim("state");
    }
}
