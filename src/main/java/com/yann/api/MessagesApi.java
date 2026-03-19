package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import java.util.function.Supplier;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for all TDLib message-related functions.
 */
public final class MessagesApi extends TdlightOperations {

    public MessagesApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Sending messages
    // ------------------------------------------------------------------

    /** Send a plain-text message. */
    public Mono<TdApi.Message> sendText(long chatId, String text) {
        return sendText(chatId, text, 0L, null);
    }

    /** Send a plain-text message with an optional reply-to message id. */
    public Mono<TdApi.Message> sendText(long chatId, String text, long replyToMessageId) {
        return sendText(chatId, text, replyToMessageId, null);
    }

    /** Full send-text with reply and custom send options. */
    public Mono<TdApi.Message> sendText(long chatId, String text, long replyToMessageId,
                                        TdApi.MessageSendOptions options) {
        TdApi.InputMessageText content = textContent(text, false, true);
        return sendContent(chatId, 0L, replyToMessageId, options, content);
    }

    /** Send an HTML-formatted text using TDLib parse mode. */
    public Mono<TdApi.Message> sendHtml(long chatId, String htmlText) {
        return send(new TdApi.ParseTextEntities(htmlText, new TdApi.TextParseModeHTML()))
            .flatMap(formatted -> {
                TdApi.InputMessageText content = new TdApi.InputMessageText();
                content.text = formatted;
                
                content.clearDraft = true;
                return sendContent(chatId, 0L, 0L, null, content);
            });
    }

    /** Send Markdown-formatted text. */
    public Mono<TdApi.Message> sendMarkdown(long chatId, String markdownText) {
        return send(new TdApi.ParseTextEntities(markdownText, new TdApi.TextParseModeMarkdown(2)))
            .flatMap(formatted -> {
                TdApi.InputMessageText content = new TdApi.InputMessageText();
                content.text = formatted;
                
                content.clearDraft = true;
                return sendContent(chatId, 0L, 0L, null, content);
            });
    }

