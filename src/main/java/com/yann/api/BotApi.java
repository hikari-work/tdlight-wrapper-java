package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import java.util.function.Supplier;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for bot-specific TDLib functions:
 * callback queries, inline queries, commands, games, payments, and web apps.
 */
public final class BotApi extends TdlightOperations {

    public BotApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Callback queries
    // ------------------------------------------------------------------

    /**
     * Answer a callback query (from inline keyboard button press).
     *
     * @param callbackQueryId id from {@link TdApi.UpdateNewCallbackQuery}
     * @param text            notification text shown to the user
     * @param showAlert       show as alert box instead of toast
     * @param url             optional deep-link url
     * @param cacheTime       seconds to cache the result client-side
     */
    public Mono<TdApi.Ok> answerCallbackQuery(long callbackQueryId, String text,
                                               boolean showAlert, String url, int cacheTime) {
        TdApi.AnswerCallbackQuery req = new TdApi.AnswerCallbackQuery();
        req.callbackQueryId = callbackQueryId;
        req.text = text;
        req.showAlert = showAlert;
        req.url = url;
        req.cacheTime = cacheTime;
        return send(req);
    }

    /** Quick answer: just a toast notification. */
    public Mono<TdApi.Ok> answerCallbackQuery(long callbackQueryId, String text) {
        return answerCallbackQuery(callbackQueryId, text, false, null, 0);
    }

    // ------------------------------------------------------------------
    // Inline queries
    // ------------------------------------------------------------------

