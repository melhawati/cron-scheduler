package org.telda;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

class CronJob implements Comparable<CronJob> {
    private final Duration expectedRunTime;
    private final Duration schedulingFrequency;
    private final CronFunction cronFunction;
    private final UUID uuid;
    public Instant nextExecutionTime;
    private int successfulRuns;
    private int failedRuns;

    public CronJob(Duration expectedRunTime, Duration schedulingFrequency, Runnable cronFunction, UUID uuid) {
        this.expectedRunTime = expectedRunTime;
        this.schedulingFrequency = schedulingFrequency;
        this.cronFunction = new CronFunction(cronFunction);
        this.uuid = uuid;
        setNextExecutionTime(Instant.now());
    }

    @Override
    public int compareTo(CronJob o) {
        return (int) (this.getNextExecutionTime().getEpochSecond() - o.getNextExecutionTime().getEpochSecond());
    }

    public Instant getNextExecutionTime() {
        return nextExecutionTime;
    }

    public void setNextExecutionTime(Instant currentTime) {
        this.nextExecutionTime = currentTime.plus(schedulingFrequency);
    }

    public int incrementSuccessfulRuns() {
        successfulRuns++;
        return successfulRuns;
    }

    public int incrementFailedRuns() {
        failedRuns++;
        return failedRuns;
    }

    public int getSuccessfulRuns() {
        return successfulRuns;
    }

    public int getFailedRuns() {
        return failedRuns;
    }

    public Duration getExpectedRunTime() {
        return expectedRunTime;
    }

    public Duration getSchedulingFrequency() {
        return schedulingFrequency;
    }

    public CronFunction getCronFunction() {
        return cronFunction;
    }

    public UUID getUuid() {
        return uuid;
    }
}
