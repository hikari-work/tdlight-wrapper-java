package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import java.util.function.Supplier;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for user/contact profile operations.
 */
public final class UsersApi extends TdlightOperations {

    public UsersApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Getting user info
    // ------------------------------------------------------------------

    /** Get the currently authenticated user. */
    public Mono<TdApi.User> getMe() {
        return send(new TdApi.GetMe());
    }

    /** Get basic info of a user. */
    public Mono<TdApi.User> getUser(long userId) {
        TdApi.GetUser req = new TdApi.GetUser();
        req.userId = userId;
        return send(req);
    }

    /** Get full info of a user (bio, friend count, etc.). */
    public Mono<TdApi.UserFullInfo> getUserFullInfo(long userId) {
        TdApi.GetUserFullInfo req = new TdApi.GetUserFullInfo();
        req.userId = userId;
        return send(req);
    }

    /** Get profile photos of a user. */
    public Mono<TdApi.ChatPhotos> getUserProfilePhotos(long userId, int offset, int limit) {
        TdApi.GetUserProfilePhotos req = new TdApi.GetUserProfilePhotos();
        req.userId = userId;
        req.offset = offset;
        req.limit = limit;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Blocking
    // ------------------------------------------------------------------

    /** Block a message sender (user or channel) by adding them to the blocklist. */
    public Mono<TdApi.Ok> blockMessageSender(TdApi.MessageSender sender) {
        TdApi.SetMessageSenderBlockList req = new TdApi.SetMessageSenderBlockList();
        req.senderId = sender;
        req.blockList = new TdApi.BlockListMain();
        return send(req);
    }

    /** Unblock a message sender by removing them from all blocklists. */
    public Mono<TdApi.Ok> unblockMessageSender(TdApi.MessageSender sender) {
        TdApi.SetMessageSenderBlockList req = new TdApi.SetMessageSenderBlockList();
        req.senderId = sender;
        req.blockList = null;
        return send(req);
    }

    /** Block by userId. */
    public Mono<TdApi.Ok> blockUser(long userId) {
        TdApi.MessageSenderUser sender = new TdApi.MessageSenderUser();
        sender.userId = userId;
        return blockMessageSender(sender);
    }

    /** Unblock by userId. */
    public Mono<TdApi.Ok> unblockUser(long userId) {
        TdApi.MessageSenderUser sender = new TdApi.MessageSenderUser();
        sender.userId = userId;
        return unblockMessageSender(sender);
    }

    /** Get blocked senders (users / channels). */
    public Mono<TdApi.MessageSenders> getBlockedMessageSenders(int offset, int limit) {
        TdApi.GetBlockedMessageSenders req = new TdApi.GetBlockedMessageSenders();
        req.offset = offset;
        req.limit = limit;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Privacy settings
    // ------------------------------------------------------------------

    public Mono<TdApi.UserPrivacySettingRules> getUserPrivacySettingRules(
            TdApi.UserPrivacySetting setting) {
        TdApi.GetUserPrivacySettingRules req = new TdApi.GetUserPrivacySettingRules();
        req.setting = setting;
        return send(req);
    }

    public Mono<TdApi.Ok> setUserPrivacySettingRules(TdApi.UserPrivacySetting setting,
                                                       TdApi.UserPrivacySettingRules rules) {
        TdApi.SetUserPrivacySettingRules req = new TdApi.SetUserPrivacySettingRules();
        req.setting = setting;
        req.rules = rules;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Account settings
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> setName(String firstName, String lastName) {
        TdApi.SetName req = new TdApi.SetName();
        req.firstName = firstName;
        req.lastName = lastName;
        return send(req);
    }

    public Mono<TdApi.Ok> setBio(String bio) {
        TdApi.SetBio req = new TdApi.SetBio();
        req.bio = bio;
        return send(req);
    }

    public Mono<TdApi.Ok> setUsername(String username) {
        TdApi.SetUsername req = new TdApi.SetUsername();
        req.username = username;
        return send(req);
    }

    public Mono<TdApi.Ok> setProfilePhoto(TdApi.InputChatPhoto photo, boolean isPublic) {
        TdApi.SetProfilePhoto req = new TdApi.SetProfilePhoto();
        req.photo = photo;
        req.isPublic = isPublic;
        return send(req);
    }

    public Mono<TdApi.Ok> deleteProfilePhoto(long profilePhotoId) {
        TdApi.DeleteProfilePhoto req = new TdApi.DeleteProfilePhoto();
        req.profilePhotoId = profilePhotoId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Status
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> setOnlineStatus(boolean online) {
        TdApi.SetOption setOption = new TdApi.SetOption();
        setOption.name = "online";
        setOption.value = new TdApi.OptionValueBoolean(online);
        return send(setOption);
    }

    // ------------------------------------------------------------------
    // Account deletion
    // ------------------------------------------------------------------

    public Mono<TdApi.TemporaryPasswordState> createTemporaryPassword(String password,
                                                                         int validFor) {
        TdApi.CreateTemporaryPassword req = new TdApi.CreateTemporaryPassword();
        req.password = password;
        req.validFor = validFor;
        return send(req);
    }

    public Mono<TdApi.Ok> requestQrCodeAuthentication(long[] otherUserIds) {
        TdApi.RequestQrCodeAuthentication req = new TdApi.RequestQrCodeAuthentication();
        req.otherUserIds = otherUserIds;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Session / active devices
    // ------------------------------------------------------------------

    public Mono<TdApi.Sessions> getActiveSessions() {
        return send(new TdApi.GetActiveSessions());
    }

    public Mono<TdApi.Ok> terminateSession(long sessionId) {
        TdApi.TerminateSession req = new TdApi.TerminateSession();
        req.sessionId = sessionId;
        return send(req);
    }

    public Mono<TdApi.Ok> terminateAllOtherSessions() {
        return send(new TdApi.TerminateAllOtherSessions());
    }

    // ------------------------------------------------------------------
    // Connected websites
    // ------------------------------------------------------------------

    public Mono<TdApi.ConnectedWebsites> getConnectedWebsites() {
        return send(new TdApi.GetConnectedWebsites());
    }

    public Mono<TdApi.Ok> disconnectWebsite(long websiteId) {
        TdApi.DisconnectWebsite req = new TdApi.DisconnectWebsite();
        req.websiteId = websiteId;
        return send(req);
    }

    public Mono<TdApi.Ok> disconnectAllWebsites() {
        return send(new TdApi.DisconnectAllWebsites());
    }
}
