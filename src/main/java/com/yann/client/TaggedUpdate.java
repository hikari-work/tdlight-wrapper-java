package com.yann.client;

import it.tdlight.jni.TdApi;

/**
 * An update paired with the account that produced it.
 *
 * <pre>{@code
 * manager.updates()
 *     .filter(t -> t.isType(TdApi.UpdateNewMessage.class))
 *     .subscribe(t -> {
 *         TdApi.UpdateNewMessage upd = t.cast(TdApi.UpdateNewMessage.class);
 *         System.out.println("[" + t.accountId() + "] " + upd.message.id);
 *     });
 * }</pre>
 */
public record TaggedUpdate(String accountId, TdApi.Update update) {

    /** Returns true when the wrapped update is an instance of {@code type}. */
    public <T extends TdApi.Update> boolean isType(Class<T> type) {
        return type.isInstance(update);
    }

    /**
     * Cast the wrapped update to {@code type}.
     *
     * @throws ClassCastException if the update is not of that type
     */
    public <T extends TdApi.Update> T cast(Class<T> type) {
        return type.cast(update);
    }
}