    /**
     * Answer an inline query.
     *
     * @param inlineQueryId       id from {@link TdApi.UpdateNewInlineQuery}
     * @param isPersonal          whether the results are personal (not cacheable globally)
     * @param cacheTime           seconds to cache the result server-side
     * @param results             array of result objects
     * @param nextOffset          pagination offset for next page
     * @param switchPmText        button text to switch to PM with the bot
     * @param switchPmParameter   start parameter appended to the PM deep link
     */
    public Mono<TdApi.Ok> answerInlineQuery(long inlineQueryId, boolean isPersonal,
                                             int cacheTime,
                                             TdApi.InputInlineQueryResult[] results,
                                             String nextOffset,
                                             String switchPmText,
                                             String switchPmParameter) {
        TdApi.AnswerInlineQuery req = new TdApi.AnswerInlineQuery();
        req.inlineQueryId = inlineQueryId;
        req.isPersonal = isPersonal;
        req.cacheTime = cacheTime;
        req.results = results;
        req.nextOffset = nextOffset;
        req.button = switchPmText != null
            ? new TdApi.InlineQueryResultsButton(switchPmText,
                new TdApi.InlineQueryResultsButtonTypeStartBot(switchPmParameter))
            : null;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Bot commands
    // ------------------------------------------------------------------

    public Mono<TdApi.BotCommands> getBotCommands(TdApi.BotCommandScope scope,
                                                    String languageCode) {
        TdApi.GetCommands req = new TdApi.GetCommands();
        req.scope = scope;
        req.languageCode = languageCode;
        return send(req);
    }

    public Mono<TdApi.Ok> setBotCommands(TdApi.BotCommandScope scope, String languageCode,
                                          TdApi.BotCommand[] commands) {
        TdApi.SetCommands req = new TdApi.SetCommands();
        req.scope = scope;
        req.languageCode = languageCode;
        req.commands = commands;
        return send(req);
    }

    public Mono<TdApi.Ok> deleteBotCommands(TdApi.BotCommandScope scope, String languageCode) {
        TdApi.DeleteCommands req = new TdApi.DeleteCommands();
        req.scope = scope;
        req.languageCode = languageCode;
        return send(req);
    }

    /** Set the list of bot commands visible in the default scope for any language. */
    public Mono<TdApi.Ok> setDefaultBotCommands(TdApi.BotCommand[] commands) {
        return setBotCommands(new TdApi.BotCommandScopeDefault(), "", commands);
    }

    // ------------------------------------------------------------------
    // Bot info
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> setBotName(long botUserId, String languageCode, String name) {
        TdApi.SetBotName req = new TdApi.SetBotName();
        req.botUserId = botUserId;
        req.languageCode = languageCode;
        req.name = name;
        return send(req);
    }

    public Mono<TdApi.Text> getBotName(long botUserId, String languageCode) {
        TdApi.GetBotName req = new TdApi.GetBotName();
        req.botUserId = botUserId;
        req.languageCode = languageCode;
        return send(req);
    }

    public Mono<TdApi.Ok> setBotInfoDescription(long botUserId, String languageCode,
                                                  String description) {
        TdApi.SetBotInfoDescription req = new TdApi.SetBotInfoDescription();
        req.botUserId = botUserId;
        req.languageCode = languageCode;
        req.description = description;
        return send(req);
    }

    public Mono<TdApi.Text> getBotInfoDescription(long botUserId, String languageCode) {
        TdApi.GetBotInfoDescription req = new TdApi.GetBotInfoDescription();
        req.botUserId = botUserId;
        req.languageCode = languageCode;
        return send(req);
    }

    public Mono<TdApi.Ok> setBotInfoShortDescription(long botUserId, String languageCode,
                                                       String shortDescription) {
        TdApi.SetBotInfoShortDescription req = new TdApi.SetBotInfoShortDescription();
        req.botUserId = botUserId;
        req.languageCode = languageCode;
        req.shortDescription = shortDescription;
        return send(req);
    }

    public Mono<TdApi.Text> getBotInfoShortDescription(long botUserId, String languageCode) {
        TdApi.GetBotInfoShortDescription req = new TdApi.GetBotInfoShortDescription();
        req.botUserId = botUserId;
        req.languageCode = languageCode;
        return send(req);
    }

    public Mono<TdApi.Ok> setBotProfilePhoto(long botUserId, TdApi.InputChatPhoto photo) {
        TdApi.SetBotProfilePhoto req = new TdApi.SetBotProfilePhoto();
        req.botUserId = botUserId;
        req.photo = photo;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Games
    // ------------------------------------------------------------------

    /**
     * Set the score of a user in a game.
     *
     * @param editMessage whether to update the message if the score changed
     * @param force       set even if the new score is less than the current
     */
    public Mono<TdApi.Message> setGameScore(long chatId, long messageId, boolean editMessage,
                                             long userId, int score, boolean force) {
        TdApi.SetGameScore req = new TdApi.SetGameScore();
        req.chatId = chatId;
        req.messageId = messageId;
        req.editMessage = editMessage;
        req.userId = userId;
        req.score = score;
        req.force = force;
        return send(req);
    }

    public Mono<TdApi.Ok> setInlineGameScore(String inlineMessageId, boolean editMessage,
                                               long userId, int score, boolean force) {
        TdApi.SetInlineGameScore req = new TdApi.SetInlineGameScore();
        req.inlineMessageId = inlineMessageId;
        req.editMessage = editMessage;
        req.userId = userId;
        req.score = score;
        req.force = force;
        return send(req);
    }

    public Mono<TdApi.GameHighScores> getGameHighScores(long chatId, long messageId,
                                                          long userId) {
        TdApi.GetGameHighScores req = new TdApi.GetGameHighScores();
        req.chatId = chatId;
        req.messageId = messageId;
        req.userId = userId;
        return send(req);
    }

    public Mono<TdApi.GameHighScores> getInlineGameHighScores(String inlineMessageId,
                                                                long userId) {
        TdApi.GetInlineGameHighScores req = new TdApi.GetInlineGameHighScores();
        req.inlineMessageId = inlineMessageId;
        req.userId = userId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Payments
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> answerPreCheckoutQuery(long preCheckoutQueryId, String errorMessage) {
        TdApi.AnswerPreCheckoutQuery req = new TdApi.AnswerPreCheckoutQuery();
        req.preCheckoutQueryId = preCheckoutQueryId;
        req.errorMessage = errorMessage;
        return send(req);
    }

    public Mono<TdApi.Ok> answerShippingQuery(long shippingQueryId,
                                               TdApi.ShippingOption[] shippingOptions,
                                               String errorMessage) {
        TdApi.AnswerShippingQuery req = new TdApi.AnswerShippingQuery();
        req.shippingQueryId = shippingQueryId;
        req.shippingOptions = shippingOptions;
        req.errorMessage = errorMessage;
        return send(req);
    }

    public Mono<TdApi.PaymentReceipt> getPaymentReceipt(long chatId, long messageId) {
        TdApi.GetPaymentReceipt req = new TdApi.GetPaymentReceipt();
        req.chatId = chatId;
        req.messageId = messageId;
        return send(req);
    }

    public Mono<TdApi.PaymentForm> getPaymentForm(long chatId, long messageId,
                                                    TdApi.ThemeParameters theme) {
        TdApi.GetPaymentForm req = new TdApi.GetPaymentForm();
        TdApi.InputInvoiceMessage invoice = new TdApi.InputInvoiceMessage();
        invoice.chatId = chatId;
        invoice.messageId = messageId;
        req.inputInvoice = invoice;
        req.theme = theme;
        return send(req);
    }

    public Mono<TdApi.ValidatedOrderInfo> validateOrderInfo(long chatId, long messageId,
                                                              TdApi.OrderInfo orderInfo,
                                                              boolean allowSave) {
        TdApi.ValidateOrderInfo req = new TdApi.ValidateOrderInfo();
        TdApi.InputInvoiceMessage invoice = new TdApi.InputInvoiceMessage();
        invoice.chatId = chatId;
        invoice.messageId = messageId;
        req.inputInvoice = invoice;
        req.orderInfo = orderInfo;
        req.allowSave = allowSave;
        return send(req);
    }

    public Mono<TdApi.PaymentResult> sendPaymentForm(long chatId, long messageId,
                                                       long paymentFormId,
                                                       String orderInfoId,
                                                       String shippingOptionId,
                                                       TdApi.InputCredentials credentials,
                                                       long tipAmount) {
        TdApi.SendPaymentForm req = new TdApi.SendPaymentForm();
        TdApi.InputInvoiceMessage invoice = new TdApi.InputInvoiceMessage();
        invoice.chatId = chatId;
        invoice.messageId = messageId;
        req.inputInvoice = invoice;
        req.paymentFormId = paymentFormId;
        req.orderInfoId = orderInfoId;
        req.shippingOptionId = shippingOptionId;
        req.credentials = credentials;
        req.tipAmount = tipAmount;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Web App
    // ------------------------------------------------------------------

    public Mono<TdApi.SentWebAppMessage> answerWebAppQuery(String webAppQueryId,
                                                             TdApi.InputInlineQueryResult result) {
        TdApi.AnswerWebAppQuery req = new TdApi.AnswerWebAppQuery();
        req.webAppQueryId = webAppQueryId;
        req.result = result;
        return send(req);
    }

    public Mono<TdApi.HttpUrl> getWebAppUrl(long botUserId, String url,
                                             TdApi.WebAppOpenParameters parameters) {
        TdApi.GetWebAppUrl req = new TdApi.GetWebAppUrl();
        req.botUserId = botUserId;
        req.url = url;
        req.parameters = parameters;
        return send(req);
    }

    public Mono<TdApi.WebAppInfo> openWebApp(long chatId, long botUserId, String url,
                                               long messageThreadId,
                                               TdApi.WebAppOpenParameters parameters) {
        TdApi.OpenWebApp req = new TdApi.OpenWebApp();
        req.chatId = chatId;
        req.botUserId = botUserId;
        req.url = url;
        req.messageThreadId = messageThreadId;
        req.parameters = parameters;
        return send(req);
    }

    public Mono<TdApi.Ok> closeWebApp(long webAppLaunchId) {
        TdApi.CloseWebApp req = new TdApi.CloseWebApp();
        req.webAppLaunchId = webAppLaunchId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Misc bot utilities
    // ------------------------------------------------------------------

    /** Revoke and regenerate the invite link for a group call. */
    public Mono<TdApi.Ok> revokeGroupCallInviteLink(int groupCallId) {
        TdApi.RevokeGroupCallInviteLink req = new TdApi.RevokeGroupCallInviteLink();
        req.groupCallId = groupCallId;
        return send(req);
    }

    public Mono<TdApi.BotMenuButton> getBotMenuButton(long userId) {
        TdApi.GetMenuButton req = new TdApi.GetMenuButton();
        req.userId = userId;
        return send(req);
    }

    public Mono<TdApi.Ok> setBotMenuButton(long userId, TdApi.BotMenuButton menuButton) {
        TdApi.SetMenuButton req = new TdApi.SetMenuButton();
        req.userId = userId;
        req.menuButton = menuButton;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Convenience: build inline result objects
    // ------------------------------------------------------------------

    public static TdApi.InputInlineQueryResultArticle articleResult(String id, String title,
                                                                      String description,
                                                                      String text) {
        TdApi.InputInlineQueryResultArticle r = new TdApi.InputInlineQueryResultArticle();
        r.id = id;
        r.title = title;
        r.description = description;
        TdApi.InputMessageText content = new TdApi.InputMessageText();
        content.text = new TdApi.FormattedText(text, new TdApi.TextEntity[0]);
        r.inputMessageContent = content;
        return r;
    }

    public static TdApi.BotCommand command(String command, String description) {
        TdApi.BotCommand cmd = new TdApi.BotCommand();
        cmd.command = command;
        cmd.description = description;
        return cmd;
    }
}
