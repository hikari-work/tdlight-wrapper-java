package com.yann.api;

import com.yann.core.TdlightOperations;
import it.tdlight.client.SimpleTelegramClient;
import java.util.function.Supplier;
import it.tdlight.jni.TdApi;
import reactor.core.publisher.Mono;

/**
 * Reactive wrapper for TDLib file operations (upload, download, manage).
 */
public final class FilesApi extends TdlightOperations {

    public FilesApi(Supplier<SimpleTelegramClient> client) {
        super(client);
    }

    // ------------------------------------------------------------------
    // Uploading
    // ------------------------------------------------------------------

    /**
     * Preliminary-upload a local file to Telegram servers (registers it for sending).
     *
     * @param localPath absolute path of the local file
     * @param fileType  e.g. {@link TdApi.FileTypePhoto}, {@link TdApi.FileTypeDocument}, …
     * @param priority  1–32, higher = faster
     */
    public Mono<TdApi.File> uploadFile(String localPath, TdApi.FileType fileType, int priority) {
        TdApi.InputFileLocal inputFile = new TdApi.InputFileLocal();
        inputFile.path = localPath;

        TdApi.PreliminaryUploadFile req = new TdApi.PreliminaryUploadFile();
        req.file = inputFile;
        req.fileType = fileType;
        req.priority = priority;
        return send(req);
    }

    public Mono<TdApi.File> uploadPhoto(String localPath) {
        return uploadFile(localPath, new TdApi.FileTypePhoto(), 16);
    }

    public Mono<TdApi.File> uploadDocument(String localPath) {
        return uploadFile(localPath, new TdApi.FileTypeDocument(), 16);
    }

    public Mono<TdApi.File> uploadVideo(String localPath) {
        return uploadFile(localPath, new TdApi.FileTypeVideo(), 16);
    }

    public Mono<TdApi.File> uploadAudio(String localPath) {
        return uploadFile(localPath, new TdApi.FileTypeAudio(), 16);
    }

    public Mono<TdApi.Ok> cancelUpload(int fileId) {
        TdApi.CancelPreliminaryUploadFile req = new TdApi.CancelPreliminaryUploadFile();
        req.fileId = fileId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Downloading
    // ------------------------------------------------------------------

    /**
     * Start or continue downloading a file.
     *
     * @param fileId      id of the file to download
     * @param priority    1–32
     * @param offset      byte offset from which to start
     * @param limit       number of bytes to download (0 = unlimited)
     * @param synchronous block until the download completes
     */
    public Mono<TdApi.File> downloadFile(int fileId, int priority, long offset,
                                          long limit, boolean synchronous) {
        TdApi.DownloadFile req = new TdApi.DownloadFile();
        req.fileId = fileId;
        req.priority = priority;
        req.offset = offset;
        req.limit = limit;
        req.synchronous = synchronous;
        return send(req);
    }

    /** Download file with defaults (priority 16, full file, async). */
    public Mono<TdApi.File> downloadFile(int fileId) {
        return downloadFile(fileId, 16, 0, 0, false);
    }

    /** Download file and wait until completion. */
    public Mono<TdApi.File> downloadFileSync(int fileId) {
        return downloadFile(fileId, 16, 0, 0, true);
    }

    public Mono<TdApi.Ok> cancelDownload(int fileId, boolean onlyIfPending) {
        TdApi.CancelDownloadFile req = new TdApi.CancelDownloadFile();
        req.fileId = fileId;
        req.onlyIfPending = onlyIfPending;
        return send(req);
    }

    public Mono<TdApi.Ok> cancelDownload(int fileId) {
        return cancelDownload(fileId, false);
    }

    // ------------------------------------------------------------------
    // Fetching file metadata
    // ------------------------------------------------------------------

    /** Get local file info by id. */
    public Mono<TdApi.File> getFile(int fileId) {
        TdApi.GetFile req = new TdApi.GetFile();
        req.fileId = fileId;
        return send(req);
    }

    /** Resolve a remote file id to a File object. */
    public Mono<TdApi.File> getRemoteFile(String remoteFileId, TdApi.FileType fileType) {
        TdApi.GetRemoteFile req = new TdApi.GetRemoteFile();
        req.remoteFileId = remoteFileId;
        req.fileType = fileType;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Deleting local copy
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> deleteFile(int fileId) {
        TdApi.DeleteFile req = new TdApi.DeleteFile();
        req.fileId = fileId;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Download manager
    // ------------------------------------------------------------------

    public Mono<TdApi.FileDownloadedPrefixSize> getFileDownloadedPrefixSize(int fileId,
                                                                               long offset) {
        TdApi.GetFileDownloadedPrefixSize req = new TdApi.GetFileDownloadedPrefixSize();
        req.fileId = fileId;
        req.offset = offset;
        return send(req);
    }

    public Mono<TdApi.FoundFileDownloads> searchFileDownloads(String query,
                                                                 boolean onlyActive,
                                                                 boolean onlyCompleted,
                                                                 String offset, int limit) {
        TdApi.SearchFileDownloads req = new TdApi.SearchFileDownloads();
        req.query = query;
        req.onlyActive = onlyActive;
        req.onlyCompleted = onlyCompleted;
        req.offset = offset;
        req.limit = limit;
        return send(req);
    }

    public Mono<TdApi.File> addFileToDownloads(int fileId, long chatId, long messageId,
                                               int priority) {
        TdApi.AddFileToDownloads req = new TdApi.AddFileToDownloads();
        req.fileId = fileId;
        req.chatId = chatId;
        req.messageId = messageId;
        req.priority = priority;
        return send(req);
    }

    public Mono<TdApi.Ok> removeFileFromDownloads(int fileId, boolean deleteFromCache) {
        TdApi.RemoveFileFromDownloads req = new TdApi.RemoveFileFromDownloads();
        req.fileId = fileId;
        req.deleteFromCache = deleteFromCache;
        return send(req);
    }

    public Mono<TdApi.Ok> removeAllFilesFromDownloads(boolean onlyActive, boolean onlyCompleted,
                                                         boolean deleteFromCache) {
        TdApi.RemoveAllFilesFromDownloads req = new TdApi.RemoveAllFilesFromDownloads();
        req.onlyActive = onlyActive;
        req.onlyCompleted = onlyCompleted;
        req.deleteFromCache = deleteFromCache;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Generated files
    // ------------------------------------------------------------------

    public Mono<TdApi.Ok> setFileGenerationProgress(long generationId, long expectedSize,
                                                       long localPrefixSize) {
        TdApi.SetFileGenerationProgress req = new TdApi.SetFileGenerationProgress();
        req.generationId = generationId;
        req.expectedSize = expectedSize;
        req.localPrefixSize = localPrefixSize;
        return send(req);
    }

    public Mono<TdApi.Ok> finishFileGeneration(long generationId, TdApi.Error error) {
        TdApi.FinishFileGeneration req = new TdApi.FinishFileGeneration();
        req.generationId = generationId;
        req.error = error;
        return send(req);
    }

    // ------------------------------------------------------------------
    // Convenience: build InputFile objects
    // ------------------------------------------------------------------

    public static TdApi.InputFile localFile(String path) {
        TdApi.InputFileLocal f = new TdApi.InputFileLocal();
        f.path = path;
        return f;
    }

    public static TdApi.InputFile remoteFile(String fileId) {
        TdApi.InputFileRemote f = new TdApi.InputFileRemote();
        f.id = fileId;
        return f;
    }

    public static TdApi.InputFile fileById(int fileId) {
        TdApi.InputFileId f = new TdApi.InputFileId();
        f.id = fileId;
        return f;
    }
}
