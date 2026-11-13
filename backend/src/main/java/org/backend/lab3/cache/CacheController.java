package org.backend.lab3.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/api/cache")
@RestController
public class CacheController {
    private final CacheAspect cacheAspect;

    @PostMapping("/toggle")
    public ResponseEntity<String> toggle() {
        if (cacheAspect.isLoggingEnabled()) {
            cacheAspect.setLoggingEnabled(false);
            return ResponseEntity.ok("Cache logging disabled");
        }
        cacheAspect.setLoggingEnabled(true);
        return ResponseEntity.ok("Cache logging enabled");
    }

    @GetMapping("/logging-status")
    public ResponseEntity<Boolean> getStatus() {
        return ResponseEntity.ok(cacheAspect.isLoggingEnabled());
    }
}
