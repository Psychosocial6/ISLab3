package org.backend.lab3.transaction;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.backend.lab3.persons.dto.PersonResponse;
import org.backend.lab3.user.User;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class TransactionContext {
    private final String fileKey;
    private final MultipartFile file;
    private final User user;
    private TransactionStatus transactionStatus;
    private List<PersonResponse> responses;
    private Exception error;
}
