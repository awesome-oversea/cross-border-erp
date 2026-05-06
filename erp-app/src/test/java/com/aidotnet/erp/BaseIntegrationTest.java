package com.aidotnet.erp;

import com.aidotnet.erp.app.ErpApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ErpApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    static {
        System.setProperty("spring.profiles.active", "test");
    }
}
