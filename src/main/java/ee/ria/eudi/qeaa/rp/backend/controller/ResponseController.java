package ee.ria.eudi.qeaa.rp.backend.controller;

import ee.ria.eudi.qeaa.rp.backend.configuration.properties.RpBackendProperties;
import ee.ria.eudi.qeaa.rp.backend.error.ServiceException;
import ee.ria.eudi.qeaa.rp.backend.model.ResponseCodeResponse;
import ee.ria.eudi.qeaa.rp.backend.model.ResponseObject;
import ee.ria.eudi.qeaa.rp.backend.model.ResponseObjectResponse;
import ee.ria.eudi.qeaa.rp.backend.model.Transaction;
import ee.ria.eudi.qeaa.rp.backend.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequiredArgsConstructor
public class ResponseController {
    public static final String RESPONSE_REQUEST_MAPPING = "/response";
    private final RpBackendProperties rpBackendProperties;
    private final TransactionRepository transactionRepository;

    @PostMapping(path = RESPONSE_REQUEST_MAPPING, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseCodeResponse postResponseObject(@RequestParam(name = "vp_token") String vpToken,
                                                   @RequestParam(name = "presentation_submission") String presentationSubmission,
                                                   @RequestParam(name = "state") String state) {
        Transaction transaction = updateTransaction(vpToken, presentationSubmission, state);
        URI redirectUri = UriComponentsBuilder.fromUriString(rpBackendProperties.callbackUrl())
            .queryParam("response_code", transaction.getResponseCode())
            .build().toUri();
        return ResponseCodeResponse.builder().redirectUri(redirectUri).build();
    }

    private Transaction updateTransaction(String vpToken, String presentationSubmission, String state) {
        Transaction transaction = transactionRepository.findByRequestObjectState(state)
            .orElseThrow(() -> new ServiceException("Invalid state"));
        transaction.setResponseObject(ResponseObject.builder()
            .vpToken(vpToken)
            .presentationSubmission(presentationSubmission)
            .build());
        transactionRepository.save(transaction);
        return transaction;
    }

    @GetMapping(path = RESPONSE_REQUEST_MAPPING, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseObjectResponse getResponseObject(@RequestParam("transaction_id") String transactionId,
                                                    @RequestParam("response_code") String responseCode) {
        Transaction transaction = transactionRepository.findByTransactionIdAndResponseCode(transactionId, responseCode)
            .orElseThrow(() -> new ServiceException("Response Object not found"));
        transactionRepository.delete(transaction);
        return ResponseObjectResponse.builder()
            .vpToken(transaction.getResponseObject().getVpToken())
            .presentationSubmission(transaction.getResponseObject().getPresentationSubmission())
            .state(transaction.getRequestObject().getState())
            .build();
    }
}
