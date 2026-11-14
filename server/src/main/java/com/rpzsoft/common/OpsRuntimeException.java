

package com.rpzsoft.common;


/**
 * default to application/json
 */
public class OpsRuntimeException extends RuntimeException implements RpzException {

    public OpsStatus status;
    //type of the exception message - for protocol translation purpose
    public String contentType;

    public OpsRuntimeException(OpsStatus status) {
        super(status.toString());
        this.status = status;
    }

    public OpsRuntimeException(OpsStatus status, Throwable cause) {
        this(status, cause.getMessage(), cause);
    }

    public OpsRuntimeException(OpsStatus status, String message) {
        this(status, message, null, null);
    }

    public OpsRuntimeException(OpsStatus status, String message, Throwable cause) {
        this(status, message, cause, null);
    }

    public OpsRuntimeException(OpsStatus status, String message, MediaType contentType) {
        this(status, message, null, contentType.str());
    }

    public OpsRuntimeException(OpsStatus status, String message, String contentType) {
        this(status, message, null, contentType);
    }

    public OpsRuntimeException(OpsStatus status, String message, Throwable cause, String contentType) {
        super(RpzException.getErr(message, true), cause);
        this.status = status == null? OpsStatus.UNKNOWN: status;
        this.contentType = contentType;
    }

    public Json getJsonMsg() {
        return new Json("err", getMessage());
    }

    public String getJsonStrMsg(){
        return RpzException.getMsg(getMessage());
    }

    public <T> T getStatus() {
        return (T) status;
    }

    public OpsStatus getOpsStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    @Override
    public String toString() {
        return status + ": "+super.getLocalizedMessage();
    }
}
