package org.telda;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * a helper test class which represents a runnable that records its runtime up to n runs.
 */
final class RecordedCronJob implements Runnable {
    private final int maxSuccessfulRuns;
    int numRunTimesCounter = 0;
    final List<Instant> runTimes = new ArrayList<>();

    RecordedCronJob(int maxSuccessfulRuns) {
        this.maxSuccessfulRuns = maxSuccessfulRuns;
    }

    @Override
    public void run() {
        //to mimic running for only n times consecutively, dont care about remaining runs
        if (numRunTimesCounter < maxSuccessfulRuns) {
            numRunTimesCounter++;
            runTimes.add(Instant.now());
        }
    }

    public List<Duration> getTimeIntervals() {
        List<Duration> timeIntervals = new ArrayList<>();
        IntStream.range(0, runTimes.size() - 1).forEach(i -> {
            Duration interval = Duration.between(runTimes.get(i), runTimes.get(i + 1));
            timeIntervals.add(interval);
        });
        return timeIntervals;
    }
}
