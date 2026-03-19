package com.yann.exception;

import it.tdlight.jni.TdApi;

/**
 * Wraps a TDLib error into a standard Java exception.
 */
public class TelegramException extends RuntimeException {

    private final int code;

    public TelegramException(TdApi.Error error) {
        super("[" + error.code + "] " + error.message);
        this.code = error.code;
    }

    public TelegramException(int code, String message) {
        super("[" + code + "] " + message);
        this.code = code;
    }

    /** TDLib error code (e.g. 400, 401, 403, 404, 420, 500). */
    public int getCode() {
        return code;
    }

    /** True when the error is a flood-wait (420). */
    public boolean isFloodWait() {
        return code == 420;
    }

    /** True when the request was unauthorized (401). */
    public boolean isUnauthorized() {
        return code == 401;
    }
}
