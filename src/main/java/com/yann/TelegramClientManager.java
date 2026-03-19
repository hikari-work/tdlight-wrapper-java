package com.yann;

import com.yann.auth.UserAuthFlow;
import com.yann.client.TaggedUpdate;
import com.yann.config.TelegramClientConfig;
import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Manages multiple Telegram accounts (bots and/or user accounts) in one process.
 *
 * <h2>Quick start — multiple bots</h2>
 * <pre>{@code
 * TelegramClientManager manager = TelegramClientManager
 *     .builder(API_ID, API_HASH)
 *     .sessionBaseDir(Path.of("sessions"))
 *     .build();
 *
 * manager.startBot("alpha", "111:TOKEN_A").subscribe();
 * manager.startBot("beta",  "222:TOKEN_B").subscribe();
 * manager.startBot("gamma", "333:TOKEN_C").subscribe();
 *
 * // Single update stream tagged with the originating account
 * manager.updates()
 *     .filter(t -> t.isType(TdApi.UpdateNewMessage.class))
 *     .subscribe(t -> {
 *         TdApi.UpdateNewMessage u = t.cast(TdApi.UpdateNewMessage.class);
 *         System.out.println("[" + t.accountId() + "] msg " + u.message.id);
 *     });
 *
 * // Address one client directly
 * manager.get("alpha").messages().sendText(chatId, "Hello from alpha!");
 *
 * // Broadcast the same request to every active account
 * manager.broadcast(c -> c.messages().sendText(chatId, "Hi everyone!")).subscribe();
 *
 * manager.awaitAll().block(); // block until all accounts disconnect
 * }</pre>
 *
 * <h2>Mixed bots + user accounts</h2>
 * <pre>{@code
 * manager.startBot("myBot",   "TOKEN")
 *        .then(manager.startUser("alice", AuthenticationSupplier.user(...)))
 *        .subscribe();
 * }</pre>
 */
