package com.yann.auth;

import it.tdlight.client.ClientInteraction;
import it.tdlight.client.InputParameter;
import it.tdlight.client.ParameterInfo;
import it.tdlight.client.ParameterInfoCode;
import it.tdlight.client.ParameterInfoPasswordHint;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.CompletableFuture;

/**
 * Reactive-friendly handler for Telegram user account authentication.
 *
 * <p>Implements {@link ClientInteraction} — the interface TDLight calls when it needs
 * user input (auth code, 2FA password, etc.) during the login flow.
 * Internally, each request is backed by a {@link CompletableFuture} that blocks the
 * TDLight thread until your application calls {@link #submitCode} /
 * {@link #submitPassword}.
 *
 * <h2>Usage with TelegramClientManager</h2>
 * <pre>{@code
 * UserAuthFlow auth = UserAuthFlow.create("+6281234567890");
 *
 * // 1. Observe state before starting
 * auth.state().subscribe(s -> System.out.println("Auth: " + s));
 *
 * // 2. Start the user account
 * manager.startUser("alice", auth).subscribe();
 *
 * // 3. After state emits WAITING_CODE, provide the code:
 * auth.submitCode("12345");
 *
 * // 4. If 2FA enabled, after WAITING_PASSWORD:
 * auth.submitPassword("my-secret");
 * }</pre>
 */
public final class UserAuthFlow implements ClientInteraction {

    /** Steps in the Telegram user authentication flow. */
    public enum State {
        /** Initial state; phone number is being sent. */
        WAITING_PHONE,
        /** Code sent to Telegram app / SMS; waiting for {@link #submitCode(String)}. */
        WAITING_CODE,
        /** 2FA required; waiting for {@link #submitPassword(String)}. */
        WAITING_PASSWORD,
        /** Authentication completed successfully. */
        READY,
        /** Flow was cancelled or encountered an error. */
        CANCELLED
    }

    private final String phoneNumber;

    // One CompletableFuture per interactive step.
    // They block the TDLight internal auth thread until we complete them externally.
    private final CompletableFuture<String> codeFuture     = new CompletableFuture<>();
    private final CompletableFuture<String> passwordFuture = new CompletableFuture<>();

    // Hot multicast sink for state transitions
    private final Sinks.Many<State> stateSink =
            Sinks.many().multicast().onBackpressureBuffer(16, false);

    private UserAuthFlow(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        stateSink.tryEmitNext(State.WAITING_PHONE);
    }

    // ------------------------------------------------------------------
    // Factory
    // ------------------------------------------------------------------

    /**
     * Create an auth flow for the given phone number.
     *
     * @param phoneNumber international format, e.g. {@code "+6281234567890"}
     */
    public static UserAuthFlow create(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }
        return new UserAuthFlow(phoneNumber);
    }

    // ------------------------------------------------------------------
    // Input (called by your application)
    // ------------------------------------------------------------------

    /**
     * Provide the verification code received via Telegram app or SMS.
     * Call this after {@link State#WAITING_CODE} is emitted.
     */
    public void submitCode(String code) {
        codeFuture.complete(code);
    }

    /**
     * Provide the Two-Step Verification (2FA) password.
     * Only needed if the account has 2FA enabled.
     * Call this after {@link State#WAITING_PASSWORD} is emitted.
     */
    public void submitPassword(String password) {
        passwordFuture.complete(password);
    }

    /** Cancel the auth flow (e.g. on timeout). */
    public void cancel() {
        codeFuture.cancel(true);
        passwordFuture.cancel(true);
        stateSink.tryEmitNext(State.CANCELLED);
        stateSink.tryEmitComplete();
    }

    // ------------------------------------------------------------------
    // Observing state
    // ------------------------------------------------------------------

    /**
     * Hot {@link Flux} emitting each {@link State} transition.
     * Subscribe <em>before</em> passing this flow to
     * {@link com.yann.TelegramClientManager#startUser(String, UserAuthFlow)}.
     */
    public Flux<State> state() {
        return stateSink.asFlux();
    }

    /**
     * {@link Mono} that completes on {@link State#READY} or errors on
     * {@link State#CANCELLED}.
     */
    public Mono<Void> awaitReady() {
        return state()
                .filter(s -> s == State.READY || s == State.CANCELLED)
                .next()
                .flatMap(s -> s == State.READY
                        ? Mono.empty()
                        : Mono.error(new IllegalStateException("Auth was cancelled")));
    }

    // ------------------------------------------------------------------
    // ClientInteraction — called by TDLight internally
    // ------------------------------------------------------------------

    /**
     * Called by TDLight's {@code SimpleTelegramClient} at each interactive step.
     * Returns a {@link CompletableFuture} that completes when the user supplies
     * the requested value via {@link #submitCode} / {@link #submitPassword}.
     */
    @Override
    public CompletableFuture<String> onParameterRequest(InputParameter parameter,
                                                          ParameterInfo parameterInfo) {
        return switch (parameter) {
            case ASK_CODE -> {
                stateSink.tryEmitNext(State.WAITING_CODE);
                yield codeFuture;
            }
            case ASK_PASSWORD -> {
                stateSink.tryEmitNext(State.WAITING_PASSWORD);
                yield passwordFuture;
            }
            // For first-time registration (new accounts): default to empty strings
            case ASK_FIRST_NAME -> CompletableFuture.completedFuture("");
            case ASK_LAST_NAME  -> CompletableFuture.completedFuture("");
            // Notification-only events: return empty string
            default -> CompletableFuture.completedFuture("");
        };
    }

    // ------------------------------------------------------------------
    // Internal helpers used by TelegramClientManager
    // ------------------------------------------------------------------

    /** The phone number this flow handles. */
    public String phoneNumber() {
        return phoneNumber;
    }

    /**
     * Called by {@link com.yann.TelegramClientManager} after it detects
     * {@link it.tdlight.jni.TdApi.AuthorizationStateReady} for this account.
     */
    public void signalReady() {
        stateSink.tryEmitNext(State.READY);
        stateSink.tryEmitComplete();
    }
}
