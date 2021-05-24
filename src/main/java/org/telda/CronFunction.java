package org.telda;

class CronFunction implements Runnable {
    private long lastExecutionTime;
    private final Runnable function;

    public CronFunction(Runnable function) {
        this.function = function;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        function.run();
        setLastExecutionTime(startTime);
    }

    public void setLastExecutionTime(long startTime) {
        this.lastExecutionTime = System.currentTimeMillis() - startTime;
    }

    public long getLastExecutionTime() {
        return lastExecutionTime;
    }
}
