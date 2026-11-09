package org.backend.lab3.imports.dto;

import org.backend.lab3.imports.ImportStatus;

public record ImportLog(
        Long id,
        ImportStatus status,
        Long userId,
        Long addedCount
) {
}
