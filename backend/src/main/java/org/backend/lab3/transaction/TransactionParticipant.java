package org.backend.lab3.transaction;

public interface TransactionParticipant {
    boolean prepare(TransactionContext context);

    void commit(TransactionContext context);

    void rollback(TransactionContext context);
}
