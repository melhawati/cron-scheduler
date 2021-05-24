package org.telda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.*;

import static java.lang.String.format;

class CronJobConsumer implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(CronJobConsumer.class);

    private final PriorityBlockingQueue<CronJob> priorityBlockingQueue;
    private final ExecutorService executorService;

    public CronJobConsumer(PriorityBlockingQueue<CronJob> priorityBlockingQueue, int noOfThreads) {
        this.priorityBlockingQueue = priorityBlockingQueue;
        this.executorService = Executors.newFixedThreadPool(noOfThreads);
    }

    /**
     * Main consumer thread which monitors the top of the priority queue.
     * If the top-priority job's next execution time is reached, submit it for asynchronous execution
     * else, sleep until its next execution time is reached.
     */
    @Override
    public void run() {
        while (true) {
            try {
                CronJob job = priorityBlockingQueue.take();
                if (job.getNextExecutionTime().isAfter(Instant.now())) {
                    priorityBlockingQueue.put(job);
                    goToSleep(job);
                } else {
                    logger.info(format("Executing cron job at %s with unique ID %s", Instant.now(), job.uuid.toString()));
                    executeReadyJob(job);
                }
            } catch (InterruptedException exception) {
                logger.info("Consumer thread up to fetch the new top priority job");
            }
        }
    }

    /**
     * Wraps the cron function into a CompletableFuture with timeout set to job.expectedRuntime,
     * and callback logging for onSuccess and OnFailure.
     * Resets job.nextExecutionTime and pushes the job back onto the queue
     *
     * @param job the cron job which is ready for execution
     */
    void executeReadyJob(CronJob job) {
        CompletableFuture.runAsync(job.cronFunction, executorService)
                .orTimeout(job.expectedRunTime.toSeconds(), TimeUnit.SECONDS)
                .whenComplete((res, ex) -> {
                    if (ex != null) {
                        logger.error(format("Run %d for cron job with ID [%s] killed with exception: [%s]",
                                job.incrementFailedRuns(), job.uuid.toString(), ex));
                    } else {
                        logger.info(format("Run [%d] for cron job with ID [%s] was successful in [%d] milliseconds",
                                job.incrementSuccessfulRuns(), job.uuid.toString(), job.cronFunction.getLastExecutionTime()));
                    }
                });
        job.setNextExecutionTime(Instant.now());
        priorityBlockingQueue.put(job);
    }

    /**
     * called when job.nextExecutionTime is not reached to put the main thread back to sleep
     * and avoid continuous polling of the queue
     *
     * @param job the cron job which was retrieved from the top of the queue
     * @throws InterruptedException when thread is interrupted
     */
    void goToSleep(CronJob job) throws InterruptedException {
        Thread.sleep(job.getNextExecutionTime().getEpochSecond() - Instant.now().getEpochSecond());
    }
}
