package org.telda;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class CronSchedulerTest {
    private static final Logger logger = LoggerFactory.getLogger(CronSchedulerTest.class);

    private final CronScheduler cronScheduler = new CronScheduler(10);

    @Test
    public void testRegisterCronJobSuccess(){
        UUID uuid = UUID.randomUUID();
        cronScheduler.registerCronJob(Duration.ofSeconds(1), Duration.ofSeconds(10), () -> System.out.println("cron"), uuid);
        assertTrue(cronScheduler.getRegisteredJobs().contains(uuid));
        assertEquals(1, cronScheduler.getPriorityQueue().size());
    }

    @Test
    public void testRegisterCronJobFails(){
        UUID uuid = UUID.randomUUID();
        cronScheduler.registerCronJob(Duration.ofSeconds(1), Duration.ofSeconds(10), () -> System.out.println("cron"), uuid);
        assertTrue(cronScheduler.getRegisteredJobs().contains(uuid));
        assertEquals(1, cronScheduler.getPriorityQueue().size());

        //now try registering a job with the same UUID, should throw an exception and registered jobs should remain unchanged
        assertThrows(RuntimeException.class, () -> cronScheduler.registerCronJob(Duration.ofSeconds(1), Duration.ofSeconds(10),
                () -> System.out.println("hello after 10 seconds"), uuid));
        assertEquals(1, cronScheduler.getRegisteredJobs().size());
        assertEquals(1, cronScheduler.getPriorityQueue().size());
    }

    @Test
    public void testRegisteredCronJobRunsSuccessfully() throws Exception{
        Duration schedulingFrequency = Duration.ofSeconds(2);
        int maxRuns = 5;
        RecordedCronJob recordedCronJob = new RecordedCronJob(maxRuns);
        cronScheduler.registerCronJob(Duration.ofSeconds(1), schedulingFrequency,
                recordedCronJob, UUID.randomUUID());
        Thread.sleep(11000);
        assertEquals(maxRuns, recordedCronJob.getNumRunTimesCounter());
        assertTrue(recordedCronJob.getTimeIntervals().stream().allMatch(i -> Math.abs(i.minus(schedulingFrequency).toMillis()) < 20));
    }
}
