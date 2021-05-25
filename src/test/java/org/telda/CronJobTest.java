package org.telda;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CronJobTest {

    private final CronJob cronJob = new CronJob(Duration.ofMinutes(30), Duration.ofMinutes(60),
            () -> System.out.println("cron"), UUID.randomUUID());

    @Test
    public void testCronJobInitialisation() {
        assertEquals(0, cronJob.getFailedRuns());
        assertEquals(0, cronJob.getSuccessfulRuns());
        assertEquals(Instant.now().plus(cronJob.getSchedulingFrequency()).truncatedTo(ChronoUnit.MINUTES),
                cronJob.nextExecutionTime.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    public void testSetNextExecutionTime() {
        Instant instant = Instant.now();
        cronJob.setNextExecutionTime(instant);
        assertEquals(instant.plus(cronJob.getSchedulingFrequency()),
                cronJob.nextExecutionTime);
    }

    @Test
    public void testCompareToAnotherCronJob() {
        CronJob aheadInTime = new CronJob(Duration.ofMinutes(30), Duration.ofMinutes(60),
                () -> System.out.println("cron"), UUID.randomUUID());
        aheadInTime.setNextExecutionTime(Instant.now().plusSeconds(1));
        assertTrue(cronJob.compareTo(aheadInTime) < 0);
    }

    @Test
    public void testIncrementSuccessfulRuns() {
        assertEquals(0, cronJob.getSuccessfulRuns());

        assertEquals(1, cronJob.incrementSuccessfulRuns());
    }

    @Test
    public void testIncrementFailedRuns() {
        CronJob cronJob = new CronJob(Duration.ofMinutes(30), Duration.ofMinutes(60),
                () -> System.out.println("cron"), UUID.randomUUID());
        assertEquals(0, cronJob.getFailedRuns());

        assertEquals(1, cronJob.incrementFailedRuns());
    }
}
