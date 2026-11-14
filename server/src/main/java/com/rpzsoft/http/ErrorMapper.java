package com.rpzsoft.http;

import com.rpzsoft.common.OpsException;
import com.rpzsoft.common.OpsRuntimeException;
import com.rpzsoft.common.Vld;

/**
 * Maps a thrown {@link Throwable} to the HTTP status and client-facing message that should be sent
 * back through {@link HttpHandler}.
 *
 * <p>Intentional errors ({@link OpsException}/{@link OpsRuntimeException}) carry their own status
 * and message. Validation errors ({@link IllegalArgumentException}) become 400. Everything else -
 * NPEs, driver failures, etc. - is treated as an unexpected server error: the client only sees
 * {@code Internal Server Error} while the real exception is logged with its stack trace.
 */
public final class ErrorMapper {

    /** Message returned for any 5xx response, so internal details never leak to clients. */
    public static final String SERVER_ERROR = "Internal Server Error";

    private ErrorMapper() {
    }

    public static HttpStatus status(Throwable err) {
        if (err instanceof OpsException) {
            return HttpStatus.valueOf(((OpsException) err).getOpsStatus());
        }
        if (err instanceof OpsRuntimeException) {
            return HttpStatus.valueOf(((OpsRuntimeException) err).getOpsStatus());
        }
        if (err instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST;
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    public static String message(Throwable err, HttpStatus status) {
        HttpStatus st = status == null ? status(err) : status;
        if (st.isServerError()) {
            return SERVER_ERROR;
        }
        String msg = err == null ? null : err.getMessage();
        return Vld.isEmpty(msg) ? st.toString() : msg;
    }
}
