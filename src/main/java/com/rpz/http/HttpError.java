package com.rpz.http;


import com.rpz.common.OpsException;
import com.rpz.common.OpsStatus;

/**
 * 
 */
public class HttpError extends OpsException {

    HttpStatus status;
    boolean processed = false;
    String [] headers;

    public HttpError(HttpStatus status) {
        this(status, status.toString(), null, null);
    }

    public HttpError(HttpStatus status, Throwable cause) {
        this(status, cause.getMessage(), cause);
    }

    public HttpError(HttpStatus status, String message) {
        this(status, message, null, null);
    }

    public HttpError(HttpStatus status, String message, Throwable cause) {
        this(status, message, cause, null);
    }

    public HttpError(HttpStatus status, String message, String contentType) {
        this(status, message, null, contentType);
    }

    public HttpError(HttpStatus status, String message, Throwable cause, String contentType) {
        super(OpsStatus.valueOf(status.code), message, cause, contentType);
        this.status = status;
    }


    public HttpStatus getStatus() {
        return status;
    }

    public String getContentType() {
        return (String) ref;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed() {
        this.processed = true;
    }

    public String[] getHeaders() {
        return headers;
    }

    @Override
    public String getMessage() {
        return super.getJsonStrMsg();
    }
}
