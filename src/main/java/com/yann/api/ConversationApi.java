package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Pyromod-style conversation listener.
 *
 * <h2>listen – wait for the next message</h2>
 * <pre>{@code
 * // Any message in a chat
 * client.conversation().listen(chatId)
 *       .subscribe(msg -> ...);
 *
 * // Only from a specific user
 * client.conversation().listen(chatId, userId)
 *       .subscribe(msg -> ...);
 *
 * // With timeout – emits TimeoutException if no reply arrives in time
 * client.conversation().listen(chatId, userId, Duration.ofSeconds(30))
 *       .subscribe(msg -> ..., err -> ...);
 *
 * // With custom filter
 * client.conversation().listen(chatId, msg -> msg.content instanceof TdApi.MessageText)
 *       .subscribe(msg -> ...);
 * }</pre>
 *
 * <h2>ask – send a message, then wait for the reply</h2>
 * <pre>{@code
 * // Ask in a chat and wait for any reply
 * client.conversation().ask(chatId, "What is your name?")
 *       .subscribe(reply -> ...);
 *
 * // Ask, wait only for a reply from a specific user, with timeout
 * client.conversation().ask(chatId, "Pick a number", userId, Duration.ofSeconds(60))
 *       .subscribe(reply -> ..., err -> ...);
 * }</pre>
 *
 * <p>The listener subscribes to the hot update stream <em>before</em> the
 * outgoing message is sent, so no reply can be missed due to a race condition.
 *
 * <p>On timeout, Reactor signals a {@link java.util.concurrent.TimeoutException}.
 */
public final class ConversationApi extends TdlightOperations {

    private final Flux<TdApi.Update> updateSource;

    public ConversationApi(Supplier<SimpleTelegramClient> client, Flux<TdApi.Update> updateSource) {
        super(client);
        this.updateSource = updateSource;
    }

    // ------------------------------------------------------------------
    // listen – wait for the next matching message
    // ------------------------------------------------------------------

    /** Wait for the next message in {@code chatId} (from anyone). */
    public Mono<TdApi.Message> listen(long chatId) {
        return listenInternal(chatId, 0L, null, null);
    }

    /** Wait for the next message in {@code chatId} from {@code userId}. */
    public Mono<TdApi.Message> listen(long chatId, long userId) {
        return listenInternal(chatId, userId, null, null);
    }

    /** Wait for the next message in {@code chatId} matching {@code filter}. */
    public Mono<TdApi.Message> listen(long chatId, Predicate<TdApi.Message> filter) {
        return listenInternal(chatId, 0L, filter, null);
    }

    /** Wait for the next message in {@code chatId}, timing out after {@code timeout}. */
    public Mono<TdApi.Message> listen(long chatId, Duration timeout) {
        return listenInternal(chatId, 0L, null, timeout);
    }

    /** Wait for the next message from {@code userId} in {@code chatId}, with timeout. */
    public Mono<TdApi.Message> listen(long chatId, long userId, Duration timeout) {
        return listenInternal(chatId, userId, null, timeout);
    }

    /** Full variant: chat, user, predicate filter, and timeout. */
    public Mono<TdApi.Message> listen(long chatId, long userId,
                                       Predicate<TdApi.Message> filter, Duration timeout) {
        return listenInternal(chatId, userId, filter, timeout);
    }

    // ------------------------------------------------------------------
    // ask – send a message, then wait for the reply
    // ------------------------------------------------------------------

    /** Send {@code text} to {@code chatId} and wait for any reply. */
    public Mono<TdApi.Message> ask(long chatId, String text) {
        return askInternal(chatId, text, 0L, null, null);
    }

    /** Send {@code text} to {@code chatId} and wait for a reply from {@code userId}. */
    public Mono<TdApi.Message> ask(long chatId, String text, long userId) {
        return askInternal(chatId, text, userId, null, null);
    }

    /** Send {@code text} to {@code chatId}, wait for any reply, with timeout. */
    public Mono<TdApi.Message> ask(long chatId, String text, Duration timeout) {
        return askInternal(chatId, text, 0L, null, timeout);
    }

    /** Send {@code text} to {@code chatId}, wait for a reply from {@code userId}, with timeout. */
    public Mono<TdApi.Message> ask(long chatId, String text, long userId, Duration timeout) {
        return askInternal(chatId, text, userId, null, timeout);
    }

    /** Full ask variant: message text, target user, custom filter, and timeout. */
    public Mono<TdApi.Message> ask(long chatId, String text, long userId,
                                    Predicate<TdApi.Message> filter, Duration timeout) {
        return askInternal(chatId, text, userId, filter, timeout);
    }

    // ------------------------------------------------------------------
    // Internal implementation
    // ------------------------------------------------------------------

    private Mono<TdApi.Message> listenInternal(long chatId, long userId,
                                                Predicate<TdApi.Message> filter,
                                                Duration timeout) {
        Mono<TdApi.Message> mono = Mono.create(sink -> {
            Disposable sub = buildMessageFlux(chatId, userId, filter)
                    .take(1)
                    .subscribe(sink::success, sink::error);
            sink.onCancel(sub::dispose);
            sink.onDispose(sub::dispose);
        });
        return timeout != null ? mono.timeout(timeout) : mono;
    }

    private Mono<TdApi.Message> askInternal(long chatId, String text, long userId,
                                              Predicate<TdApi.Message> filter,
                                              Duration timeout) {
        return Mono.defer(() -> {
            // Subscribe to the hot update stream BEFORE sending so no reply is missed.
            Sinks.One<TdApi.Message> replySink = Sinks.one();
            Disposable sub = buildMessageFlux(chatId, userId, filter)
                    .take(1)
                    .subscribe(
                            msg -> replySink.tryEmitValue(msg),
                            e   -> replySink.tryEmitError(e)
                    );

            Mono<TdApi.Message> replyMono = replySink.asMono()
                    .doFinally(__ -> sub.dispose());
            if (timeout != null) {
                replyMono = replyMono.timeout(timeout);
            }

            return send(buildSendTextRequest(chatId, text)).then(replyMono);
        });
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private Flux<TdApi.Message> buildMessageFlux(long chatId, long userId,
                                                   Predicate<TdApi.Message> filter) {
        Flux<TdApi.Message> flux = updateSource
                .ofType(TdApi.UpdateNewMessage.class)
                .map(u -> u.message)
                .filter(msg -> msg.chatId == chatId);

        if (userId != 0L) {
            flux = flux.filter(msg -> isFromUser(msg, userId));
        }
        if (filter != null) {
            flux = flux.filter(filter);
        }
        return flux;
    }

    private static TdApi.SendMessage buildSendTextRequest(long chatId, String text) {
        TdApi.InputMessageText content = new TdApi.InputMessageText();
        content.text = new TdApi.FormattedText(text, new TdApi.TextEntity[0]);
        content.clearDraft = true;

        TdApi.MessageSendOptions opts = new TdApi.MessageSendOptions();
        opts.disableNotification = false;
        opts.fromBackground = false;
        opts.protectContent = false;

        TdApi.SendMessage req = new TdApi.SendMessage();
        req.chatId = chatId;
        req.options = opts;
        req.inputMessageContent = content;
        return req;
    }

    private static boolean isFromUser(TdApi.Message msg, long userId) {
        return msg.senderId instanceof TdApi.MessageSenderUser senderUser
                && senderUser.userId == userId;
    }
}
