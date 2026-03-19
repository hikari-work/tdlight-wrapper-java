package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Reactive wrapper for TDLib payment operations.
 *
 * <h2>Typical payment flow</h2>
 * <ol>
 *   <li>Bot sends an invoice via {@link MessagesApi#sendInvoice}</li>
 *   <li>User taps "Pay" → bot receives {@link TdApi.UpdateNewPreCheckoutQuery}</li>
 *   <li>Bot calls {@link BotApi#answerPreCheckoutQuery} to confirm/reject</li>
 *   <li>Payment is processed; bot receives {@link TdApi.UpdateNewMessage} with
 *       {@link TdApi.MessagePaymentSuccessful}</li>
 * </ol>
 */
public final class PaymentsApi extends TdlightOperations {

    public PaymentsApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Payment form
    // ------------------------------------------------------------------

    /**
     * Get a payment form for an invoice message.
     * Use {@link TdApi.InputInvoiceMessage} for messages, or
     * {@link TdApi.InputInvoiceName} for named payment links.
     */
    public Mono<TdApi.PaymentForm> getPaymentForm(TdApi.InputInvoice inputInvoice,
                                                    TdApi.ThemeParameters theme) {
        TdApi.GetPaymentForm req = new TdApi.GetPaymentForm();
        req.inputInvoice = inputInvoice;
        req.theme = theme;
        return send(req);
    }

    /** Convenience: get payment form for a message invoice. */
    public Mono<TdApi.PaymentForm> getPaymentForm(long chatId, long messageId) {
        TdApi.InputInvoiceMessage invoice = new TdApi.InputInvoiceMessage();
        invoice.chatId = chatId;
        invoice.messageId = messageId;
        return getPaymentForm(invoice, null);
    }

    /**
     * Validate shipping information for an invoice.
     * Returns available shipping options if the bot supports them.
     */
    public Mono<TdApi.ValidatedOrderInfo> validateOrderInfo(TdApi.InputInvoice inputInvoice,
                                                              TdApi.OrderInfo orderInfo,
                                                              boolean allowSave) {
        TdApi.ValidateOrderInfo req = new TdApi.ValidateOrderInfo();
        req.inputInvoice = inputInvoice;
        req.orderInfo = orderInfo;
        req.allowSave = allowSave;
        return send(req);
    }

    /** Convenience: validate order info for a message invoice. */
    public Mono<TdApi.ValidatedOrderInfo> validateOrderInfo(long chatId, long messageId,
                                                              TdApi.OrderInfo orderInfo,
                                                              boolean allowSave) {
        TdApi.InputInvoiceMessage invoice = new TdApi.InputInvoiceMessage();
        invoice.chatId = chatId;
        invoice.messageId = messageId;
        return validateOrderInfo(invoice, orderInfo, allowSave);
    }

    // ------------------------------------------------------------------
    // Sending payment
    // ------------------------------------------------------------------

    /**
     * Send a payment form to complete a payment.
     *
     * @param inputInvoice   the target invoice
     * @param formId         from {@link TdApi.PaymentForm#id}
     * @param orderInfoId    from {@link TdApi.ValidatedOrderInfo#orderInfoId}, or empty string
     * @param shippingOptionId selected shipping option id, or empty string
     * @param credentials    payment credentials (e.g. {@link TdApi.InputCredentialsNew})
     * @param tipAmount      tip in the smallest currency unit (0 if no tip)
     */
    public Mono<TdApi.PaymentResult> sendPaymentForm(TdApi.InputInvoice inputInvoice,
                                                       long formId,
                                                       String orderInfoId,
                                                       String shippingOptionId,
                                                       TdApi.InputCredentials credentials,
                                                       long tipAmount) {
        TdApi.SendPaymentForm req = new TdApi.SendPaymentForm();
        req.inputInvoice = inputInvoice;
        req.paymentFormId = formId;
        req.orderInfoId = orderInfoId != null ? orderInfoId : "";
        req.shippingOptionId = shippingOptionId != null ? shippingOptionId : "";
        req.credentials = credentials;
        req.tipAmount = tipAmount;
        return send(req);
    }

    /** Convenience: send payment for a message invoice. */
    public Mono<TdApi.PaymentResult> sendPaymentForm(long chatId, long messageId,
                                                       long formId,
                                                       String orderInfoId,
                                                       String shippingOptionId,
                                                       TdApi.InputCredentials credentials,
                                                       long tipAmount) {
        TdApi.InputInvoiceMessage invoice = new TdApi.InputInvoiceMessage();
        invoice.chatId = chatId;
        invoice.messageId = messageId;
        return sendPaymentForm(invoice, formId, orderInfoId, shippingOptionId, credentials, tipAmount);
    }

    // ------------------------------------------------------------------
    // Receipts & saved data
    // ------------------------------------------------------------------

    /** Get payment receipt for a successful payment message. */
    public Mono<TdApi.PaymentReceipt> getPaymentReceipt(long chatId, long messageId) {
        TdApi.GetPaymentReceipt req = new TdApi.GetPaymentReceipt();
        req.chatId = chatId;
        req.messageId = messageId;
        return send(req);
    }

    /** Get saved order information (shipping address, etc.). */
    public Mono<TdApi.OrderInfo> getSavedOrderInfo() {
        return send(new TdApi.GetSavedOrderInfo());
    }

    /** Delete saved order information. */
    public Mono<TdApi.Ok> deleteSavedOrderInfo() {
        return send(new TdApi.DeleteSavedOrderInfo());
    }

    /** Delete saved payment credentials (e.g. card tokens). */
    public Mono<TdApi.Ok> deleteSavedCredentials() {
        return send(new TdApi.DeleteSavedCredentials());
    }

    // ------------------------------------------------------------------
    // Invoice links
    // ------------------------------------------------------------------

    /**
     * Create a payment link for an invoice.
     * The invoice must be an {@link TdApi.InputMessageInvoice}.
     */
    public Mono<TdApi.HttpUrl> createInvoiceLink(TdApi.InputMessageInvoice invoice) {
        TdApi.CreateInvoiceLink req = new TdApi.CreateInvoiceLink();
        req.invoice = invoice;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Telegram Stars
    // ------------------------------------------------------------------

    /**
     * Refund a Telegram Star payment.
     * Only available to bots; refunds the Stars to the user.
     *
     * @param userId                  Telegram user id of the payer
     * @param telegramPaymentChargeId charge id from the payment receipt
     */
    public Mono<TdApi.Ok> refundStarPayment(long userId, String telegramPaymentChargeId) {
        TdApi.RefundStarPayment req = new TdApi.RefundStarPayment();
        req.userId = userId;
        req.telegramPaymentChargeId = telegramPaymentChargeId;
        return send(req);
    }

    /**
     * Get Telegram Star transactions for a given owner (bot, channel, etc.).
     *
     * @param owner          the owner (e.g. a {@link TdApi.MessageSenderUser} or
     *                       {@link TdApi.MessageSenderChat})
     * @param subscriptionId filter by subscription id, or empty string for all
     * @param offset         pagination offset (empty string for first page)
     * @param limit          maximum number of results per page
     */
    /**
     * @param ownerId        the owner (e.g. a {@link TdApi.MessageSenderUser} or
     *                       {@link TdApi.MessageSenderChat})
     * @param subscriptionId filter by subscription id, or empty string for all
     * @param direction      filter by direction (null = both directions)
     * @param offset         pagination offset (empty string for first page)
     * @param limit          maximum number of results per page
     */
    public Mono<TdApi.StarTransactions> getStarTransactions(TdApi.MessageSender ownerId,
                                                              String subscriptionId,
                                                              TdApi.TransactionDirection direction,
                                                              String offset, int limit) {
        TdApi.GetStarTransactions req = new TdApi.GetStarTransactions();
        req.ownerId = ownerId;
        req.subscriptionId = subscriptionId != null ? subscriptionId : "";
        req.direction = direction;
        req.offset = offset != null ? offset : "";
        req.limit = limit;
        return send(req);
    }
}
