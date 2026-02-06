package config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import alchemy.config.Autoconfiguration;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ActiveProfiles("test")
@ContextConfiguration(classes = Autoconfiguration.class)
public class AbstractTest {

    @BeforeEach
    void logStart() {
    	log.info("=============== STARTING TEST ==============");
    }

    @AfterEach
    void logEnd() {
    	log.info("================ ENDING TEST ===============");
    }

}
