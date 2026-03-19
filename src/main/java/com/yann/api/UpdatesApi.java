package com.yann.api;

import it.tdlight.jni.TdApi;
import reactor.core.publisher.Flux;

/**
 * Exposes all TDLib update types as typed reactive streams.
 * Each method returns a hot {@link Flux} backed by the shared update sink
 * inside {@link com.yann.ReactiveTelegramClient}.
 */
public final class UpdatesApi {

    private final Flux<TdApi.Update> source;

    public UpdatesApi(Flux<TdApi.Update> source) {
        this.source = source;
    }

    /** All updates. */
    public Flux<TdApi.Update> all() {
        return source;
    }

    // ------------------------------------------------------------------
    // Messages
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateNewMessage> newMessages() {
        return source.ofType(TdApi.UpdateNewMessage.class);
    }

    public Flux<TdApi.UpdateMessageSendSucceeded> messageSendSucceeded() {
        return source.ofType(TdApi.UpdateMessageSendSucceeded.class);
    }

    public Flux<TdApi.UpdateMessageSendFailed> messageSendFailed() {
        return source.ofType(TdApi.UpdateMessageSendFailed.class);
    }

    public Flux<TdApi.UpdateMessageContent> messageContentChanged() {
        return source.ofType(TdApi.UpdateMessageContent.class);
    }

    public Flux<TdApi.UpdateMessageEdited> messageEdited() {
        return source.ofType(TdApi.UpdateMessageEdited.class);
    }

    public Flux<TdApi.UpdateDeleteMessages> messagesDeleted() {
        return source.ofType(TdApi.UpdateDeleteMessages.class);
    }

    public Flux<TdApi.UpdateMessageIsPinned> messagePinnedChanged() {
        return source.ofType(TdApi.UpdateMessageIsPinned.class);
    }

    public Flux<TdApi.UpdateMessageInteractionInfo> messageInteractionInfo() {
        return source.ofType(TdApi.UpdateMessageInteractionInfo.class);
    }

    public Flux<TdApi.UpdateMessageUnreadReactions> messageUnreadReactions() {
        return source.ofType(TdApi.UpdateMessageUnreadReactions.class);
    }

    // ------------------------------------------------------------------
    // Chats
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateNewChat> newChat() {
        return source.ofType(TdApi.UpdateNewChat.class);
    }

    public Flux<TdApi.UpdateChatTitle> chatTitleChanged() {
        return source.ofType(TdApi.UpdateChatTitle.class);
    }

    public Flux<TdApi.UpdateChatPhoto> chatPhotoChanged() {
        return source.ofType(TdApi.UpdateChatPhoto.class);
    }

    public Flux<TdApi.UpdateChatPermissions> chatPermissionsChanged() {
        return source.ofType(TdApi.UpdateChatPermissions.class);
    }

    public Flux<TdApi.UpdateChatLastMessage> chatLastMessage() {
        return source.ofType(TdApi.UpdateChatLastMessage.class);
    }

    public Flux<TdApi.UpdateChatPosition> chatPosition() {
        return source.ofType(TdApi.UpdateChatPosition.class);
    }

    public Flux<TdApi.UpdateChatReadInbox> chatReadInbox() {
        return source.ofType(TdApi.UpdateChatReadInbox.class);
    }

    public Flux<TdApi.UpdateChatReadOutbox> chatReadOutbox() {
        return source.ofType(TdApi.UpdateChatReadOutbox.class);
    }

    public Flux<TdApi.UpdateChatUnreadMentionCount> chatUnreadMentionCount() {
        return source.ofType(TdApi.UpdateChatUnreadMentionCount.class);
    }

    public Flux<TdApi.UpdateChatUnreadReactionCount> chatUnreadReactionCount() {
        return source.ofType(TdApi.UpdateChatUnreadReactionCount.class);
    }

    public Flux<TdApi.UpdateChatNotificationSettings> chatNotificationSettings() {
        return source.ofType(TdApi.UpdateChatNotificationSettings.class);
    }

    public Flux<TdApi.UpdateChatActionBar> chatActionBar() {
        return source.ofType(TdApi.UpdateChatActionBar.class);
    }

    public Flux<TdApi.UpdateChatAvailableReactions> chatAvailableReactions() {
        return source.ofType(TdApi.UpdateChatAvailableReactions.class);
    }

    public Flux<TdApi.UpdateChatDraftMessage> chatDraftMessage() {
        return source.ofType(TdApi.UpdateChatDraftMessage.class);
    }

    public Flux<TdApi.UpdateChatMessageSender> chatMessageSender() {
        return source.ofType(TdApi.UpdateChatMessageSender.class);
    }

    public Flux<TdApi.UpdateChatMessageAutoDeleteTime> chatMessageAutoDeleteTime() {
        return source.ofType(TdApi.UpdateChatMessageAutoDeleteTime.class);
    }

