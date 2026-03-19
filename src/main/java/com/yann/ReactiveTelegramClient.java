package com.yann;

import com.yann.api.*;
import com.yann.config.TelegramClientConfig;
import com.yann.exception.TelegramException;
import it.tdlight.Init;
import it.tdlight.Log;
import it.tdlight.Slf4JLogMessageHandler;
import it.tdlight.client.AuthenticationSupplier;
import it.tdlight.client.SimpleAuthenticationSupplier;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.client.ClientInteraction;
import it.tdlight.client.SimpleTelegramClientBuilder;
import it.tdlight.client.SimpleTelegramClientFactory;
import it.tdlight.jni.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

/**
 * Reactive façade over {@link SimpleTelegramClient}.
 *
 * <h2>Lifecycle</h2>
 * <pre>{@code
 * ReactiveTelegramClient client = ReactiveTelegramClient.create(config);
 * client.start(AuthenticationSupplier.bot("TOKEN"))
 *       .subscribe(); // non-blocking; completes when the client disconnects
 * }</pre>
 *
 * <h2>Sending a message</h2>
 * <pre>{@code
 * client.messages().sendText(chatId, "Hello!")
 *       .subscribe(msg -> System.out.println("Sent: " + msg.id));
 * }</pre>
 *
 * <h2>Listening to updates</h2>
 * <pre>{@code
 * client.updates().newMessages()
 *       .subscribe(upd -> System.out.println(upd.message.id));
 * }</pre>
 */
