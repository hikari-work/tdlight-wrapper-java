package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import java.util.function.Supplier;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for all TDLib chat-management functions.
 */
public final class ChatsApi extends TdlightOperations {

    public ChatsApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Fetching chats
    // ------------------------------------------------------------------

    /** Get a single chat by id. */
    public Mono<TdApi.Chat> getChat(long chatId) {
        TdApi.GetChat req = new TdApi.GetChat();
        req.chatId = chatId;
        return send(req);
    }

    /** Fetch the chat list (ordered by position). */
    public Mono<TdApi.Chats> getChats(int limit) {
        TdApi.GetChats req = new TdApi.GetChats();
        req.chatList = new TdApi.ChatListMain();
        req.limit = limit;
        return send(req);
    }

    /** Fetch chats from the archive. */
    public Mono<TdApi.Chats> getArchivedChats(int limit) {
        TdApi.GetChats req = new TdApi.GetChats();
        req.chatList = new TdApi.ChatListArchive();
        req.limit = limit;
        return send(req);
    }

    /** Fetch chats in a folder. */
    public Mono<TdApi.Chats> getChatsInFolder(int chatFolderId, int limit) {
        TdApi.GetChats req = new TdApi.GetChats();
        req.chatList = new TdApi.ChatListFolder(chatFolderId);
        req.limit = limit;
        return send(req);
    }

    /** Search public chats (channels / groups) by username. */
    public Mono<TdApi.Chats> searchPublicChats(String query) {
        TdApi.SearchPublicChats req = new TdApi.SearchPublicChats();
        req.query = query;
        return send(req);
    }

    /** Search among known chats. */
    public Mono<TdApi.Chats> searchChats(String query, int limit) {
        TdApi.SearchChats req = new TdApi.SearchChats();
        req.query = query;
        req.limit = limit;
        return send(req);
    }

    /** Search chats on the server. */
    public Mono<TdApi.Chats> searchChatsOnServer(String query, int limit) {
        TdApi.SearchChatsOnServer req = new TdApi.SearchChatsOnServer();
        req.query = query;
        req.limit = limit;
        return send(req);
    }

    /** Get recommended chats. */
    public Mono<TdApi.Chats> getRecommendedChats() {
        return send(new TdApi.GetRecommendedChats());
    }

    /** Resolve a chat by username or phone-number invite link. */
    public Mono<TdApi.Chat> searchPublicChat(String username) {
        TdApi.SearchPublicChat req = new TdApi.SearchPublicChat();
        req.username = username;
        return send(req);
    }