    public Flux<TdApi.UpdateChatHasScheduledMessages> chatHasScheduledMessages() {
        return source.ofType(TdApi.UpdateChatHasScheduledMessages.class);
    }

    public Flux<TdApi.UpdateChatBlockList> chatBlockList() {
        return source.ofType(TdApi.UpdateChatBlockList.class);
    }

    public Flux<TdApi.UpdateChatIsMarkedAsUnread> chatIsMarkedAsUnread() {
        return source.ofType(TdApi.UpdateChatIsMarkedAsUnread.class);
    }

    public Flux<TdApi.UpdateChatFolders> chatFolders() {
        return source.ofType(TdApi.UpdateChatFolders.class);
    }

    public Flux<TdApi.UpdateChatOnlineMemberCount> chatOnlineMemberCount() {
        return source.ofType(TdApi.UpdateChatOnlineMemberCount.class);
    }

    // ------------------------------------------------------------------
    // Chat members
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateChatMember> chatMember() {
        return source.ofType(TdApi.UpdateChatMember.class);
    }

    public Flux<TdApi.UpdateNewChatJoinRequest> newChatJoinRequest() {
        return source.ofType(TdApi.UpdateNewChatJoinRequest.class);
    }

    // ------------------------------------------------------------------
    // Users
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateUser> user() {
        return source.ofType(TdApi.UpdateUser.class);
    }

    public Flux<TdApi.UpdateUserStatus> userStatus() {
        return source.ofType(TdApi.UpdateUserStatus.class);
    }

    public Flux<TdApi.UpdateUserFullInfo> userFullInfo() {
        return source.ofType(TdApi.UpdateUserFullInfo.class);
    }

    public Flux<TdApi.UpdateUserPrivacySettingRules> userPrivacySettingRules() {
        return source.ofType(TdApi.UpdateUserPrivacySettingRules.class);
    }

    // ------------------------------------------------------------------
    // Authorization / connection
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateAuthorizationState> authorizationState() {
        return source.ofType(TdApi.UpdateAuthorizationState.class);
    }

    public Flux<TdApi.UpdateConnectionState> connectionState() {
        return source.ofType(TdApi.UpdateConnectionState.class);
    }

    // ------------------------------------------------------------------
    // Bot interactions
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateNewCallbackQuery> callbackQuery() {
        return source.ofType(TdApi.UpdateNewCallbackQuery.class);
    }

    public Flux<TdApi.UpdateNewInlineCallbackQuery> inlineCallbackQuery() {
        return source.ofType(TdApi.UpdateNewInlineCallbackQuery.class);
    }

    public Flux<TdApi.UpdateNewInlineQuery> inlineQuery() {
        return source.ofType(TdApi.UpdateNewInlineQuery.class);
    }

    public Flux<TdApi.UpdateNewChosenInlineResult> chosenInlineResult() {
        return source.ofType(TdApi.UpdateNewChosenInlineResult.class);
    }

    public Flux<TdApi.UpdateNewPreCheckoutQuery> preCheckoutQuery() {
        return source.ofType(TdApi.UpdateNewPreCheckoutQuery.class);
    }

    public Flux<TdApi.UpdateNewShippingQuery> shippingQuery() {
        return source.ofType(TdApi.UpdateNewShippingQuery.class);
    }

    public Flux<TdApi.UpdateNewCustomEvent> customEvent() {
        return source.ofType(TdApi.UpdateNewCustomEvent.class);
    }

    public Flux<TdApi.UpdateNewCustomQuery> customQuery() {
        return source.ofType(TdApi.UpdateNewCustomQuery.class);
    }

    // ------------------------------------------------------------------
    // Files
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateFile> file() {
        return source.ofType(TdApi.UpdateFile.class);
    }

    public Flux<TdApi.UpdateFileGenerationStart> fileGenerationStart() {
        return source.ofType(TdApi.UpdateFileGenerationStart.class);
    }

    public Flux<TdApi.UpdateFileGenerationStop> fileGenerationStop() {
        return source.ofType(TdApi.UpdateFileGenerationStop.class);
    }

    public Flux<TdApi.UpdateFileDownloads> fileDownloads() {
        return source.ofType(TdApi.UpdateFileDownloads.class);
    }

    public Flux<TdApi.UpdateFileAddedToDownloads> fileAddedToDownloads() {
        return source.ofType(TdApi.UpdateFileAddedToDownloads.class);
    }

    public Flux<TdApi.UpdateFileRemovedFromDownloads> fileRemovedFromDownloads() {
        return source.ofType(TdApi.UpdateFileRemovedFromDownloads.class);
    }

    // ------------------------------------------------------------------
    // Notifications
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateNotification> notification() {
        return source.ofType(TdApi.UpdateNotification.class);
    }

