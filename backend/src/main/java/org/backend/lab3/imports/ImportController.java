package org.backend.lab3.imports;

import lombok.RequiredArgsConstructor;
import org.backend.lab3.imports.dto.ImportLog;
import org.backend.lab3.minio.MinioService;
import org.backend.lab3.user.User;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/api/import")
@RestController
public class ImportController {
    private final ImportService importService;
    private final MinioService minioService;

    @GetMapping("/history")
    public ResponseEntity<List<ImportLog>> getHistory(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(importService.getImportHistory(user));
    }

    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        InputStream stream = minioService.getFile(fileName);
        Resource resource = new InputStreamResource(stream);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
