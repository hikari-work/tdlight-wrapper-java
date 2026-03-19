package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import java.util.function.Supplier;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for all TDLib sticker-related functions.
 */
public final class StickersApi extends TdlightOperations {

    public StickersApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Getting stickers
    // ------------------------------------------------------------------

    /** Get stickers suggested for a given query in a chat. */
    public Mono<TdApi.Stickers> getStickers(TdApi.StickerType stickerType, String query,
                                              int limit, long chatId) {
        TdApi.GetStickers req = new TdApi.GetStickers();
        req.stickerType = stickerType;
        req.query = query;
        req.limit = limit;
        req.chatId = chatId;
        return send(req);
    }

    /** Search stickers in all sets by emoji. */
    public Mono<TdApi.Stickers> searchStickers(TdApi.StickerType stickerType,
                                                 String query, int limit) {
        TdApi.SearchStickers req = new TdApi.SearchStickers();
        req.stickerType = stickerType;
        req.query = query;
        req.limit = limit;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Sticker sets
    // ------------------------------------------------------------------

    /** Get a sticker set by its id. */
    public Mono<TdApi.StickerSet> getStickerSet(long setId) {
        TdApi.GetStickerSet req = new TdApi.GetStickerSet();
        req.setId = setId;
        return send(req);
    }

    /** Resolve a sticker set by name (short name). */
    public Mono<TdApi.StickerSet> searchStickerSet(String name) {
        TdApi.SearchStickerSet req = new TdApi.SearchStickerSet();
        req.name = name;
        return send(req);
    }

    /** Get all installed sticker sets of a given type. */
    public Mono<TdApi.StickerSets> getInstalledStickerSets(TdApi.StickerType stickerType) {
        TdApi.GetInstalledStickerSets req = new TdApi.GetInstalledStickerSets();
        req.stickerType = stickerType;
        return send(req);
    }

    /** Get trending sticker sets. */
    public Mono<TdApi.TrendingStickerSets> getTrendingStickerSets(TdApi.StickerType stickerType,
                                                                    int offset, int limit) {
        TdApi.GetTrendingStickerSets req = new TdApi.GetTrendingStickerSets();
        req.stickerType = stickerType;
        req.offset = offset;
        req.limit = limit;
        return send(req);
    }

    /** Search public sticker sets by name. */
    public Mono<TdApi.StickerSets> searchStickerSets(String query) {
        TdApi.SearchStickerSets req = new TdApi.SearchStickerSets();
        req.query = query;
        return send(req);
    }

    /** Get sticker sets attached to a given file. */
    public Mono<TdApi.StickerSets> getAttachedStickerSets(int fileId) {
        TdApi.GetAttachedStickerSets req = new TdApi.GetAttachedStickerSets();
        req.fileId = fileId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Installing / uninstalling
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> installStickerSet(long setId, boolean isArchived) {
        TdApi.ChangeStickerSet req = new TdApi.ChangeStickerSet();
        req.setId = setId;
        req.isInstalled = true;
        req.isArchived = isArchived;
        return send(req);
    }

    public Mono<TdApi.Ok> uninstallStickerSet(long setId) {
        TdApi.ChangeStickerSet req = new TdApi.ChangeStickerSet();
        req.setId = setId;
        req.isInstalled = false;
        req.isArchived = false;
        return send(req);
    }

    public Mono<TdApi.Ok> reorderInstalledStickerSets(TdApi.StickerType stickerType,
                                                         long[] stickerSetIds) {
        TdApi.ReorderInstalledStickerSets req = new TdApi.ReorderInstalledStickerSets();
        req.stickerType = stickerType;
        req.stickerSetIds = stickerSetIds;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Creating / managing custom sticker sets
    // ------------------------------------------------------------------

    /**
     * Create a new sticker set owned by {@code userId}.
     * The bot must be an administrator in the Telegram Stickers channel.
     */
    public Mono<TdApi.StickerSet> createNewStickerSet(long userId, String title, String name,
                                                        TdApi.StickerType stickerType,
                                                        TdApi.InputSticker[] stickers,
                                                        String source) {
        TdApi.CreateNewStickerSet req = new TdApi.CreateNewStickerSet();
        req.userId = userId;
        req.title = title;
        req.name = name;
        req.stickerType = stickerType;
        req.stickers = stickers;
        req.source = source;
        return send(req);
    }

    public Mono<TdApi.Ok> addStickerToSet(long userId, String name,
                                           TdApi.InputSticker sticker) {
        TdApi.AddStickerToSet req = new TdApi.AddStickerToSet();
        req.userId = userId;
        req.name = name;
        req.sticker = sticker;
        return send(req);
    }

    public Mono<TdApi.Ok> setStickerPositionInSet(TdApi.InputFile sticker, int position) {
        TdApi.SetStickerPositionInSet req = new TdApi.SetStickerPositionInSet();
        req.sticker = sticker;
        req.position = position;
        return send(req);
    }

    public Mono<TdApi.Ok> removeStickerFromSet(TdApi.InputFile sticker) {
        TdApi.RemoveStickerFromSet req = new TdApi.RemoveStickerFromSet();
        req.sticker = sticker;
        return send(req);
    }

    public Mono<TdApi.Ok> setStickerSetThumbnail(long userId, String name,
                                                   TdApi.InputFile thumbnail) {
        TdApi.SetStickerSetThumbnail req = new TdApi.SetStickerSetThumbnail();
        req.userId = userId;
        req.name = name;
        req.thumbnail = thumbnail;
        return send(req);
    }

    public Mono<TdApi.Ok> setStickerEmojis(TdApi.InputFile sticker, String emojis) {
        TdApi.SetStickerEmojis req = new TdApi.SetStickerEmojis();
        req.sticker = sticker;
        req.emojis = emojis;
        return send(req);
    }

    public Mono<TdApi.Ok> setStickerKeywords(TdApi.InputFile sticker, String[] keywords) {
        TdApi.SetStickerKeywords req = new TdApi.SetStickerKeywords();
        req.sticker = sticker;
        req.keywords = keywords;
        return send(req);
    }

    public Mono<TdApi.Ok> setStickerMaskPosition(TdApi.InputFile sticker,
                                                   TdApi.MaskPosition maskPosition) {
        TdApi.SetStickerMaskPosition req = new TdApi.SetStickerMaskPosition();
        req.sticker = sticker;
        req.maskPosition = maskPosition;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Recent / favourite stickers
    // ------------------------------------------------------------------

    public Mono<TdApi.Stickers> getRecentStickers(boolean isAttached) {
        TdApi.GetRecentStickers req = new TdApi.GetRecentStickers();
        req.isAttached = isAttached;
        return send(req);
    }

    public Mono<TdApi.Stickers> addRecentSticker(boolean isAttached, TdApi.InputFile sticker) {
        TdApi.AddRecentSticker req = new TdApi.AddRecentSticker();
        req.isAttached = isAttached;
        req.sticker = sticker;
        return send(req);
    }

    public Mono<TdApi.Ok> removeRecentSticker(boolean isAttached, TdApi.InputFile sticker) {
        TdApi.RemoveRecentSticker req = new TdApi.RemoveRecentSticker();
        req.isAttached = isAttached;
        req.sticker = sticker;
        return send(req);
    }

    public Mono<TdApi.Ok> clearRecentStickers(boolean isAttached) {
        TdApi.ClearRecentStickers req = new TdApi.ClearRecentStickers();
        req.isAttached = isAttached;
        return send(req);
    }

    public Mono<TdApi.Stickers> getFavoriteStickers() {
        return send(new TdApi.GetFavoriteStickers());
    }

    public Mono<TdApi.Ok> addFavoriteSticker(TdApi.InputFile sticker) {
        TdApi.AddFavoriteSticker req = new TdApi.AddFavoriteSticker();
        req.sticker = sticker;
        return send(req);
    }

    public Mono<TdApi.Ok> removeFavoriteSticker(TdApi.InputFile sticker) {
        TdApi.RemoveFavoriteSticker req = new TdApi.RemoveFavoriteSticker();
        req.sticker = sticker;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Emojis
    // ------------------------------------------------------------------

    public Mono<TdApi.Emojis> getStickerEmojis(TdApi.InputFile sticker) {
        TdApi.GetStickerEmojis req = new TdApi.GetStickerEmojis();
        req.sticker = sticker;
        return send(req);
    }

    public Mono<TdApi.EmojiKeywords> searchEmojis(String text, String[] inputLanguageCodes) {
        TdApi.SearchEmojis req = new TdApi.SearchEmojis();
        req.text = text;
        req.inputLanguageCodes = inputLanguageCodes;
        return send(req);
    }

    public Mono<TdApi.Stickers> getCustomEmojiStickers(long[] customEmojiIds) {
        TdApi.GetCustomEmojiStickers req = new TdApi.GetCustomEmojiStickers();
        req.customEmojiIds = customEmojiIds;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Animations (GIFs)
    // ------------------------------------------------------------------

    public Mono<TdApi.Animations> getSavedAnimations() {
        return send(new TdApi.GetSavedAnimations());
    }

    public Mono<TdApi.Ok> addSavedAnimation(TdApi.InputFile animation) {
        TdApi.AddSavedAnimation req = new TdApi.AddSavedAnimation();
        req.animation = animation;
        return send(req);
    }

    public Mono<TdApi.Ok> removeSavedAnimation(TdApi.InputFile animation) {
        TdApi.RemoveSavedAnimation req = new TdApi.RemoveSavedAnimation();
        req.animation = animation;
        return send(req);
    }
}
