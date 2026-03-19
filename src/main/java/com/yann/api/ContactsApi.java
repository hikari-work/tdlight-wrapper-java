package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import java.util.function.Supplier;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for TDLib contacts operations.
 */
public final class ContactsApi extends TdlightOperations {

    public ContactsApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Reading contacts
    // ------------------------------------------------------------------

    /** Get all local contacts (user ids only). */
    public Mono<TdApi.Users> getContacts() {
        return send(new TdApi.GetContacts());
    }

    /** Search contacts by display name prefix. */
    public Mono<TdApi.Users> searchContacts(String query, int limit) {
        TdApi.SearchContacts req = new TdApi.SearchContacts();
        req.query = query;
        req.limit = limit;
        return send(req);
    }

    /** Look up users who have a specific phone number. */
    public Mono<TdApi.Users> lookupUserByPhoneNumber(String phoneNumber) {
        TdApi.SearchUserByPhoneNumber req = new TdApi.SearchUserByPhoneNumber();
        req.phoneNumber = phoneNumber;
        return send(req).map(user -> {
            TdApi.Users users = new TdApi.Users();
            users.userIds = new long[]{user.id};
            users.totalCount = 1;
            return users;
        });
    }

    // ------------------------------------------------------------------
    // Adding / removing contacts
    // ------------------------------------------------------------------

    /**
     * Add or update a contact.
     *
     * @param userId           user to add
     * @param firstName        first name override
     * @param lastName         last name override
     * @param phoneNumber      phone number (may be empty)
     * @param sharePhoneNumber whether to share the current user's number with this contact
     */
    public Mono<TdApi.Ok> addContact(long userId, String firstName, String lastName,
                                      String phoneNumber, boolean sharePhoneNumber) {
        TdApi.Contact contact = new TdApi.Contact();
        contact.userId = userId;
        contact.firstName = firstName;
        contact.lastName = lastName;
        contact.phoneNumber = phoneNumber;
        contact.vcard = "";

        TdApi.AddContact req = new TdApi.AddContact();
        req.contact = contact;
        req.sharePhoneNumber = sharePhoneNumber;
        return send(req);
    }

    /** Remove contacts by user id. */
    public Mono<TdApi.Ok> removeContacts(long[] userIds) {
        TdApi.RemoveContacts req = new TdApi.RemoveContacts();
        req.userIds = userIds;
        return send(req);
    }

    /** Bulk-import contacts from an external source (phone book sync). */
    public Mono<TdApi.ImportedContacts> importContacts(TdApi.Contact[] contacts) {
        TdApi.ImportContacts req = new TdApi.ImportContacts();
        req.contacts = contacts;
        return send(req);
    }

    /** Clear all previously imported contacts. */
    public Mono<TdApi.Ok> clearImportedContacts() {
        return send(new TdApi.ClearImportedContacts());
    }

    /** Change the phone number visibility setting for all contacts. */
    public Mono<TdApi.ImportedContacts> changeImportedContacts(TdApi.Contact[] contacts) {
        TdApi.ChangeImportedContacts req = new TdApi.ChangeImportedContacts();
        req.contacts = contacts;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Close contacts
    // ------------------------------------------------------------------

    public Mono<TdApi.Users> getCloseFriends() {
        return send(new TdApi.GetCloseFriends());
    }

    public Mono<TdApi.Ok> setCloseFriends(long[] userIds) {
        TdApi.SetCloseFriends req = new TdApi.SetCloseFriends();
        req.userIds = userIds;
        return send(req);
    }
}
