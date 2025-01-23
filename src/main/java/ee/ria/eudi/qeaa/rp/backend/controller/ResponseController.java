package ee.ria.eudi.qeaa.rp.backend.controller;

import ee.ria.eudi.qeaa.rp.backend.configuration.properties.RpBackendProperties;
import ee.ria.eudi.qeaa.rp.backend.error.ServiceException;
import ee.ria.eudi.qeaa.rp.backend.model.Transaction;
import ee.ria.eudi.qeaa.rp.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ResponseController {
    public static final String RESPONSE_REQUEST_MAPPING = "/response";
    private final RpBackendProperties rpBackendProperties;
    private final TransactionRepository transactionRepository;

    @PostMapping(path = RESPONSE_REQUEST_MAPPING, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE, params = "response")
    public ResponseCodeResponse postResponseObject(@RequestParam(name = "response") String response,
                                                   @RequestParam(name = "state") String state) {
        log.debug("Received response object for state: {}", state);
        Transaction transaction = updateTransaction(response, state);
        URI redirectUri = UriComponentsBuilder.fromUriString(rpBackendProperties.callbackUrl())
            .queryParam("response_code", transaction.getResponseCode())
            .build().toUri();
        return ResponseCodeResponse.builder().redirectUri(redirectUri).build();
    }

    private Transaction updateTransaction(String response, String state) {
        Transaction transaction = transactionRepository.findByRequestObjectState(state)
            .orElseThrow(() -> new ServiceException("Invalid state"));
        transaction.setResponseObject(response);
        transactionRepository.save(transaction);
        return transaction;
    }

    @GetMapping(path = RESPONSE_REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseObjectResponse getResponseObject(@RequestParam("transaction_id") String transactionId,
                                                    @RequestParam("response_code") String responseCode) {
        log.debug("Getting response object for transaction_id: {} and response_code: {}", transactionId, responseCode);
        Transaction transaction = transactionRepository.findByTransactionIdAndResponseCode(transactionId, responseCode)
            .orElseThrow(() -> new ServiceException("Response Object not found"));
        transactionRepository.delete(transaction);
        String state = transaction.getRequestObject().getState();
        log.debug("Request object state: {}", state);
        return ResponseObjectResponse.builder()
            .response(transaction.getResponseObject())
            .state(state)
            .build();
    }
}