public final class ReactiveTelegramClient implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(ReactiveTelegramClient.class);

    private final TelegramClientConfig config;

    /** Set in {@link #start(SimpleAuthenticationSupplier)}; null before that. */
    private volatile SimpleTelegramClient underlying;

    /** Kept alive for the full client lifetime; closed when the client disconnects. */
    private volatile SimpleTelegramClientFactory clientFactory;

    /** Optional interaction handler for interactive auth (user accounts). */
    private volatile ClientInteraction clientInteraction;

    // Hot multicast sink – all updates flow through here
    private final Sinks.Many<TdApi.Update> updateSink =
        Sinks.many().multicast().onBackpressureBuffer(1024, false);

    // API modules
    private final MessagesApi      messages;
    private final ChatsApi         chats;
    private final UsersApi         users;
    private final FilesApi         files;
    private final StickersApi      stickers;
    private final ContactsApi      contacts;
    private final SupergroupApi    supergroups;
    private final BotApi           bot;
    private final CallsApi         calls;
    private final PaymentsApi      payments;
    private final UpdatesApi       updates;
    private final ConversationApi  conversation;

    // ------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------

    private ReactiveTelegramClient(TelegramClientConfig config) {
        this.config = config;

        Flux<TdApi.Update> updateFlux = updateSink.asFlux();

        // Supplier that lazily resolves to the underlying client
        Supplier<SimpleTelegramClient> clientRef = () -> underlying;
        this.messages    = new MessagesApi(clientRef);
        this.chats       = new ChatsApi(clientRef);
        this.users       = new UsersApi(clientRef);
        this.files       = new FilesApi(clientRef);
        this.stickers    = new StickersApi(clientRef);
        this.contacts    = new ContactsApi(clientRef);
        this.supergroups = new SupergroupApi(clientRef);
        this.bot         = new BotApi(clientRef);
        this.calls        = new CallsApi(clientRef);
        this.payments     = new PaymentsApi(clientRef);
        this.updates      = new UpdatesApi(updateFlux);
        this.conversation = new ConversationApi(clientRef, updateFlux);
    }

    /**
     * Create a new client from a {@link TelegramClientConfig}.
     * Call {@link #start(SimpleAuthenticationSupplier)} afterwards.
     */
    public static ReactiveTelegramClient create(TelegramClientConfig config) {
        return new ReactiveTelegramClient(config);
    }

    /**
     * Initialize TDLight native libraries. Must be called <em>once</em> per JVM
     * before creating any client. Safe to call multiple times.
     */
    public static void initNatives() throws Exception {
        Init.init();
        Log.setLogMessageHandler(1, new Slf4JLogMessageHandler());
    }

    // ------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------

    /**
     * Start the client with the given authentication supplier.
     * Returns a {@link Mono} that completes when the client disconnects.
     */
    public Mono<Void> start(SimpleAuthenticationSupplier<?> auth) {
        return Mono.defer(() -> {
            // Factory must remain open for the entire client lifetime.
            // Closing it prematurely shuts down the underlying thread pool.
            SimpleTelegramClientFactory factory = new SimpleTelegramClientFactory();
            this.clientFactory = factory;
            SimpleTelegramClientBuilder builder = factory.builder(config.toTDLibSettings());
            builder.addUpdatesHandler(updateSink::tryEmitNext);
            if (clientInteraction != null) {
                builder.setClientInteraction(clientInteraction);
            }
            this.underlying = builder.build(auth);
            return Mono.fromFuture(underlying.waitForExitAsync())
                .doOnTerminate(() -> {
                    updateSink.tryEmitComplete();
                    try { factory.close(); } catch (Exception e) { log.warn("Error closing factory", e); }
                });
        });
    }

    /**
     * Set a {@link ClientInteraction} to handle interactive auth requests
     * (code, 2FA password, etc.). Must be called <em>before</em> {@link #start}.
     * Used internally by {@link com.yann.TelegramClientManager#startUser(String, com.yann.auth.UserAuthFlow)}.
     */
    public void setClientInteraction(ClientInteraction interaction) {
        this.clientInteraction = interaction;
    }

    /** Convenience: start as a bot with the given token. */
    public Mono<Void> startAsBot(String botToken) {
        return start(AuthenticationSupplier.bot(botToken));
    }

    /**
     * Send <em>any</em> TDLib function and get a reactive result.
     * Useful for functions not covered by the typed API modules.
     */
    public <T extends TdApi.Object> Mono<T> send(TdApi.Function<T> function) {
        return Mono.create(sink -> {
            SimpleTelegramClient client = underlying;
            if (client == null) {
                sink.error(new IllegalStateException("Client not started yet"));
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

    /**
     * Set a TDLight / TDLib option value.
     * Consult {@code TdApi.SetOption} and {@code TdApi.OptionValue} for available options.
     */
    public Mono<TdApi.Ok> setOption(String name, TdApi.OptionValue value) {
        TdApi.SetOption req = new TdApi.SetOption();
        req.name = name;
        req.value = value;
        return send(req);
    }

    /**
     * Enable TDLight-specific memory optimizations.
     * See README for a full list of supported option names.
     */
    public Mono<TdApi.Ok> disableMiniThumbnails() {
        return setOption("disable_minithumbnails", new TdApi.OptionValueBoolean(true));
    }

    public Mono<TdApi.Ok> disableDocumentFilenames() {
        return setOption("disable_document_filenames", new TdApi.OptionValueBoolean(true));
    }

    public Mono<TdApi.Ok> disableNotifications() {
        return setOption("disable_notifications", new TdApi.OptionValueBoolean(true));
    }

    public Mono<TdApi.Ok> ignoreUpdateChatLastMessage() {
        return setOption("ignore_update_chat_last_message", new TdApi.OptionValueBoolean(true));
    }

    public Mono<TdApi.Ok> ignoreUpdateChatReadInbox() {
        return setOption("ignore_update_chat_read_inbox", new TdApi.OptionValueBoolean(true));
    }

    public Mono<TdApi.Ok> receiveAccessHashes(boolean enable) {
        return setOption("receive_access_hashes", new TdApi.OptionValueBoolean(enable));
    }

    /** Get TDLight memory statistics. */
    public Mono<TdApi.MemoryStatistics> getMemoryStatistics() {
        return send(new TdApi.GetMemoryStatistics());
    }

    // ------------------------------------------------------------------
    // API accessors
    // ------------------------------------------------------------------

    public MessagesApi messages() {
        return messages;
    }

    public ChatsApi chats() {
        return chats;
    }

    public UsersApi users() {
        return users;
    }

    public FilesApi files() {
        return files;
    }

    public StickersApi stickers() {
        return stickers;
    }

    public ContactsApi contacts() {
        return contacts;
    }

    public SupergroupApi supergroups() {
        return supergroups;
    }

    public BotApi bot() {
        return bot;
    }

    public CallsApi calls() {
        return calls;
    }

    public PaymentsApi payments() {
        return payments;
    }

    public UpdatesApi updates() {
        return updates;
    }

    public ConversationApi conversation() {
        return conversation;
    }

    /** Direct access to the underlying {@link SimpleTelegramClient} for advanced use. */
    public SimpleTelegramClient underlying() {
        return underlying;
    }

    // ------------------------------------------------------------------
    // AutoCloseable
    // ------------------------------------------------------------------

    @Override
    public void close() {
        SimpleTelegramClient client = underlying;
        if (client != null) {
            try {
                client.close();
            } catch (Exception e) {
                log.warn("Error closing TDLight client", e);
            }
        }
        SimpleTelegramClientFactory factory = clientFactory;
        if (factory != null) {
            try {
                factory.close();
            } catch (Exception e) {
                log.warn("Error closing TDLight factory", e);
            }
        }
    }
}
