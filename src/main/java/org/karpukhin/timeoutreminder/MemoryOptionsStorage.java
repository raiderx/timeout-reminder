package org.karpukhin.timeoutreminder;

/**
 * @author Pavel Karpukhin
 * @since 23.07.15
 */
public class MemoryOptionsStorage implements OptionsStorage {

    private final Options options;

    public MemoryOptionsStorage() {
        options = new Options();
        options.setWorkDuration(35);
        options.setBreakDuration(10);
        options.setStartAutomatically(true);
    }

    @Override
    public Options load() {
        Options result = new Options();
        result.setWorkDuration(options.getWorkDuration());
        result.setBreakDuration(options.getBreakDuration());
        result.setStartAutomatically(options.isStartAutomatically());
        return result;
    }

    @Override
    public void save(Options options) {
        this.options.setWorkDuration(options.getWorkDuration());
        this.options.setBreakDuration(options.getBreakDuration());
        this.options.setStartAutomatically(options.isStartAutomatically());
    }
}
