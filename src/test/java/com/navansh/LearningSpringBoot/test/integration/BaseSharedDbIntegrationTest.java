package com.navansh.LearningSpringBoot.test.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.Assumptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("it-shared")
public abstract class BaseSharedDbIntegrationTest {

    @Autowired
    private Environment environment;

    @BeforeEach
    void guardSharedDbExecution() {
        String enabled = environment.getProperty("IT_SHARED_DB_ENABLED", "false");
        Assumptions.assumeTrue("true".equalsIgnoreCase(enabled),
                "Set IT_SHARED_DB_ENABLED=true to run shared DB integration tests.");

        String datasourceUrl = environment.getProperty("spring.datasource.url", "");
        String allowedUrlFragment = environment.getProperty("it.shared-db.allowed-url-fragment", "pooler.supabase.com");
        Assumptions.assumeTrue(datasourceUrl.contains(allowedUrlFragment),
                "Shared DB integration tests are blocked for this datasource URL.");
    }
}