    /** Send a photo with optional caption. */
    public Mono<TdApi.Message> sendPhoto(long chatId, TdApi.InputFile photo, String caption) {
        TdApi.InputMessagePhoto content = new TdApi.InputMessagePhoto();
        content.photo = photo;
        if (caption != null && !caption.isBlank()) {
            content.caption = plainText(caption);
        }
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a video with optional caption. */
    public Mono<TdApi.Message> sendVideo(long chatId, TdApi.InputFile video, String caption,
                                         int width, int height, int duration) {
        TdApi.InputMessageVideo content = new TdApi.InputMessageVideo();
        content.video = video;
        content.width = width;
        content.height = height;
        content.duration = duration;
        content.supportsStreaming = true;
        if (caption != null && !caption.isBlank()) {
            content.caption = plainText(caption);
        }
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send an audio file with optional caption. */
    public Mono<TdApi.Message> sendAudio(long chatId, TdApi.InputFile audio, String caption,
                                         int duration, String title, String performer) {
        TdApi.InputMessageAudio content = new TdApi.InputMessageAudio();
        content.audio = audio;
        content.duration = duration;
        content.title = title;
        content.performer = performer;
        if (caption != null && !caption.isBlank()) {
            content.caption = plainText(caption);
        }
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a document/file with optional caption. */
    public Mono<TdApi.Message> sendDocument(long chatId, TdApi.InputFile document, String caption) {
        TdApi.InputMessageDocument content = new TdApi.InputMessageDocument();
        content.document = document;
        content.disableContentTypeDetection = false;
        if (caption != null && !caption.isBlank()) {
            content.caption = plainText(caption);
        }
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a sticker. */
    public Mono<TdApi.Message> sendSticker(long chatId, TdApi.InputFile sticker, String emoji) {
        TdApi.InputMessageSticker content = new TdApi.InputMessageSticker();
        content.sticker = sticker;
        content.emoji = emoji;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send an animation (GIF). */
    public Mono<TdApi.Message> sendAnimation(long chatId, TdApi.InputFile animation, String caption,
                                              int width, int height, int duration) {
        TdApi.InputMessageAnimation content = new TdApi.InputMessageAnimation();
        content.animation = animation;
        content.width = width;
        content.height = height;
        content.duration = duration;
        if (caption != null && !caption.isBlank()) {
            content.caption = plainText(caption);
        }
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a voice note (OGG/Opus). */
    public Mono<TdApi.Message> sendVoiceNote(long chatId, TdApi.InputFile voice, int duration,
                                              TdApi.FormattedText caption) {
        TdApi.InputMessageVoiceNote content = new TdApi.InputMessageVoiceNote();
        content.voiceNote = voice;
        content.duration = duration;
        content.caption = caption;
        content.waveform = new byte[0];
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a video note (circle video). */
    public Mono<TdApi.Message> sendVideoNote(long chatId, TdApi.InputFile videoNote,
                                              int duration, int length) {
        TdApi.InputMessageVideoNote content = new TdApi.InputMessageVideoNote();
        content.videoNote = videoNote;
        content.duration = duration;
        content.length = length;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a location. */
    public Mono<TdApi.Message> sendLocation(long chatId, double latitude, double longitude,
                                             int horizontalAccuracy, int livePeriod,
                                             int heading, int proximityAlertRadius) {
        TdApi.InputMessageLocation content = new TdApi.InputMessageLocation();
        TdApi.Location loc = new TdApi.Location();
        loc.latitude = latitude;
        loc.longitude = longitude;
        loc.horizontalAccuracy = horizontalAccuracy;
        content.location = loc;
        content.livePeriod = livePeriod;
        content.heading = heading;
        content.proximityAlertRadius = proximityAlertRadius;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a venue. */
    public Mono<TdApi.Message> sendVenue(long chatId, double latitude, double longitude,
                                          String title, String address, String provider,
                                          String providerId, String providerData) {
        TdApi.InputMessageVenue content = new TdApi.InputMessageVenue();
        TdApi.Venue venue = new TdApi.Venue();
        TdApi.Location loc = new TdApi.Location();
        loc.latitude = latitude;
        loc.longitude = longitude;
        venue.location = loc;
        venue.title = title;
        venue.address = address;
        venue.provider = provider;
        venue.id = providerId;
        venue.type = providerData;
        content.venue = venue;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a contact. */
    public Mono<TdApi.Message> sendContact(long chatId, String phoneNumber, String firstName,
                                            String lastName, String vcard) {
        TdApi.InputMessageContact content = new TdApi.InputMessageContact();
        TdApi.Contact contact = new TdApi.Contact();
        contact.phoneNumber = phoneNumber;
        contact.firstName = firstName;
        contact.lastName = lastName;
        contact.vcard = vcard;
        content.contact = contact;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a poll. */
    public Mono<TdApi.Message> sendPoll(long chatId, String question, String[] options,
                                         boolean isAnonymous, boolean allowMultipleAnswers,
                                         int openPeriod) {
        TdApi.InputMessagePoll content = new TdApi.InputMessagePoll();
        content.question = plainText(question);
        content.options = toFormattedTextArray(options);
        content.isAnonymous = isAnonymous;
        content.type = new TdApi.PollTypeRegular(allowMultipleAnswers);
        content.openPeriod = openPeriod;
        content.isClosed = false;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a quiz poll. */
    public Mono<TdApi.Message> sendQuiz(long chatId, String question, String[] options,
                                         int correctOptionId, String explanation, int openPeriod) {
        TdApi.InputMessagePoll content = new TdApi.InputMessagePoll();
        content.question = plainText(question);
        content.options = toFormattedTextArray(options);
        content.isAnonymous = true;
        TdApi.PollTypeQuiz quiz = new TdApi.PollTypeQuiz();
        quiz.correctOptionId = correctOptionId;
        quiz.explanation = explanation != null ? plainText(explanation) : plainText("");
        content.type = quiz;
        content.openPeriod = openPeriod;
        content.isClosed = false;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a dice with a given emoji (🎲 🎯 🏀 ⚽ 🎰 🎳). */
    public Mono<TdApi.Message> sendDice(long chatId, String emoji) {
        TdApi.InputMessageDice content = new TdApi.InputMessageDice();
        content.emoji = emoji;
        content.clearDraft = false;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send a game by its short name. */
    public Mono<TdApi.Message> sendGame(long chatId, long botUserId, String gameShortName) {
        TdApi.InputMessageGame content = new TdApi.InputMessageGame();
        content.botUserId = botUserId;
        content.gameShortName = gameShortName;
        return sendContent(chatId, 0L, 0L, null, content);
    }

    /** Send an invoice. */
    public Mono<TdApi.Message> sendInvoice(long chatId, TdApi.InputMessageInvoice invoice) {
        return sendContent(chatId, 0L, 0L, null, invoice);
    }

    /** Forward a list of messages to another chat. */
    public Mono<TdApi.Messages> forwardMessages(long chatId, long fromChatId, long[] messageIds,
                                                  boolean sendCopy, boolean removeCaption) {
        TdApi.ForwardMessages req = new TdApi.ForwardMessages();
        req.chatId = chatId;
        req.fromChatId = fromChatId;
        req.messageIds = messageIds;
        req.options = defaultSendOptions();
        req.sendCopy = sendCopy;
        req.removeCaption = removeCaption;
        return send(req);
    }

    /** Send a group of photos/videos as an album. */
    public Mono<TdApi.Messages> sendAlbum(long chatId,
                                            TdApi.InputMessageContent[] contents) {
        TdApi.SendMessageAlbum req = new TdApi.SendMessageAlbum();
        req.chatId = chatId;
        req.inputMessageContents = contents;
        req.options = defaultSendOptions();
        return send(req);
    }

    // ------------------------------------------------------------------
    // Editing messages
    // ------------------------------------------------------------------

    /** Edit the text of a sent message. */
    public Mono<TdApi.Message> editText(long chatId, long messageId, String newText) {
        TdApi.EditMessageText req = new TdApi.EditMessageText();
        req.chatId = chatId;
        req.messageId = messageId;
        req.inputMessageContent = textContent(newText, false, false);
        return send(req);
    }

    /** Edit with formatted text and optional inline keyboard. */
    public Mono<TdApi.Message> editText(long chatId, long messageId,
                                         TdApi.FormattedText formattedText,
                                         TdApi.ReplyMarkup replyMarkup) {
        TdApi.EditMessageText req = new TdApi.EditMessageText();
        req.chatId = chatId;
        req.messageId = messageId;
        TdApi.InputMessageText content = new TdApi.InputMessageText();
        content.text = formattedText;
        req.inputMessageContent = content;
        req.replyMarkup = replyMarkup;
        return send(req);
    }

    /** Edit message caption. */
    public Mono<TdApi.Message> editCaption(long chatId, long messageId, String caption) {
        TdApi.EditMessageCaption req = new TdApi.EditMessageCaption();
        req.chatId = chatId;
        req.messageId = messageId;
        req.caption = plainText(caption);
        return send(req);
    }

    /** Edit message media content. */
    public Mono<TdApi.Message> editMedia(long chatId, long messageId,
                                          TdApi.InputMessageContent newContent) {
        TdApi.EditMessageMedia req = new TdApi.EditMessageMedia();
        req.chatId = chatId;
        req.messageId = messageId;
        req.inputMessageContent = newContent;
        return send(req);
    }

    /** Edit inline keyboard only (without changing text). */
    public Mono<TdApi.Message> editReplyMarkup(long chatId, long messageId,
                                                 TdApi.ReplyMarkup replyMarkup) {
        TdApi.EditMessageReplyMarkup req = new TdApi.EditMessageReplyMarkup();
        req.chatId = chatId;
        req.messageId = messageId;
        req.replyMarkup = replyMarkup;
        return send(req);
    }

    /** Edit live location. */
    public Mono<TdApi.Message> editLiveLocation(long chatId, long messageId,
                                                   double latitude, double longitude,
                                                   double horizontalAccuracy,
                                                   int heading, int proximityAlertRadius) {
        TdApi.EditMessageLiveLocation req = new TdApi.EditMessageLiveLocation();
        req.chatId = chatId;
        req.messageId = messageId;
        TdApi.Location loc = new TdApi.Location();
        loc.latitude = latitude;
        loc.longitude = longitude;
        loc.horizontalAccuracy = horizontalAccuracy;
        req.location = loc;
        req.heading = heading;
        req.proximityAlertRadius = proximityAlertRadius;
        return send(req);
    }

    /** Stop live location sharing. */
    public Mono<TdApi.Message> stopLiveLocation(long chatId, long messageId) {
        TdApi.EditMessageLiveLocation req = new TdApi.EditMessageLiveLocation();
        req.chatId = chatId;
        req.messageId = messageId;
        req.location = null;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Deleting messages
    // ------------------------------------------------------------------

    /**
     * Delete messages.
     *
     * @param revoke {@code true} to delete for all participants (if allowed)
     */
    public Mono<TdApi.Ok> deleteMessages(long chatId, long[] messageIds, boolean revoke) {
        TdApi.DeleteMessages req = new TdApi.DeleteMessages();
        req.chatId = chatId;
        req.messageIds = messageIds;
        req.revoke = revoke;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Fetching messages
    // ------------------------------------------------------------------

    /** Get a single message by id. */
    public Mono<TdApi.Message> getMessage(long chatId, long messageId) {
        TdApi.GetMessage req = new TdApi.GetMessage();
        req.chatId = chatId;
        req.messageId = messageId;
        return send(req);
    }

    /** Get multiple messages in one call. */
    public Mono<TdApi.Messages> getMessages(long chatId, long[] messageIds) {
        TdApi.GetMessages req = new TdApi.GetMessages();
        req.chatId = chatId;
        req.messageIds = messageIds;
        return send(req);
    }

    /** Fetch chat history. */
    public Mono<TdApi.Messages> getChatHistory(long chatId, long fromMessageId,
                                                 int offset, int limit, boolean onlyLocal) {
        TdApi.GetChatHistory req = new TdApi.GetChatHistory();
        req.chatId = chatId;
        req.fromMessageId = fromMessageId;
        req.offset = offset;
        req.limit = limit;
        req.onlyLocal = onlyLocal;
        return send(req);
    }

    /** Search messages across all chats. */
    public Mono<TdApi.FoundMessages> searchMessages(String query, int limit) {
        TdApi.SearchMessages req = new TdApi.SearchMessages();
        req.query = query;
        req.limit = limit;
        req.chatList = new TdApi.ChatListMain();
        req.filter = new TdApi.SearchMessagesFilterEmpty();
        return send(req);
    }

    /** Search messages in a specific chat. */
    public Mono<TdApi.FoundChatMessages> searchChatMessages(long chatId, String query,
                                                              long fromMessageId, int offset, int limit,
                                                              TdApi.SearchMessagesFilter filter) {
        TdApi.SearchChatMessages req = new TdApi.SearchChatMessages();
        req.chatId = chatId;
        req.query = query;
        req.fromMessageId = fromMessageId;
        req.offset = offset;
        req.limit = limit;
        req.filter = filter != null ? filter : new TdApi.SearchMessagesFilterEmpty();
        return send(req);
    }

    /** Get all pinned messages in a chat. */
    public Mono<TdApi.Messages> getPinnedMessages(long chatId) {
        TdApi.GetChatPinnedMessage req = new TdApi.GetChatPinnedMessage();
        req.chatId = chatId;
        return send(req).map(msg -> {
            TdApi.Messages msgs = new TdApi.Messages();
            msgs.messages = new TdApi.Message[]{msg};
            msgs.totalCount = 1;
            return msgs;
        });
    }

    /** Get a permalink for a message. */
    public Mono<TdApi.MessageLink> getMessageLink(long chatId, long messageId,
                                                    int mediaTimestamp, boolean forAlbum,
                                                    boolean inMessageThread) {
        TdApi.GetMessageLink req = new TdApi.GetMessageLink();
        req.chatId = chatId;
        req.messageId = messageId;
        req.mediaTimestamp = mediaTimestamp;
        req.forAlbum = forAlbum;
        req.inMessageThread = inMessageThread;
        return send(req);
    }

    /** Get replies to a message (message thread). */
    public Mono<TdApi.MessageThreadInfo> getMessageThread(long chatId, long messageId) {
        TdApi.GetMessageThread req = new TdApi.GetMessageThread();
        req.chatId = chatId;
        req.messageId = messageId;
        return send(req);
    }

    /** Get messages scheduled in a chat. */
    public Mono<TdApi.Messages> getScheduledMessages(long chatId) {
        TdApi.GetChatScheduledMessages req = new TdApi.GetChatScheduledMessages();
        req.chatId = chatId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Pinning
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> pinMessage(long chatId, long messageId, boolean disableNotification,
                                      boolean onlyForSelf) {
        TdApi.PinChatMessage req = new TdApi.PinChatMessage();
        req.chatId = chatId;
        req.messageId = messageId;
        req.disableNotification = disableNotification;
        req.onlyForSelf = onlyForSelf;
        return send(req);
    }

    public Mono<TdApi.Ok> unpinMessage(long chatId, long messageId) {
        TdApi.UnpinChatMessage req = new TdApi.UnpinChatMessage();
        req.chatId = chatId;
        req.messageId = messageId;
        return send(req);
    }

    public Mono<TdApi.Ok> unpinAllMessages(long chatId) {
        TdApi.UnpinAllChatMessages req = new TdApi.UnpinAllChatMessages();
        req.chatId = chatId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Chat actions (typing, uploading, etc.)
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> sendChatAction(long chatId, long messageThreadId,
                                          TdApi.ChatAction action) {
        TdApi.SendChatAction req = new TdApi.SendChatAction();
        req.chatId = chatId;
        req.messageThreadId = messageThreadId;
        req.action = action;
        return send(req);
    }

    public Mono<TdApi.Ok> sendTypingAction(long chatId) {
        return sendChatAction(chatId, 0L, new TdApi.ChatActionTyping());
    }

    public Mono<TdApi.Ok> sendUploadPhotoAction(long chatId) {
        return sendChatAction(chatId, 0L, new TdApi.ChatActionUploadingPhoto(0));
    }

    public Mono<TdApi.Ok> sendUploadDocumentAction(long chatId) {
        return sendChatAction(chatId, 0L, new TdApi.ChatActionUploadingDocument(0));
    }

    public Mono<TdApi.Ok> sendRecordVideoAction(long chatId) {
        return sendChatAction(chatId, 0L, new TdApi.ChatActionRecordingVideo());
    }

    public Mono<TdApi.Ok> cancelChatAction(long chatId) {
        return sendChatAction(chatId, 0L, new TdApi.ChatActionCancel());
    }

    // ------------------------------------------------------------------
    // Reactions
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> addMessageReaction(long chatId, long messageId,
                                               TdApi.ReactionType reactionType,
                                               boolean isBig, boolean updateRecentReactions) {
        TdApi.AddMessageReaction req = new TdApi.AddMessageReaction();
        req.chatId = chatId;
        req.messageId = messageId;
        req.reactionType = reactionType;
        req.isBig = isBig;
        req.updateRecentReactions = updateRecentReactions;
        return send(req);
    }

    public Mono<TdApi.Ok> removeMessageReaction(long chatId, long messageId,
                                                   TdApi.ReactionType reactionType) {
        TdApi.RemoveMessageReaction req = new TdApi.RemoveMessageReaction();
        req.chatId = chatId;
        req.messageId = messageId;
        req.reactionType = reactionType;
        return send(req);
    }

    public Mono<TdApi.AddedReactions> getMessageAddedReactions(long chatId, long messageId,
                                                                  TdApi.ReactionType filter,
                                                                  String offset, int limit) {
        TdApi.GetMessageAddedReactions req = new TdApi.GetMessageAddedReactions();
        req.chatId = chatId;
        req.messageId = messageId;
        req.reactionType = filter;
        req.offset = offset;
        req.limit = limit;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Read state
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> viewMessages(long chatId, long[] messageIds, boolean forceRead) {
        TdApi.ViewMessages req = new TdApi.ViewMessages();
        req.chatId = chatId;
        req.messageIds = messageIds;
        req.forceRead = forceRead;
        return send(req);
    }

    public Mono<TdApi.Ok> openMessage(long chatId, long messageId) {
        TdApi.OpenMessageContent req = new TdApi.OpenMessageContent();
        req.chatId = chatId;
        req.messageId = messageId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Private helpers
    // ------------------------------------------------------------------

    private Mono<TdApi.Message> sendContent(long chatId, long messageThreadId,
                                              long replyToMessageId,
                                              TdApi.MessageSendOptions options,
                                              TdApi.InputMessageContent content) {
        TdApi.SendMessage req = new TdApi.SendMessage();
        req.chatId = chatId;
        req.messageThreadId = messageThreadId;
        req.options = options != null ? options : defaultSendOptions();
        req.inputMessageContent = content;

        if (replyToMessageId > 0) {
            TdApi.InputMessageReplyToMessage replyTo = new TdApi.InputMessageReplyToMessage();
            replyTo.messageId = replyToMessageId;
            req.replyTo = replyTo;
        }

        return send(req);
    }

    private static TdApi.MessageSendOptions defaultSendOptions() {
        TdApi.MessageSendOptions opts = new TdApi.MessageSendOptions();
        opts.disableNotification = false;
        opts.fromBackground = false;
        opts.protectContent = false;
        return opts;
    }

    private static TdApi.InputMessageText textContent(String text,
                                                        boolean disableLinkPreview,
                                                        boolean clearDraft) {
        TdApi.InputMessageText content = new TdApi.InputMessageText();
        content.text = plainText(text);
        if (disableLinkPreview) {
            TdApi.LinkPreviewOptions opts = new TdApi.LinkPreviewOptions();
            opts.isDisabled = true;
            content.linkPreviewOptions = opts;
        }
        content.clearDraft = clearDraft;
        return content;
    }

    /** Create a FormattedText with no entities from a plain string. */
    public static TdApi.FormattedText plainText(String text) {
        return new TdApi.FormattedText(text, new TdApi.TextEntity[0]);
    }

    /** Convert a String array to a FormattedText array (plain, no entities). */
    private static TdApi.FormattedText[] toFormattedTextArray(String[] strings) {
        TdApi.FormattedText[] result = new TdApi.FormattedText[strings.length];
        for (int i = 0; i < strings.length; i++) {
            result[i] = plainText(strings[i]);
        }
        return result;
    }
}