    /** Get basic group full information. */
    public Mono<TdApi.BasicGroupFullInfo> getBasicGroupFullInfo(long basicGroupId) {
        TdApi.GetBasicGroupFullInfo req = new TdApi.GetBasicGroupFullInfo();
        req.basicGroupId = basicGroupId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Creating chats
    // ------------------------------------------------------------------

    /** Open a private chat with a user. */
    public Mono<TdApi.Chat> createPrivateChat(long userId, boolean force) {
        TdApi.CreatePrivateChat req = new TdApi.CreatePrivateChat();
        req.userId = userId;
        req.force = force;
        return send(req);
    }

    /** Create a new basic group with given members. */
    public Mono<TdApi.CreatedBasicGroupChat> createGroupChat(long[] userIds, String title,
                                                               int messageAutoDeleteTime) {
        TdApi.CreateNewBasicGroupChat req = new TdApi.CreateNewBasicGroupChat();
        req.userIds = userIds;
        req.title = title;
        req.messageAutoDeleteTime = messageAutoDeleteTime;
        return send(req);
    }

    /** Create a new supergroup or channel. */
    public Mono<TdApi.Chat> createSupergroupChat(String title, boolean isChannel,
                                                    String description,
                                                    int messageAutoDeleteTime,
                                                    boolean forImport) {
        TdApi.CreateNewSupergroupChat req = new TdApi.CreateNewSupergroupChat();
        req.title = title;
        req.isChannel = isChannel;
        req.description = description;
        req.messageAutoDeleteTime = messageAutoDeleteTime;
        req.forImport = forImport;
        return send(req);
    }

    /** Create a new secret chat. */
    public Mono<TdApi.Chat> createSecretChat(long userId) {
        TdApi.CreateNewSecretChat req = new TdApi.CreateNewSecretChat();
        req.userId = userId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Joining / leaving
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> joinChat(long chatId) {
        TdApi.JoinChat req = new TdApi.JoinChat();
        req.chatId = chatId;
        return send(req);
    }

    public Mono<TdApi.Chat> joinChatByInviteLink(String inviteLink) {
        TdApi.JoinChatByInviteLink req = new TdApi.JoinChatByInviteLink();
        req.inviteLink = inviteLink;
        return send(req);
    }

    public Mono<TdApi.Ok> leaveChat(long chatId) {
        TdApi.LeaveChat req = new TdApi.LeaveChat();
        req.chatId = chatId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Deleting / clearing
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> deleteChat(long chatId) {
        TdApi.DeleteChat req = new TdApi.DeleteChat();
        req.chatId = chatId;
        return send(req);
    }

    public Mono<TdApi.Ok> deleteChatHistory(long chatId, boolean removeFromChatList,
                                              boolean revoke) {
        TdApi.DeleteChatHistory req = new TdApi.DeleteChatHistory();
        req.chatId = chatId;
        req.removeFromChatList = removeFromChatList;
        req.revoke = revoke;
        return send(req);
    }

    public Mono<TdApi.Ok> deleteChatMessagesFromUser(long chatId, long userId) {
        TdApi.DeleteAllCallMessages req = new TdApi.DeleteAllCallMessages();
        req.revoke = true;
        // Note: use DeleteChatMessagesBySender for filtering by sender
        TdApi.DeleteChatMessagesBySender bySender = new TdApi.DeleteChatMessagesBySender();
        bySender.chatId = chatId;
        TdApi.MessageSenderUser sender = new TdApi.MessageSenderUser();
        sender.userId = userId;
        bySender.senderId = sender;
        return send(bySender);
    }

    public Mono<TdApi.Ok> clearChatHistory(long chatId) {
        return deleteChatHistory(chatId, false, false);
    }

    // ------------------------------------------------------------------
    // Chat settings
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> setChatTitle(long chatId, String title) {
        TdApi.SetChatTitle req = new TdApi.SetChatTitle();
        req.chatId = chatId;
        req.title = title;
        return send(req);
    }

    public Mono<TdApi.Ok> setChatDescription(long chatId, String description) {
        TdApi.SetChatDescription req = new TdApi.SetChatDescription();
        req.chatId = chatId;
        req.description = description;
        return send(req);
    }

    public Mono<TdApi.Ok> setChatPhoto(long chatId, TdApi.InputChatPhoto photo) {
        TdApi.SetChatPhoto req = new TdApi.SetChatPhoto();
        req.chatId = chatId;
        req.photo = photo;
        return send(req);
    }

    public Mono<TdApi.Ok> deleteChatPhoto(long chatId) {
        return setChatPhoto(chatId, null);
    }

    public Mono<TdApi.Ok> setChatPermissions(long chatId, TdApi.ChatPermissions permissions) {
        TdApi.SetChatPermissions req = new TdApi.SetChatPermissions();
        req.chatId = chatId;
        req.permissions = permissions;
        return send(req);
    }


    public Mono<TdApi.Ok> setChatNotificationSettings(long chatId,
                                                         TdApi.ChatNotificationSettings settings) {
        TdApi.SetChatNotificationSettings req = new TdApi.SetChatNotificationSettings();
        req.chatId = chatId;
        req.notificationSettings = settings;
        return send(req);
    }

    public Mono<TdApi.Ok> muteChat(long chatId) {
        TdApi.ChatNotificationSettings settings = new TdApi.ChatNotificationSettings();
        settings.useDefaultMuteFor = false;
        settings.muteFor = Integer.MAX_VALUE;
        settings.useDefaultSound = true;
        settings.useDefaultShowPreview = true;
        settings.useDefaultDisablePinnedMessageNotifications = true;
        settings.useDefaultDisableMentionNotifications = true;
        return setChatNotificationSettings(chatId, settings);
    }

    public Mono<TdApi.Ok> setChatMessageAutoDeleteTime(long chatId, int messageAutoDeleteTime) {
        TdApi.SetChatMessageAutoDeleteTime req = new TdApi.SetChatMessageAutoDeleteTime();
        req.chatId = chatId;
        req.messageAutoDeleteTime = messageAutoDeleteTime;
        return send(req);
    }

    public Mono<TdApi.Ok> toggleChatIsPinned(long chatId, TdApi.ChatList chatList,
                                               boolean isPinned) {
        TdApi.ToggleChatIsPinned req = new TdApi.ToggleChatIsPinned();
        req.chatId = chatId;
        req.chatList = chatList;
        req.isPinned = isPinned;
        return send(req);
    }

    public Mono<TdApi.Ok> toggleChatIsMarkedAsUnread(long chatId, boolean isMarkedAsUnread) {
        TdApi.ToggleChatIsMarkedAsUnread req = new TdApi.ToggleChatIsMarkedAsUnread();
        req.chatId = chatId;
        req.isMarkedAsUnread = isMarkedAsUnread;
        return send(req);
    }

    public Mono<TdApi.Ok> setChatDefaultDisableNotification(long chatId,
                                                               boolean defaultDisableNotification) {
        TdApi.ToggleChatDefaultDisableNotification req =
                new TdApi.ToggleChatDefaultDisableNotification();
        req.chatId = chatId;
        req.defaultDisableNotification = defaultDisableNotification;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Chat members
    // ------------------------------------------------------------------

    public Mono<TdApi.ChatMember> getChatMember(long chatId, TdApi.MessageSender memberId) {
        TdApi.GetChatMember req = new TdApi.GetChatMember();
        req.chatId = chatId;
        req.memberId = memberId;
        return send(req);
    }

    public Mono<TdApi.ChatMembers> getChatMembers(long chatId,
                                                    TdApi.ChatMembersFilter filter,
                                                    int offset, int limit) {
        TdApi.SearchChatMembers req = new TdApi.SearchChatMembers();
        req.chatId = chatId;
        req.query = "";
        req.filter = filter != null ? filter : new TdApi.ChatMembersFilterMembers();
        req.limit = limit;
        return send(req);
    }

    public Mono<TdApi.ChatMembers> getChatAdministrators(long chatId) {
        return getChatMembers(chatId, new TdApi.ChatMembersFilterAdministrators(), 0, 200);
    }

    public Mono<TdApi.ChatMembers> getChatBannedMembers(long chatId, int offset, int limit) {
        return getChatMembers(chatId, new TdApi.ChatMembersFilterBanned(), offset, limit);
    }

    public Mono<TdApi.ChatMembers> getChatRestrictedMembers(long chatId, int offset, int limit) {
        return getChatMembers(chatId, new TdApi.ChatMembersFilterRestricted(), offset, limit);
    }

    // ------------------------------------------------------------------
    // Invite links
    // ------------------------------------------------------------------

    public Mono<TdApi.ChatInviteLink> createChatInviteLink(long chatId, String name,
                                                              int expirationDate, int memberLimit,
                                                              boolean createsJoinRequest) {
        TdApi.CreateChatInviteLink req = new TdApi.CreateChatInviteLink();
        req.chatId = chatId;
        req.name = name;
        req.expirationDate = expirationDate;
        req.memberLimit = memberLimit;
        req.createsJoinRequest = createsJoinRequest;
        return send(req);
    }

    public Mono<TdApi.ChatInviteLink> editChatInviteLink(long chatId, String inviteLink,
                                                            String name, int expirationDate,
                                                            int memberLimit,
                                                            boolean createsJoinRequest) {
        TdApi.EditChatInviteLink req = new TdApi.EditChatInviteLink();
        req.chatId = chatId;
        req.inviteLink = inviteLink;
        req.name = name;
        req.expirationDate = expirationDate;
        req.memberLimit = memberLimit;
        req.createsJoinRequest = createsJoinRequest;
        return send(req);
    }

    /** Revoke an invite link. Returns the updated list of invite links. */
    public Mono<TdApi.ChatInviteLinks> revokeChatInviteLink(long chatId, String inviteLink) {
        TdApi.RevokeChatInviteLink req = new TdApi.RevokeChatInviteLink();
        req.chatId = chatId;
        req.inviteLink = inviteLink;
        return send(req);
    }

    public Mono<TdApi.ChatInviteLinkInfo> checkChatInviteLink(String inviteLink) {
        TdApi.CheckChatInviteLink req = new TdApi.CheckChatInviteLink();
        req.inviteLink = inviteLink;
        return send(req);
    }

    public Mono<TdApi.ChatInviteLinks> getChatInviteLinks(long chatId, long creatorUserId,
                                                            boolean isRevoked, int offsetDate,
                                                            String offsetInviteLink, int limit) {
        TdApi.GetChatInviteLinks req = new TdApi.GetChatInviteLinks();
        req.chatId = chatId;
        req.creatorUserId = creatorUserId;
        req.isRevoked = isRevoked;
        req.offsetDate = offsetDate;
        req.offsetInviteLink = offsetInviteLink;
        req.limit = limit;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Open / close (affects unread count tracking)
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> openChat(long chatId) {
        TdApi.OpenChat req = new TdApi.OpenChat();
        req.chatId = chatId;
        return send(req);
    }

    public Mono<TdApi.Ok> closeChat(long chatId) {
        TdApi.CloseChat req = new TdApi.CloseChat();
        req.chatId = chatId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Chat folders
    // ------------------------------------------------------------------

    /** Get the full description of a single chat folder. Use UpdateChatFolders for the full list. */
    public Mono<TdApi.ChatFolder> getChatFolder(int chatFolderId) {
        TdApi.GetChatFolder req = new TdApi.GetChatFolder();
        req.chatFolderId = chatFolderId;
        return send(req);
    }

    public Mono<TdApi.ChatFolderInfo> createChatFolder(TdApi.ChatFolder folder) {
        TdApi.CreateChatFolder req = new TdApi.CreateChatFolder();
        req.folder = folder;
        return send(req);
    }

    public Mono<TdApi.ChatFolderInfo> editChatFolder(int chatFolderId, TdApi.ChatFolder folder) {
        TdApi.EditChatFolder req = new TdApi.EditChatFolder();
        req.chatFolderId = chatFolderId;
        req.folder = folder;
        return send(req);
    }

    public Mono<TdApi.Ok> deleteChatFolder(int chatFolderId, long[] leaveChatIds) {
        TdApi.DeleteChatFolder req = new TdApi.DeleteChatFolder();
        req.chatFolderId = chatFolderId;
        req.leaveChatIds = leaveChatIds;
        return send(req);
    }

    public Mono<TdApi.Ok> addChatToFolder(long chatId, int chatFolderId) {
        TdApi.AddChatToList req = new TdApi.AddChatToList();
        req.chatId = chatId;
        req.chatList = new TdApi.ChatListFolder(chatFolderId);
        return send(req);
    }
}
