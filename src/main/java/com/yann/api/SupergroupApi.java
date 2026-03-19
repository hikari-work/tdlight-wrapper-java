package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import java.util.function.Supplier;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for supergroup/channel administration operations.
 */
public final class SupergroupApi extends TdlightOperations {

    public SupergroupApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Info
    // ------------------------------------------------------------------

    public Mono<TdApi.Supergroup> getSupergroup(long supergroupId) {
        TdApi.GetSupergroup req = new TdApi.GetSupergroup();
        req.supergroupId = supergroupId;
        return send(req);
    }

    public Mono<TdApi.SupergroupFullInfo> getSupergroupFullInfo(long supergroupId) {
        TdApi.GetSupergroupFullInfo req = new TdApi.GetSupergroupFullInfo();
        req.supergroupId = supergroupId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Members
    // ------------------------------------------------------------------

    public Mono<TdApi.ChatMembers> getMembers(long supergroupId,
                                               TdApi.SupergroupMembersFilter filter,
                                               int offset, int limit) {
        TdApi.GetSupergroupMembers req = new TdApi.GetSupergroupMembers();
        req.supergroupId = supergroupId;
        req.filter = filter != null ? filter : new TdApi.SupergroupMembersFilterRecent();
        req.offset = offset;
        req.limit = limit;
        return send(req);
    }

    public Mono<TdApi.ChatMembers> getAdministrators(long supergroupId) {
        return getMembers(supergroupId, new TdApi.SupergroupMembersFilterAdministrators(),
                0, 200);
    }

    public Mono<TdApi.ChatMembers> getBannedMembers(long supergroupId, String query,
                                                      int offset, int limit) {
        TdApi.SupergroupMembersFilterBanned filter = new TdApi.SupergroupMembersFilterBanned();
        filter.query = query;
        return getMembers(supergroupId, filter, offset, limit);
    }

    public Mono<TdApi.ChatMembers> getRestrictedMembers(long supergroupId, String query,
                                                          int offset, int limit) {
        TdApi.SupergroupMembersFilterRestricted filter =
                new TdApi.SupergroupMembersFilterRestricted();
        filter.query = query;
        return getMembers(supergroupId, filter, offset, limit);
    }

    public Mono<TdApi.ChatMembers> searchMembers(long supergroupId, String query,
                                                   int limit) {
        TdApi.SupergroupMembersFilterSearch filter = new TdApi.SupergroupMembersFilterSearch();
        filter.query = query;
        return getMembers(supergroupId, filter, 0, limit);
    }

    // ------------------------------------------------------------------
    // Setting member status
    // ------------------------------------------------------------------

    /**
     * Set a member's status (ban, restrict, promote, kick, etc.).
     */
    public Mono<TdApi.Ok> setMemberStatus(long chatId, TdApi.MessageSender memberId,
                                           TdApi.ChatMemberStatus status) {
        TdApi.SetChatMemberStatus req = new TdApi.SetChatMemberStatus();
        req.chatId = chatId;
        req.memberId = memberId;
        req.status = status;
        return send(req);
    }

    /** Ban a user until a unix timestamp (0 = forever). */
    public Mono<TdApi.Ok> banUser(long chatId, long userId, int bannedUntilDate,
                                   boolean revokeMessages) {
        TdApi.MessageSenderUser sender = new TdApi.MessageSenderUser();
        sender.userId = userId;

        TdApi.BanChatMember req = new TdApi.BanChatMember();
        req.chatId = chatId;
        req.memberId = sender;
        req.bannedUntilDate = bannedUntilDate;
        req.revokeMessages = revokeMessages;
        return send(req);
    }

    /** Permanently ban a user and delete their messages. */
    public Mono<TdApi.Ok> banUserPermanently(long chatId, long userId) {
        return banUser(chatId, userId, 0, true);
    }

    /** Unban a previously banned user. */
    public Mono<TdApi.Ok> unbanUser(long chatId, long userId) {
        TdApi.MessageSenderUser sender = new TdApi.MessageSenderUser();
        sender.userId = userId;
        TdApi.ChatMemberStatusLeft left = new TdApi.ChatMemberStatusLeft();
        return setMemberStatus(chatId, sender, left);
    }

    /** Kick (remove) a user without banning. */
    public Mono<TdApi.Ok> kickUser(long chatId, long userId) {
        TdApi.MessageSenderUser sender = new TdApi.MessageSenderUser();
        sender.userId = userId;
        TdApi.ChatMemberStatusLeft left = new TdApi.ChatMemberStatusLeft();
        return setMemberStatus(chatId, sender, left);
    }

    /**
     * Restrict a user with granular {@link TdApi.ChatPermissions}.
     *
     * @param restrictedUntilDate 0 = forever
     */
    public Mono<TdApi.Ok> restrictUser(long chatId, long userId,
                                        TdApi.ChatPermissions permissions,
                                        int restrictedUntilDate) {
        TdApi.MessageSenderUser sender = new TdApi.MessageSenderUser();
        sender.userId = userId;

        TdApi.ChatMemberStatusRestricted status = new TdApi.ChatMemberStatusRestricted();
        status.isMember = true;
        status.restrictedUntilDate = restrictedUntilDate;
        status.permissions = permissions;

        return setMemberStatus(chatId, sender, status);
    }

    /**
     * Promote a user to administrator.
     *
     * @param customTitle admin title badge shown in the chat
     */
    public Mono<TdApi.Ok> promoteUser(long chatId, long userId,
                                       TdApi.ChatAdministratorRights rights,
                                       String customTitle) {
        TdApi.MessageSenderUser sender = new TdApi.MessageSenderUser();
        sender.userId = userId;

        TdApi.ChatMemberStatusAdministrator status = new TdApi.ChatMemberStatusAdministrator();
        status.rights = rights;
        status.customTitle = customTitle;
        status.canBeEdited = true;

        return setMemberStatus(chatId, sender, status);
    }

    // ------------------------------------------------------------------
    // Supergroup settings
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> setUsername(long supergroupId, String username) {
        TdApi.SetSupergroupUsername req = new TdApi.SetSupergroupUsername();
        req.supergroupId = supergroupId;
        req.username = username;
        return send(req);
    }

    public Mono<TdApi.Ok> setStickerSet(long supergroupId, long stickerSetId) {
        TdApi.SetSupergroupStickerSet req = new TdApi.SetSupergroupStickerSet();
        req.supergroupId = supergroupId;
        req.stickerSetId = stickerSetId;
        return send(req);
    }

    public Mono<TdApi.Ok> toggleAllHistoryAvailable(long supergroupId,
                                                      boolean isAllHistoryAvailable) {
        TdApi.ToggleSupergroupIsAllHistoryAvailable req =
                new TdApi.ToggleSupergroupIsAllHistoryAvailable();
        req.supergroupId = supergroupId;
        req.isAllHistoryAvailable = isAllHistoryAvailable;
        return send(req);
    }

    public Mono<TdApi.Ok> toggleHasAggressiveAntiSpamEnabled(long supergroupId,
                                                                boolean hasAggressiveAntiSpamEnabled) {
        TdApi.ToggleSupergroupHasAggressiveAntiSpamEnabled req =
                new TdApi.ToggleSupergroupHasAggressiveAntiSpamEnabled();
        req.supergroupId = supergroupId;
        req.hasAggressiveAntiSpamEnabled = hasAggressiveAntiSpamEnabled;
        return send(req);
    }

    public Mono<TdApi.Ok> toggleIsChannel(long supergroupId) {
        TdApi.ToggleSupergroupIsBroadcastGroup req =
                new TdApi.ToggleSupergroupIsBroadcastGroup();
        req.supergroupId = supergroupId;
        return send(req);
    }

    public Mono<TdApi.Ok> toggleSignMessages(long supergroupId, boolean signMessages,
                                              boolean showMessageSender) {
        TdApi.ToggleSupergroupSignMessages req = new TdApi.ToggleSupergroupSignMessages();
        req.supergroupId = supergroupId;
        req.signMessages = signMessages;
        req.showMessageSender = showMessageSender;
        return send(req);
    }

    public Mono<TdApi.Ok> setSlowModeDelay(long supergroupId, int slowModeDelay) {
        TdApi.SetChatSlowModeDelay req = new TdApi.SetChatSlowModeDelay();
        req.chatId = supergroupIdToChatId(supergroupId);
        req.slowModeDelay = slowModeDelay;
        return send(req);
    }

    public Mono<TdApi.Ok> deleteSupergroupMessages(long supergroupId,
                                                     TdApi.MessageSender senderId) {
        TdApi.DeleteChatMessagesBySender req = new TdApi.DeleteChatMessagesBySender();
        req.chatId = supergroupIdToChatId(supergroupId);
        req.senderId = senderId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Join requests
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> approveChatJoinRequest(long chatId, long userId) {
        TdApi.ProcessChatJoinRequest req = new TdApi.ProcessChatJoinRequest();
        req.chatId = chatId;
        req.userId = userId;
        req.approve = true;
        return send(req);
    }

    public Mono<TdApi.Ok> declineChatJoinRequest(long chatId, long userId) {
        TdApi.ProcessChatJoinRequest req = new TdApi.ProcessChatJoinRequest();
        req.chatId = chatId;
        req.userId = userId;
        req.approve = false;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Helper
    // ------------------------------------------------------------------

    /** TDLib encodes supergroup chat ids as -(1000000000000 + supergroupId). */
    private static long supergroupIdToChatId(long supergroupId) {
        return -1000000000000L - supergroupId;
    }
}
