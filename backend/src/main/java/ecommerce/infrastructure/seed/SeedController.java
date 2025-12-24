package ecommerce.infrastructure.seed;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/seed")
public class SeedController {

    private final SeedDataService seedDataService;

    @PostMapping
    public ResponseEntity<Void> seed() {
        seedDataService.generateSeedData();
        return ResponseEntity.ok().build();
    }
}
