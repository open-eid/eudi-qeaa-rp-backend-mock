package ee.ria.eudi.qeaa.rp.backend.repository;

import ee.ria.eudi.qeaa.rp.backend.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {

    Optional<Transaction> findByTransactionIdAndResponseCode(String transactionId, String responseCode);

    Optional<Transaction> findByRequestObjectRequestUriId(String requestUriId);

    Optional<Transaction> findByRequestObjectState(String state);
}
