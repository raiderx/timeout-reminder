package org.karpukhin.timeoutreminder;

/**
 * @author Pavel Karpukhin
 * @since 23.07.15
 */
public interface OptionsStorage {

    Options load();

    void save(Options options);
}
