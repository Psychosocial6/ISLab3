package org.backend.lab3.transaction;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.backend.lab3.imports.ImportService;
import org.backend.lab3.persons.PersonService;
import org.backend.lab3.persons.dto.PersonRequest;
import org.backend.lab3.persons.dto.PersonResponse;
import org.backend.lab3.utils.ImportMapper;
import org.backend.lab3.utils.ImportValidator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class DbParticipant implements TransactionParticipant {
    private final PlatformTransactionManager txManager;
    private final PersonService personService;
    private final ImportService importService;
    private final ImportValidator validator;
    private final ImportMapper importMapper;

    @Override
    public boolean prepare(TransactionContext context) {
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition();
            def.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
            TransactionStatus status = txManager.getTransaction(def);
            context.setTransactionStatus(status);

            List<PersonRequest> personRequests = importMapper.parseRequest(context.getFile());
            validator.validateImport(personRequests);

            List<PersonResponse> responses = personService.importPersons(personRequests);
            importService.logSuccessImport(context.getUser().getId(), (long) responses.size(), context.getFileKey());
            context.setResponses(responses);
            return true;
        } catch (Exception e) {
            log.error("db prepare failed: {}", e.getMessage());
            context.setError(e);
            rollback(context);
            return false;
        }
    }

    @Override
    public void commit(TransactionContext context) {
        if (context.getTransactionStatus() != null) {
            txManager.commit(context.getTransactionStatus());
            log.info("db transaction committed");
        }
    }

    @Override
    public void rollback(TransactionContext context) {
        if (context.getTransactionStatus() != null && !context.getTransactionStatus().isCompleted()) {
            txManager.rollback(context.getTransactionStatus());
            log.info("db transaction rollback");
        }
    }
}
