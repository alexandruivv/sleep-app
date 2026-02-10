package com.noom.interview.fullstack.sleep;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles(SleepApplication.UNIT_TEST_PROFILE)
public class SleepApplicationTests {

    @Test
    void contextLoads() {
        Assertions.assertThat(true).isTrue();
    }
}
