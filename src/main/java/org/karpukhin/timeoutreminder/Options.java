package org.karpukhin.timeoutreminder;

/**
 * @author Pavel Karpukhin
 * @since 23.07.15
 */
public class Options {

    private int workDuration;
    private int breakDuration;
    private boolean startAutomatically;
    private String message;

    public int getWorkDuration() {
        return workDuration;
    }

    public void setWorkDuration(int workDuration) {
        this.workDuration = workDuration;
    }

    public int getBreakDuration() {
        return breakDuration;
    }

    public void setBreakDuration(int breakDuration) {
        this.breakDuration = breakDuration;
    }

    public boolean isStartAutomatically() {
        return startAutomatically;
    }

    public void setStartAutomatically(boolean startAutomatically) {
        this.startAutomatically = startAutomatically;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
