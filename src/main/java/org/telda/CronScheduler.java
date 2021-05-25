package org.telda;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.PriorityBlockingQueue;

import static java.lang.String.format;

public class CronScheduler {
    private static final Logger logger = LoggerFactory.getLogger(CronScheduler.class);

    private final PriorityBlockingQueue<CronJob> priorityQueue;
    private final Set<UUID> registeredJobs;
    private final Thread cronJobConsumerThread;

    public CronScheduler(int noOfExecutorThreads) {
        this.priorityQueue = new PriorityBlockingQueue<>();
        this.registeredJobs = new HashSet<>();
        this.cronJobConsumerThread = new Thread(new CronJobConsumer(priorityQueue, noOfExecutorThreads));
        cronJobConsumerThread.start();
        logger.info(format("Cron Scheduler started. %d threads available to consume from the job queue", noOfExecutorThreads));
    }

    /**
     * creates a new cron job, registers it and push it onto the job queue
     *
     * @param expectedRuntime     A single run expected interval eg.30m
     * @param schedulingFrequency The frequency by which to run the job eg. 1hr for a job that should run every hour
     * @param cronFunction        The function/lamda expression to execute
     * @param uId                 A unique identifier for the cron job
     * @throws RuntimeException if the unique identifier matches that of an already registered job
     */
    public void registerCronJob(Duration expectedRuntime, Duration schedulingFrequency,
                                Runnable cronFunction, UUID uId) throws RuntimeException {
        if (registeredJobs.contains(uId)) {
            throw new RuntimeException(format("Cron Job with UID:[%s] is already registered with the scheduler", uId.toString()));
        }
        CronJob newCronJob = new CronJob(expectedRuntime, schedulingFrequency, cronFunction, uId);
        registeredJobs.add(uId);
        priorityQueue.add(newCronJob);
        logger.info(format("Job with unique ID:[%s], has been successfully registered with the scheduler",
                uId.toString()));
        //after adding it to the priority queue, we check if it jumped to the top of the queue
        if (priorityQueue.peek() == newCronJob) {
            logger.info(format("Waking up consumer thread as a new cron job, with ID [%s], just registered as top priority",
                    uId.toString()));
            cronJobConsumerThread.interrupt();
        }
    }

    PriorityBlockingQueue<CronJob> getPriorityQueue() {
        return priorityQueue;
    }

    Set<UUID> getRegisteredJobs() {
        return registeredJobs;
    }

    Thread getCronJobConsumerThread() {
        return cronJobConsumerThread;
    }
}