    public Flux<TdApi.UpdateNotificationGroup> notificationGroup() {
        return source.ofType(TdApi.UpdateNotificationGroup.class);
    }

    public Flux<TdApi.UpdateActiveNotifications> activeNotifications() {
        return source.ofType(TdApi.UpdateActiveNotifications.class);
    }

    public Flux<TdApi.UpdateHavePendingNotifications> havePendingNotifications() {
        return source.ofType(TdApi.UpdateHavePendingNotifications.class);
    }

    // ------------------------------------------------------------------
    // Calls
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateCall> call() {
        return source.ofType(TdApi.UpdateCall.class);
    }

    public Flux<TdApi.UpdateGroupCall> groupCall() {
        return source.ofType(TdApi.UpdateGroupCall.class);
    }

    public Flux<TdApi.UpdateGroupCallParticipant> groupCallParticipant() {
        return source.ofType(TdApi.UpdateGroupCallParticipant.class);
    }

    // ------------------------------------------------------------------
    // Stickers
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateInstalledStickerSets> installedStickerSets() {
        return source.ofType(TdApi.UpdateInstalledStickerSets.class);
    }

    public Flux<TdApi.UpdateTrendingStickerSets> trendingStickerSets() {
        return source.ofType(TdApi.UpdateTrendingStickerSets.class);
    }

    public Flux<TdApi.UpdateRecentStickers> recentStickers() {
        return source.ofType(TdApi.UpdateRecentStickers.class);
    }

    public Flux<TdApi.UpdateFavoriteStickers> favoriteStickers() {
        return source.ofType(TdApi.UpdateFavoriteStickers.class);
    }

    public Flux<TdApi.UpdateSavedAnimations> savedAnimations() {
        return source.ofType(TdApi.UpdateSavedAnimations.class);
    }

    // ------------------------------------------------------------------
    // Miscellaneous
    // ------------------------------------------------------------------

    public Flux<TdApi.UpdateOption> option() {
        return source.ofType(TdApi.UpdateOption.class);
    }

    public Flux<TdApi.UpdateScopeNotificationSettings> scopeNotificationSettings() {
        return source.ofType(TdApi.UpdateScopeNotificationSettings.class);
    }

    public Flux<TdApi.UpdateChatThemes> chatThemes() {
        return source.ofType(TdApi.UpdateChatThemes.class);
    }

    public Flux<TdApi.UpdateLanguagePackStrings> languagePackStrings() {
        return source.ofType(TdApi.UpdateLanguagePackStrings.class);
    }

    public Flux<TdApi.UpdateTermsOfService> termsOfService() {
        return source.ofType(TdApi.UpdateTermsOfService.class);
    }

    public Flux<TdApi.UpdateUnreadMessageCount> unreadMessageCount() {
        return source.ofType(TdApi.UpdateUnreadMessageCount.class);
    }

    public Flux<TdApi.UpdateUnreadChatCount> unreadChatCount() {
        return source.ofType(TdApi.UpdateUnreadChatCount.class);
    }

    public Flux<TdApi.UpdateAnimatedEmojiMessageClicked> animatedEmojiMessageClicked() {
        return source.ofType(TdApi.UpdateAnimatedEmojiMessageClicked.class);
    }

    public Flux<TdApi.UpdateAnimationSearchParameters> animationSearchParameters() {
        return source.ofType(TdApi.UpdateAnimationSearchParameters.class);
    }

    public Flux<TdApi.UpdateSuggestedActions> suggestedActions() {
        return source.ofType(TdApi.UpdateSuggestedActions.class);
    }

    public Flux<TdApi.UpdateAttachmentMenuBots> attachmentMenuBots() {
        return source.ofType(TdApi.UpdateAttachmentMenuBots.class);
    }

    public Flux<TdApi.UpdateWebAppMessageSent> webAppMessageSent() {
        return source.ofType(TdApi.UpdateWebAppMessageSent.class);
    }

    public Flux<TdApi.UpdateActiveEmojiReactions> activeEmojiReactions() {
        return source.ofType(TdApi.UpdateActiveEmojiReactions.class);
    }

    public Flux<TdApi.UpdateDefaultReactionType> defaultReactionType() {
        return source.ofType(TdApi.UpdateDefaultReactionType.class);
    }

    public Flux<TdApi.UpdateSpeechRecognitionTrial> speechRecognitionTrial() {
        return source.ofType(TdApi.UpdateSpeechRecognitionTrial.class);
    }

    public Flux<TdApi.UpdateDiceEmojis> diceEmojis() {
        return source.ofType(TdApi.UpdateDiceEmojis.class);
    }

    public Flux<TdApi.UpdateSavedNotificationSounds> savedNotificationSounds() {
        return source.ofType(TdApi.UpdateSavedNotificationSounds.class);
    }
}
