package com.yann;

import com.yann.client.TaggedUpdate;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

import java.nio.file.Path;

/**
 * Example: managing multiple bots (and/or user accounts) in a single process.
 */
public class App {

    // ── Replace with your real credentials ─────────────────────────────────
    private static final int    API_ID   = 12345;
    private static final String API_HASH = "your_api_hash_here";

    private static final String BOT_TOKEN_1 = "111111111:AAABBBCCC-TOKEN1";
    private static final String BOT_TOKEN_2 = "222222222:AAABBBCCC-TOKEN2";
    private static final String BOT_TOKEN_3 = "333333333:AAABBBCCC-TOKEN3";

    private static final long BROADCAST_CHAT_ID = -1001234567890L;
    // ────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) throws Exception {

        // 1. Init natives once per JVM
        ReactiveTelegramClient.initNatives();

        // 2. Build the manager (all accounts share API credentials + base path)
        TelegramClientManager manager = TelegramClientManager
                .builder(API_ID, API_HASH)
                .sessionBaseDir(Path.of("sessions"))    // sessions/bot1/data, sessions/bot2/data, …
                .configCustomizer(b -> b                // extra settings applied to every account
                        .applicationVersion("1.0")
                        .systemLanguageCode("en"))
                .build();

        // 3. Subscribe to the COMBINED update stream before starting any client
        //    so no updates are missed.
        manager.updates()
                .filter(t -> t.isType(TdApi.UpdateNewMessage.class))
                .subscribe(t -> {
                    TdApi.UpdateNewMessage upd = t.cast(TdApi.UpdateNewMessage.class);
                    TdApi.Message msg = upd.message;

                    System.out.printf("[%s] New message id=%d in chat=%d%n",
                            t.accountId(), msg.id, msg.chatId);

                    // Echo only messages sent to THIS specific bot
                    if (!msg.isOutgoing && msg.content instanceof TdApi.MessageText txt) {
                        manager.get(t.accountId())
                                .messages()
                                .sendText(msg.chatId, "[" + t.accountId() + "] Echo: " + txt.text.text)
                                .subscribe(
                                        sent -> System.out.println("  echoed: " + sent.id),
                                        err  -> System.err.println("  send error: " + err.getMessage())
                                );
                    }
                });

        // Monitor auth state per-account
        manager.updatesOfType(TdApi.UpdateAuthorizationState.class)
                .subscribe(t -> {
                    TdApi.UpdateAuthorizationState upd = t.cast(TdApi.UpdateAuthorizationState.class);
                    System.out.printf("[%s] Auth: %s%n",
                            t.accountId(), upd.authorizationState.getClass().getSimpleName());
                });

        // 4. Start accounts — each runs its lifecycle in the background
        Mono.when(
                manager.startBot("bot1", BOT_TOKEN_1),
                manager.startBot("bot2", BOT_TOKEN_2),
                manager.startBot("bot3", BOT_TOKEN_3)
                // To add a user account:
                // manager.startUser("alice", AuthenticationSupplier.user(phoneSupplier, codeSupplier, ...))
        ).block(); // waits until all three are REGISTERED (not until they disconnect)

        System.out.println("All accounts started. Active: " + manager.accountIds());

        // 5. Send directly to one specific account
        manager.get("bot1")
                .messages()
                .sendText(BROADCAST_CHAT_ID, "Hello from bot1 only!")
                .subscribe(msg -> System.out.println("bot1 sent: " + msg.id));

        // 6. Broadcast the same message with every bot
        manager.broadcast(client -> client.messages().sendText(BROADCAST_CHAT_ID, "Broadcast!"))
                .subscribe(msg -> System.out.println("Broadcast sent: " + msg.id));

        // 7. Target a specific subset
        manager.broadcastTo(
                java.util.List.of("bot1", "bot2"),
                client -> client.messages().sendText(BROADCAST_CHAT_ID, "Subset message")
        ).subscribe();

        // 8. Block main thread until ALL accounts disconnect
        manager.awaitAll().block();

        manager.close();
    }
}
