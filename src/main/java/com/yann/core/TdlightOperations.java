package com.yann.core;

import com.yann.exception.TelegramException;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Base class shared by all API modules.
 * Provides a Reactor-aware {@link #send(TdApi.Function)} helper that converts
 * the callback-based TDLight response into a {@link Mono}.
 */
public abstract class TdlightOperations {

    private final Supplier<SimpleTelegramClient> clientSupplier;

    protected TdlightOperations(Supplier<SimpleTelegramClient> clientSupplier) {
        this.clientSupplier = clientSupplier;
    }

    /**
     * Sends any TDLib function and returns a cold {@link Mono} that emits
     * the result or signals a {@link TelegramException} on failure.
     */
    protected <T extends TdApi.Object> Mono<T> send(TdApi.Function<T> function) {
        return Mono.create(sink -> {
            SimpleTelegramClient client = clientSupplier.get();
            if (client == null) {
                sink.error(new IllegalStateException("TDLight client not started yet"));
                return;
            }
            client.send(function, result -> {
                if (result.isError()) {
                    sink.error(new TelegramException(result.getError()));
                } else {
                    sink.success(result.get());
                }
            });
        });
    }
}
