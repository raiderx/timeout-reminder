package org.karpukhin.timeoutreminder;

/**
 * @author Pavel Karpukhin
 * @since 23.07.15
 */
public interface MessageSource {

    String getMessage(String key);
}