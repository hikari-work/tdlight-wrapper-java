package com.yann.config;

import it.tdlight.client.APIToken;
import it.tdlight.client.TDLibSettings;
import it.tdlight.jni.TdApi;

import java.nio.file.Path;

/**
 * Immutable configuration for {@link com.yann.ReactiveTelegramClient}.
 * Build with {@link #builder(int, String)}.
 */
public final class TelegramClientConfig {

    private final int apiId;
    private final String apiHash;
    private final Path databasePath;
    private final Path downloadPath;
    private final boolean ephemeral;
    private final String systemLanguageCode;
    private final String deviceModel;
    private final String systemVersion;
    private final String applicationVersion;
    private final TdApi.Proxy proxy;

    private TelegramClientConfig(Builder b) {
        this.apiId = b.apiId;
        this.apiHash = b.apiHash;
        this.databasePath = b.databasePath;
        this.downloadPath = b.downloadPath;
        this.ephemeral = b.ephemeral;
        this.systemLanguageCode = b.systemLanguageCode;
        this.deviceModel = b.deviceModel;
        this.systemVersion = b.systemVersion;
        this.applicationVersion = b.applicationVersion;
        this.proxy = b.proxy;
    }

    /** Convert to the TDLight {@link TDLibSettings} object. */
    public TDLibSettings toTDLibSettings() {
        APIToken token = new APIToken(apiId, apiHash);
        TDLibSettings settings = TDLibSettings.create(token);

        if (ephemeral) {
            // Use a unique temp directory for ephemeral sessions (no persistence)
            java.nio.file.Path tmp = java.nio.file.Path.of(
                System.getProperty("java.io.tmpdir"),
                "tdlight-ephemeral-" + System.nanoTime()
            );
            settings.setDatabaseDirectoryPath(tmp.resolve("data"));
            settings.setDownloadedFilesDirectoryPath(tmp.resolve("downloads"));
            settings.setFileDatabaseEnabled(false);
            settings.setChatInfoDatabaseEnabled(false);
            settings.setMessageDatabaseEnabled(false);
        } else {
            settings.setDatabaseDirectoryPath(databasePath);
            settings.setDownloadedFilesDirectoryPath(downloadPath);
        }

        settings.setSystemLanguageCode(systemLanguageCode);
        settings.setDeviceModel(deviceModel);
        settings.setSystemVersion(systemVersion);
        settings.setApplicationVersion(applicationVersion);

        // Note: proxies are configured after start via TdApi.AddProxy / TdApi.EnableProxy

        return settings;
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    public static Builder builder(int apiId, String apiHash) {
        return new Builder(apiId, apiHash);
    }

    public static final class Builder {

        private final int apiId;
        private final String apiHash;

        private Path databasePath = Path.of("tdlight-session/data");
        private Path downloadPath = Path.of("tdlight-session/downloads");
        private boolean ephemeral = false;
        private String systemLanguageCode = "en";
        private String deviceModel = "Desktop";
        private String systemVersion = "Unknown";
        private String applicationVersion = "1.0";
        private TdApi.Proxy proxy = null;

        private Builder(int apiId, String apiHash) {
            this.apiId = apiId;
            this.apiHash = apiHash;
        }

        public Builder databasePath(Path path) {
            this.databasePath = path;
            return this;
        }

        public Builder downloadPath(Path path) {
            this.downloadPath = path;
            return this;
        }

        /** Use an in-memory database – no files written to disk. */
        public Builder ephemeral(boolean ephemeral) {
            this.ephemeral = ephemeral;
            return this;
        }

        public Builder systemLanguageCode(String code) {
            this.systemLanguageCode = code;
            return this;
        }

        public Builder deviceModel(String model) {
            this.deviceModel = model;
            return this;
        }

        public Builder systemVersion(String version) {
            this.systemVersion = version;
            return this;
        }

        public Builder applicationVersion(String version) {
            this.applicationVersion = version;
            return this;
        }

        public Builder proxy(TdApi.Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        /** Convenience: SOCKS5 proxy. */
        public Builder socks5Proxy(String host, int port, String username, String password) {
            TdApi.ProxyTypeSocks5 type = new TdApi.ProxyTypeSocks5();
            type.username = username;
            type.password = password;
            TdApi.Proxy p = new TdApi.Proxy();
            p.server = host;
            p.port = port;
            p.type = type;
            this.proxy = p;
            return this;
        }

        /** Convenience: HTTP proxy. */
        public Builder httpProxy(String host, int port, String username, String password) {
            TdApi.ProxyTypeHttp type = new TdApi.ProxyTypeHttp();
            type.username = username;
            type.password = password;
            type.httpOnly = false;
            TdApi.Proxy p = new TdApi.Proxy();
            p.server = host;
            p.port = port;
            p.type = type;
            this.proxy = p;
            return this;
        }

        public TelegramClientConfig build() {
            return new TelegramClientConfig(this);
        }
    }
}
