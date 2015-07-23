package org.karpukhin.timeoutreminder;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author Pavel Karpukhin
 * @since 23.07.15
 */
public class Reminder {

    private final ScheduledExecutorService executor;
    private Runnable command;
    private ScheduledFuture future;
    private long elapsed;

    public Reminder(ScheduledExecutorService executor) {
        this.executor = executor;
    }

    public void start(int duration, Runnable command) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Parameter 'duration' has to be greater than 0");
        }
        if (command == null) {
            throw new IllegalArgumentException("Parameter 'command' can't be null");
        }
        if (future != null && !future.isDone()) {
            throw new IllegalStateException("Reminder is already started");
        }
        future = executor.schedule(command, duration, TimeUnit.MINUTES);
        this.command = command;
        this.elapsed = 0;
    }

    public void pause() {
        if (future == null) {
            throw new IllegalStateException("Reminder was not started");
        }
        elapsed = future.getDelay(TimeUnit.SECONDS);
        future.cancel(true);
        future = null;
    }

    public void resume() {
        if (elapsed <= 0) {
            throw new IllegalStateException("Reminder was not started");
        }
        future = executor.schedule(command, elapsed, TimeUnit.SECONDS);
        elapsed = 0;
    }

    public State getState() {
        if (elapsed > 0) {
            return State.Paused;
        }
        if (future != null) {
            return State.Running;
        }
        return State.NotStarted;
    }

    public long getElapsed() {
        if (elapsed > 0) {
            return elapsed;
        }
        if (future != null) {
            return future.getDelay(TimeUnit.SECONDS);
        }
        return 0;
    }
}
