package dk.nap.atlas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Black-box smoke-test: verificerer at hele Spring-application-konteksten
 * loader uden fejl med default profile.
 */
@SpringBootTest
@ActiveProfiles("test")
class AtlasApplicationTests {

    @Test
    void contextLoads() {
        // Hvis Spring fejler bønne-creation eller config, fejler denne test.
    }
}