public final class TelegramClientManager implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TelegramClientManager.class);

    private final int apiId;
    private final String apiHash;
    private final Path sessionBaseDir;
    private final Consumer<TelegramClientConfig.Builder> configCustomizer;

    /** Live registry of accountId → client. */
    private final ConcurrentHashMap<String, ReactiveTelegramClient> clients =
            new ConcurrentHashMap<>();

    /**
     * Tracks outstanding client lifetimes so {@link #awaitAll()} can block
     * until every account has disconnected.
     */
    private final Sinks.Many<String> terminationSink =
            Sinks.many().multicast().onBackpressureBuffer(256, false);

    /** Merged update stream from every registered account. */
    private final Sinks.Many<TaggedUpdate> updateSink =
            Sinks.many().multicast().onBackpressureBuffer(8192, false);

    // ------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------

    private TelegramClientManager(Builder b) {
        this.apiId = b.apiId;
        this.apiHash = b.apiHash;
        this.sessionBaseDir = b.sessionBaseDir;
        this.configCustomizer = b.configCustomizer;
    }

    public static Builder builder(int apiId, String apiHash) {
        return new Builder(apiId, apiHash);
    }

    // ------------------------------------------------------------------
    // Adding accounts
    // ------------------------------------------------------------------

    /**
     * Register and start a bot account.
     * The returned {@link Mono} emits the client as soon as it is registered
     * (authentication runs in the background).
     *
     * @param accountId unique name/key for this account within the manager
     * @param botToken  BotFather token, e.g. {@code "123456:ABC..."}
     */
    public Mono<ReactiveTelegramClient> startBot(String accountId, String botToken) {
        return start(accountId, AuthenticationSupplier.bot(botToken));
    }

    /**
     * Register and start a user account.
     * Supply a phone-number or QR-code authentication supplier from
     * {@link AuthenticationSupplier}.
     */
    public Mono<ReactiveTelegramClient> startUser(String accountId,
                                                    SimpleAuthenticationSupplier<?> auth) {
        return start(accountId, auth);
    }

    /**
     * Register and start a user account using a {@link UserAuthFlow}.
     *
     * <p>The {@code UserAuthFlow} acts as the {@link it.tdlight.client.ClientInteraction}
     * — TDLight calls it when it needs the verification code or 2FA password.
     * Your application provides those values via
     * {@link UserAuthFlow#submitCode(String)} / {@link UserAuthFlow#submitPassword(String)}.
     *
     * <pre>{@code
     * UserAuthFlow auth = UserAuthFlow.create("+6281234567890");
     *
     * auth.state().subscribe(s -> System.out.println("Auth: " + s));
     *
     * manager.startUser("alice", auth).subscribe();
     *
     * // When code arrives:
     * auth.submitCode("12345");
     * }</pre>
     */
    public Mono<ReactiveTelegramClient> startUser(String accountId, UserAuthFlow authFlow) {
        return Mono.defer(() -> {
            if (clients.containsKey(accountId)) {
                return Mono.error(new IllegalArgumentException(
                        "Account '" + accountId + "' is already registered"));
            }

            return Mono.fromCallable(() -> buildClient(accountId, authFlow))
                    .doOnNext(client -> {
                        client.updates().all()
                                .doOnNext(upd -> updateSink.tryEmitNext(new TaggedUpdate(accountId, upd)))
                                .subscribe(
                                        __ -> {},
                                        err -> log.warn("[{}] Update stream error", accountId, err)
                                );

                        // Auto-signal ready when TDLight reaches AuthorizationStateReady
                        client.updates().authorizationState()
                                .filter(u -> u.authorizationState instanceof TdApi.AuthorizationStateReady)
                                .next()
                                .subscribe(__ -> authFlow.signalReady());

                        client.start(AuthenticationSupplier.user(authFlow.phoneNumber()))
                                .doOnError(err -> log.error("[{}] Client stopped with error", accountId, err))
                                .doOnTerminate(() -> {
                                    clients.remove(accountId);
                                    terminationSink.tryEmitNext(accountId);
                                    log.info("[{}] Removed from manager", accountId);
                                })
                                .subscribe();
                    });
        });
    }

    /**
     * Register and start an account with a fully custom auth supplier.
     * The {@link Mono} completes once the client is registered; the actual
     * TDLight lifecycle runs on a background subscription.
     */
    public Mono<ReactiveTelegramClient> start(String accountId,
                                               SimpleAuthenticationSupplier<?> auth) {
        if (clients.containsKey(accountId)) {
            return Mono.error(new IllegalArgumentException(
                    "Account '" + accountId + "' is already registered"));
        }

        return Mono.fromCallable(() -> buildClient(accountId))
                .doOnNext(client -> {
                    // Forward all updates to the shared sink, tagged with this accountId
                    client.updates().all()
                            .doOnNext(upd -> updateSink.tryEmitNext(new TaggedUpdate(accountId, upd)))
                            .subscribe(
                                    __ -> { /* handled above */ },
                                    err -> log.warn("[{}] Update stream error", accountId, err)
                            );

                    // Run the TDLight lifecycle in the background.
                    // When it terminates (disconnect / error), remove from registry
                    // and signal the termination sink so awaitAll() can track completion.
                    client.start(auth)
                            .doOnError(err -> log.error("[{}] Client stopped with error", accountId, err))
                            .doOnTerminate(() -> {
                                clients.remove(accountId);
                                terminationSink.tryEmitNext(accountId);
                                log.info("[{}] Removed from manager", accountId);
                            })
                            .subscribe();
                });
    }

    // ------------------------------------------------------------------
    // Accessing clients
    // ------------------------------------------------------------------

    /**
     * Get a client by its account id.
     *
     * @throws IllegalArgumentException if the account is not registered
     */
    public ReactiveTelegramClient get(String accountId) {
        ReactiveTelegramClient c = clients.get(accountId);
        if (c == null) {
            throw new IllegalArgumentException("No account registered under '" + accountId + "'");
        }
        return c;
    }

    /** Returns {@code true} if an account with this id is currently active. */
    public boolean has(String accountId) {
        return clients.containsKey(accountId);
    }

    /** Unmodifiable snapshot of all registered account ids. */
    public Collection<String> accountIds() {
        return Collections.unmodifiableSet(clients.keySet());
    }

    /** Unmodifiable snapshot of all active clients. */
    public Map<String, ReactiveTelegramClient> all() {
        return Collections.unmodifiableMap(clients);
    }

    // ------------------------------------------------------------------
    // Combined update stream
    // ------------------------------------------------------------------

    /**
     * Hot {@link Flux} of every update from every account, tagged with the
     * originating account id.
     */
    public Flux<TaggedUpdate> updates() {
        return updateSink.asFlux();
    }

    /**
     * Convenience: stream only updates of a specific TDLib type, still tagged.
     *
     * <pre>{@code
     * manager.updatesOfType(TdApi.UpdateNewMessage.class)
     *        .subscribe(t -> System.out.println(t.accountId() + " → " + t.update().message.id));
     * }</pre>
     */
    public <T extends TdApi.Update> Flux<TaggedUpdate> updatesOfType(Class<T> type) {
        return updateSink.asFlux().filter(t -> t.isType(type));
    }

    // ------------------------------------------------------------------
    // Multi-account operations
    // ------------------------------------------------------------------

    /**
     * Execute {@code operation} on every registered client and merge the results.
     *
     * <pre>{@code
     * // Send a message with every bot
     * manager.broadcast(c -> c.messages().sendText(chatId, "Hello!"))
     *        .subscribe(msg -> System.out.println("Sent " + msg.id));
     * }</pre>
     */
    public <T extends TdApi.Object> Flux<T> broadcast(
            Function<ReactiveTelegramClient, Mono<T>> operation) {
        return Flux.fromIterable(clients.values())
                .flatMap(operation);
    }

    /**
     * Execute {@code operation} on a specific set of account ids.
     */
    public <T extends TdApi.Object> Flux<T> broadcastTo(
            Iterable<String> accountIds,
            Function<ReactiveTelegramClient, Mono<T>> operation) {
        return Flux.fromIterable(accountIds)
                .map(this::get)
                .flatMap(operation);
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    /**
     * Gracefully disconnect and remove one account.
     */
    public Mono<Void> stop(String accountId) {
        return Mono.fromRunnable(() -> {
            ReactiveTelegramClient c = clients.remove(accountId);
            if (c != null) {
                c.close();
                log.info("[{}] Stopped", accountId);
            }
        });
    }

    /**
     * Gracefully disconnect all accounts.
     */
    public Mono<Void> stopAll() {
        return Flux.fromIterable(clients.keySet().stream().toList())
                .flatMap(this::stop)
                .then();
    }

    /**
     * Returns a {@link Mono} that completes only when every currently-registered
     * client has disconnected. Useful as a keep-alive at the end of {@code main()}.
     *
     * <pre>{@code
     * manager.awaitAll().block();
     * }</pre>
     */
    public Mono<Void> awaitAll() {
        return Mono.defer(() -> {
            int expected = clients.size();
            if (expected == 0) return Mono.empty();

            // Count termination events until all *currently registered* accounts disconnect.
            // Accounts added after this call are not counted — use awaitAll() again for those.
            return terminationSink.asFlux()
                    .take(expected)
                    .then();
        });
    }

    @Override
    public void close() {
        clients.values().forEach(c -> {
            try {
                c.close();
            } catch (Exception ex) {
                log.warn("Error closing client", ex);
            }
        });
        clients.clear();
        updateSink.tryEmitComplete();
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private ReactiveTelegramClient buildClient(String accountId) {
        return buildClient(accountId, null);
    }

    private ReactiveTelegramClient buildClient(String accountId, UserAuthFlow authFlow) {
        TelegramClientConfig.Builder b = TelegramClientConfig
                .builder(apiId, apiHash)
                .databasePath(sessionBaseDir.resolve(accountId).resolve("data"))
                .downloadPath(sessionBaseDir.resolve(accountId).resolve("downloads"));

        if (configCustomizer != null) {
            configCustomizer.accept(b);
        }

        ReactiveTelegramClient client = ReactiveTelegramClient.create(b.build());
        if (authFlow != null) {
            client.setClientInteraction(authFlow);
        }
        clients.put(accountId, client);
        log.info("[{}] Client created", accountId);
        return client;
    }

    // ------------------------------------------------------------------
    // Builder
    // ------------------------------------------------------------------

    public static final class Builder {

        private final int apiId;
        private final String apiHash;
        private Path sessionBaseDir = Path.of("sessions");
        private Consumer<TelegramClientConfig.Builder> configCustomizer;

        private Builder(int apiId, String apiHash) {
            this.apiId = apiId;
            this.apiHash = apiHash;
        }

        /** Base directory under which each account gets its own subdirectory. */
        public Builder sessionBaseDir(Path path) {
            this.sessionBaseDir = path;
            return this;
        }

        /**
         * Apply extra settings to every client config before creation.
         * Useful for setting proxies, memory options, language code, etc.
         *
         * <pre>{@code
         * .configCustomizer(b -> b
         *     .socks5Proxy("127.0.0.1", 1080, "user", "pass")
         *     .systemLanguageCode("en"))
         * }</pre>
         */
        public Builder configCustomizer(Consumer<TelegramClientConfig.Builder> customizer) {
            this.configCustomizer = customizer;
            return this;
        }

        public TelegramClientManager build() {
            return new TelegramClientManager(this);
        }
    }
}
