package org.backend.lab3;

import org.backend.lab3.exceptions.custom.MinioOperationException;
import org.backend.lab3.imports.ImportService;
import org.backend.lab3.persons.dto.PersonResponse;
import org.backend.lab3.security.Role;
import org.backend.lab3.transaction.DbParticipant;
import org.backend.lab3.transaction.MinioParticipant;
import org.backend.lab3.transaction.TransactionContext;
import org.backend.lab3.transaction.TransactionCoordinator;
import org.backend.lab3.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TransactionCoordinatorTest {
    @Mock
    private MinioParticipant minioParticipant;
    @Mock
    private DbParticipant dbParticipant;
    @Mock
    private ImportService importService;
    @InjectMocks
    private TransactionCoordinator coordinator;
    private MultipartFile multipartFile;
    private User user;

    @BeforeEach
    public void setup() {
        multipartFile = new MockMultipartFile(
                "file",
                "import.json",
                "application/json",
                "[]".getBytes());
        user = new User("admin", "admin", Role.ROLE_ADMIN);
        user.setId(1L);
    }

    @Test
    @DisplayName("Successful commit")
    public void successfulImport() {
        when(minioParticipant.prepare(any())).thenReturn(true);
        when(dbParticipant.prepare(any())).thenAnswer(invocation -> {
            TransactionContext ctx = invocation.getArgument(0);
            ctx.setResponses(Collections.emptyList());
            return true;
        });

        List<PersonResponse> responses = coordinator.importPersons(multipartFile, user);

        assertThat(responses).isNotNull();

        verify(minioParticipant).prepare(any(TransactionContext.class));
        verify(dbParticipant).prepare(any(TransactionContext.class));
        verify(minioParticipant).commit(any(TransactionContext.class));
        verify(dbParticipant).commit(any(TransactionContext.class));
        verify(minioParticipant, never()).rollback(any(TransactionContext.class));
        verify(dbParticipant, never()).rollback(any(TransactionContext.class));
    }

    @Test
    @DisplayName("Minio prepare() failed")
    public void minioPrepareFail() {
        when(minioParticipant.prepare(any())).thenReturn(false);

        assertThatThrownBy(() -> coordinator.importPersons(multipartFile, user))
                .isInstanceOf(MinioOperationException.class)
                .hasMessageContaining("MinIO preparation failed");

        verify(minioParticipant).rollback(any(TransactionContext.class));
        verifyNoInteractions(dbParticipant);
    }

    @Test
    @DisplayName("Minio commit() failed")
    public void minioCommitFail() {
        when(minioParticipant.prepare(any())).thenReturn(true);
        when(dbParticipant.prepare(any())).thenReturn(true);

        doThrow(new RuntimeException("minio commit failed"))
                .when(minioParticipant)
                .commit(any());

        assertThatThrownBy(() -> coordinator.importPersons(multipartFile, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Commit failed");

        verify(dbParticipant).rollback(any(TransactionContext.class));
        verify(minioParticipant).rollback(any(TransactionContext.class));
        verify(dbParticipant, never()).commit(any());
    }

    @Test
    @DisplayName("DB prepare() failed")
    public void dbPrepareFail() {
        when(minioParticipant.prepare(any())).thenReturn(true);
        when(dbParticipant.prepare(any())).thenAnswer(invocation -> {
            TransactionContext ctx = invocation.getArgument(0);
            ctx.setError(new RuntimeException("DB prepare failed"));
            return false;
        });

        assertThatThrownBy(() -> coordinator.importPersons(multipartFile, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database failed during import");

        verify(dbParticipant).rollback(any(TransactionContext.class));
        verify(minioParticipant).rollback(any(TransactionContext.class));
        verify(minioParticipant, never()).commit(any());
        verify(dbParticipant, never()).commit(any());
    }

    @Test
    @DisplayName("DB commit() failed")
    public void dbCommitFail() {
        when(minioParticipant.prepare(any())).thenReturn(true);
        when(dbParticipant.prepare(any())).thenReturn(true);

        doThrow(new RuntimeException("db commit failed")).when(dbParticipant).commit(any());

        assertThatThrownBy(() -> coordinator.importPersons(multipartFile, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Commit failed");

        verify(minioParticipant).commit(any());
        verify(dbParticipant).rollback(any(TransactionContext.class));
        verify(minioParticipant).rollback(any(TransactionContext.class));
    }

    @Test
    @DisplayName("Error dew to not passed validation")
    public void dbValidationError() {
        when(minioParticipant.prepare(any())).thenReturn(true);
        when(dbParticipant.prepare(any())).thenAnswer(invocation -> {
            TransactionContext ctx = invocation.getArgument(0);
            ctx.setError(new IllegalArgumentException("Validation failed"));
            return false;
        });

        assertThatThrownBy(() -> coordinator.importPersons(multipartFile, user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Validation failed");
        verify(minioParticipant).commit(any(TransactionContext.class));
        verify(importService).logFailedImport(eq(user.getId()), eq(0L), any());
    }


    @Test
    @DisplayName("Exception between minio prepare and db prepare")
    public void exceptionBetweenPrepare() {
        when(minioParticipant.prepare(any())).thenReturn(true);
        when(dbParticipant.prepare(any()))
                .thenThrow(new RuntimeException("db prepare failed"));

        assertThatThrownBy(() -> coordinator.importPersons(multipartFile, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database failed during import");

        verify(dbParticipant).rollback(any(TransactionContext.class));
        verify(minioParticipant).rollback(any(TransactionContext.class));
        verify(minioParticipant, never()).commit(any());
        verify(dbParticipant, never()).commit(any());
    }

    @Test
    @DisplayName("Exception between minio commit and db commit")
    public void exceptionBetweenCommit() {
        when(minioParticipant.prepare(any())).thenReturn(true);
        when(dbParticipant.prepare(any())).thenReturn(true);

        doThrow(new RuntimeException("db commit failed"))
                .when(dbParticipant).commit(any());

        assertThatThrownBy(() -> coordinator.importPersons(multipartFile, user))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Commit failed");

        verify(minioParticipant).commit(any(TransactionContext.class));
        verify(dbParticipant).commit(any(TransactionContext.class));
        verify(dbParticipant).rollback(any(TransactionContext.class));
        verify(minioParticipant).rollback(any(TransactionContext.class));
    }
}
